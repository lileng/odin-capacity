/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package odin.gateway;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import odin.config.Configuration;
import odin.demand.ManageBacklog;
import odin.domain.Individual;
import odin.domain.Observation;
import odin.domain.Sprint;
import odin.util.OdinResponse;
import odin.util.ReadGoogleSpreadsheet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * <ol>
 * <li>Looks up an active sprint and participants of the sprint.
 * <li>Queries JIRA for tasks in this sprint for a specific individual.
 * <li>Compares hours of capacity for an individual with the remaining hours
 * estimated for the tasks assigned to this individual for this specific sprint.
 * <li>Sends email to the individual if remaining hours > reported capacity for
 * the remaining time.
 * </ol>
 * 
 *
 */
public class ReportAllocation {
	protected static Logger logger = Logger.getLogger(ReportAllocation.class);

	static String[] notificationList = Configuration.getDefaultValue(
			"odin.notify.prod.to").split(" ");

	public static void main(String[] args) throws Exception {
		logger.info("Starting ReportAllocation");
		printEnvMap();
		printClassPath();
		OdinResponse res = new OdinResponse();
		process(res);
		sendJobMessage(res);
		logger.info("Stopping ReportAllocation");
	}

	private static void sendJobMessage(OdinResponse res) throws Exception {
		String header = "The Job ReportAllocation Completed Normally";
		if (res.getStatusCode() != 0) {
			header = "The Job ReportAllocation Completed Abnormally";
		}
		try {
			SendMail.sendBacklogManagementMessage(notificationList, null,
					header, res.toString());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Unable to send email notification", e);
		}
	}

	private static void process(OdinResponse res) {
		logger.info("process");
		StringBuffer msg = new StringBuffer();
		msg.append("<p>ReportAllocation Completed.");
		List<Sprint> activeSprints = Sprint.getActiveSprints();
		List<Individual> activeIndividuals = null;

		for (Sprint sprint : activeSprints) {
			activeIndividuals = Sprint
					.getActiveParticipantsNotContactedToday(sprint
							.getSprintName());
			msg.append(" Processed the following individuals for sprint: "
					+ sprint.getSprintName() + "<ul>");
			for (Individual i : activeIndividuals) {
				try {
					processIndividual(i.getUserID(), sprint.getSprintName(),
							i.getEmailAddress(), i.getFirstName(),
							i.getManagerEmailAddress(), false);
				} catch (Exception e) {
					msg.append("<li>GOT ERROR WHEN ATTEMPTING TO PROCESS THE FOLLOWING RECORD: "
							+ i.getFirstName()
							+ " "
							+ i.getLastName()
							+ ", user="
							+ i.getUserID()
							+ ", email="
							+ i.getEmailAddress());
					msg.append("<br>----->   " + e.getStackTrace() + "</li>");
					res.setStatusCode(-1);
					res.setReasonPhrase(e.getMessage());
					logger.error("failed", e);
				}
				msg.append("<li>" + i.getFirstName() + " " + i.getLastName()
						+ ", user=" + i.getUserID() + ", email="
						+ i.getEmailAddress() + "</li>");
			}
			msg.append("</ul>");
		}
		res.setStatusCode(0);
		res.setReasonPhrase("OK");
		res.setMessageBody(msg.toString());

	}

	public static void processIndividual(String username, String sprint,
			String emailAddress, String name, String mgrEmail, boolean test)
			throws Exception {
		int hoursRemainingCapacity = Sprint.getRemainingAvailability(sprint,
				username);
		int minutesRemainingWork = 0;
		int hoursRemainingWork = 0;
		StringBuffer sb = new StringBuffer();

		Set<Hashtable<String, String>> collectedData = JiraHarvester.collect(
				null, sprint, username);

		for (Hashtable<String, String> keyValue : collectedData) {
			if (keyValue.get("remainingEstimateMinutes") != null
					&& !keyValue.get("remainingEstimateMinutes").equals("null")) {
				minutesRemainingWork = minutesRemainingWork
						+ Integer.parseInt(keyValue
								.get("remainingEstimateMinutes"));
				if (!keyValue.get("remainingEstimateMinutes").equals("0")) {
					sb.append("<li>[");
					sb.append(keyValue.get("key"));
					sb.append("] - ");
					sb.append(keyValue.get("summary"));
				}
			}
		}

		if (minutesRemainingWork > 0) {
			hoursRemainingWork = minutesRemainingWork / 60;
			logger.info("hoursRemainingWork for " + username + "="
					+ hoursRemainingWork);
			logger.info("hoursRemainingCapacity for " + username + "="
					+ hoursRemainingCapacity);
		}
		int delta = hoursRemainingCapacity - hoursRemainingWork;
		String overAllocationThreshold = Configuration
				.getDefaultValue("capacity.threshold.overallocation");
		String underAllocationThreshold = Configuration
				.getDefaultValue("capacity.threshold.underallocation");

		if (delta < Integer.parseInt(overAllocationThreshold)) {
			// Send mail
			logger.info("Overallocation: HoursRemainingCapacity < hoursRemainingWork for "
					+ username + ". Sending email to notify...");
			if (!test) {
				Observation.recordObservation(username, "Overallocation",
						"Overallocation: HoursRemainingCapacity < hoursRemainingWork for "
								+ username, "hoursRemainingCapacity",
						hoursRemainingCapacity, "hoursRemainingWork",
						hoursRemainingWork);

				sendOverallocatedMail(sprint, emailAddress, name,
						hoursRemainingCapacity, hoursRemainingWork, sb,
						mgrEmail);
				Individual.recordUserContactedNow(username);
			} else {
				logger.warn("*********    In testing mode   *************");
			}

		} else if (delta > Integer.parseInt(underAllocationThreshold)) {
			// Send mail
			logger.info("Underallocation: HoursRemainingCapacity > hoursRemainingWork for "
					+ username + ". Sending email to notify...");
			if (!test) {
				Observation.recordObservation(username, "Underallocation",
						"Underallocation: HoursRemainingCapacity > hoursRemainingWork for "
								+ username, "hoursRemainingCapacity",
						hoursRemainingCapacity, "hoursRemainingWork",
						hoursRemainingWork);
				sendUnderallocatedMail(sprint, emailAddress, name,
						hoursRemainingCapacity, hoursRemainingWork, sb,
						mgrEmail);
				Individual.recordUserContactedNow(username);
			} else {
				logger.warn("*********    In testing mode   *************");
			}

		} else {
			Observation.recordObservation(username, "Allocation ok", "n/a",
					"hoursRemainingCapacity", hoursRemainingCapacity,
					"hoursRemainingWork", hoursRemainingWork);

		}
	}

	private static void sendUnderallocatedMail(String sprintName,
			String emailAddress, String name, int hoursRemainingCapacity,
			int hoursRemainingWork, StringBuffer sb, String mgrEmail)
			throws Exception {

		String[] cc = Configuration.getDefaultValue("gateway.sendmail.cc")
				.split(" ");
		ArrayList<String> ar = new ArrayList(Arrays.asList(cc));
		ar.add(mgrEmail);
		SendMail.sendCapacityMessage(
				emailAddress,
				ar.toArray(new String[ar.size()]),
				sprintName + " Capacity Status - " + name
						+ ", you are underallocated",
				"<h1>"
						+ name
						+ ", you are underallocated</h1>"
						+ "<p>Hi "
						+ name
						+ ". It looks like you may have hours available, and not enough tasks on your plate for "
						+ sprintName
						+ ". <ul>"
						+ "<li>Your estimated remaining capacity in hours: "
						+ hoursRemainingCapacity
						+ "<li>Hours for your estimated tasks: "
						+ hoursRemainingWork
						+ "</ul>"
						+ "<p>How do you resolve this? <ul>"
						+ "<li>Make sure your estimates for remaining tasks assigned to you are as accurate as they can."
						+ "<li>Let your team leader know that you need more work. In general, she/he will look at the other "
						+ "members. You may get tasks from overallocated resources. The next possibility is to bring more tasks from the product "
						+ "backlog into the current sprint."
						+ "<li>Note that all tickets in the sprint and product backlog should have a numeric ranking. "
						+ "Tickets with lower ranking number has higher priority."
						+ "</ul>"
						+ "<p>The tasks with hours still remaining are the following:"
						+ "<ul>" + sb.toString() + "</ul>");
	}

	private static void sendOverallocatedMail(String sprintName,
			String emailAddress, String name, int hoursRemainingCapacity,
			int hoursRemainingWork, StringBuffer sb, String mgrEmail)
			throws Exception {
		String[] cc = Configuration.getDefaultValue("gateway.sendmail.cc")
				.split(" ");
		ArrayList<String> ar = new ArrayList(Arrays.asList(cc));
		ar.add(mgrEmail);
		SendMail.sendCapacityMessage(
				emailAddress,
				ar.toArray(new String[ar.size()]),
				sprintName + " Capacity Status - " + name
						+ ", you are overallocated",
				"<h1>"
						+ name
						+ ", you are overallocated</h1>"
						+ "<p>Hi "
						+ name
						+ ". It looks like you may have more tasks than available hours left for "
						+ sprintName
						+ ". <ul>"
						+ "<li>Your estimated remaining capacity in hours: "
						+ hoursRemainingCapacity
						+ "<li>Hours for your estimated tasks: "
						+ hoursRemainingWork
						+ "</ul>"
						+ "<p>How do you resolve this? <ul>"
						+ "<li>Make sure you update the remaining hours on tasks with status 'In Progress' every day."
						+ "<li>All tasks should have a numeric ranking. "
						+ "Ask your team lead if you can push the task with highest number (prioritized lowest) out of the sprint. "
						+ "You can also ask if there are other ways you can get help - maybe a team member can take over a task from you."
						+ "</ul>"
						+ "<p>The tasks with hours still remaining are the following:"
						+ "<ul>" + sb.toString() + "</ul>");
	}

	private static void printClassPath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader) cl).getURLs();

		for (URL url : urls) {
			System.out.println(url.getFile());
		}
	}

	public static void printEnvMap() {
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			System.out.format("%s=%s%n", envName, env.get(envName));
		}
	}
}
