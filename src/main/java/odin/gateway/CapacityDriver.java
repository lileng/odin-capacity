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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import odin.config.Configuration;
import odin.domain.Individual;
import odin.domain.Observation;
import odin.domain.Sprint;

import org.apache.log4j.Logger;

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
 */
@Path("/capacity")
public class CapacityDriver {
	protected static Logger logger = Logger.getLogger(CapacityDriver.class);
	// The @Context annotation allows us to have certain contextual objects
	// injected into this class.
	// UriInfo object allows us to get URI information (no kidding).
	@Context
	UriInfo uriInfo;

	// Another "injected" object. This allows us to use the information that's
	// part of any incoming request.
	// We could, for example, get header information, or the requestor's
	// address.
	@Context
	Request request;

	public static void main(String[] args) throws IOException {
		logger.info("Starting CapacityDriver");
		printEnvMap();
		printClassPath();
		process();
		logger.info("Stopping CapacityDriver");
	}

	@GET
	@Path("ping")
	@Produces(value = "application/json")
	public String ping() {
		return "{'ping': 'pong'}";
	}

	private static void process() throws IOException {
		logger.info("process");
		List<Sprint> activeSprints = Sprint.getActiveSprints();
		List<Individual> activeIndividuals = null;

		for (Sprint sprint : activeSprints) {
			activeIndividuals = Sprint
					.getActiveParticipantsNotContactedToday(sprint
							.getSprintName());
			for (Individual i : activeIndividuals) {
				processIndividual(i.getUserID(), sprint.getSprintName(),
						i.getEmailAddress(), i.getFirstName());
			}
		}

	}

	public static void processIndividual(String username, String sprint,
			String emailAddress, String name) throws IOException {
		int hoursRemainingCapacity = Sprint.getRemainingAvailability(sprint,
				username);
		int minutesRemainingWork = 0;
		;
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
			Observation.recordObservation(username, "Overallocation",
					"Overallocation: HoursRemainingCapacity < hoursRemainingWork for "
							+ username, "hoursRemainingCapacity",
					hoursRemainingCapacity, "hoursRemainingWork",
					hoursRemainingWork);
			sendOverallocatedMail(emailAddress, name, hoursRemainingCapacity,
					hoursRemainingWork, sb);
			Individual.recordUserContactedNow(username);

		} else if (delta > Integer.parseInt(underAllocationThreshold)) {
			// Send mail
			logger.info("Underallocation: HoursRemainingCapacity > hoursRemainingWork for "
					+ username + ". Sending email to notify...");
			Observation.recordObservation(username, "Underallocation",
					"Underallocation: HoursRemainingCapacity > hoursRemainingWork for "
							+ username, "hoursRemainingCapacity",
					hoursRemainingCapacity, "hoursRemainingWork",
					hoursRemainingWork);
			sendUnderallocatedMail(emailAddress, name, hoursRemainingCapacity,
					hoursRemainingWork, sb);
			Individual.recordUserContactedNow(username);

		} else {
			Observation.recordObservation(username, "Allocation ok", "n/a",
					"hoursRemainingCapacity", hoursRemainingCapacity,
					"hoursRemainingWork", hoursRemainingWork);

		}
	}

	private static void sendUnderallocatedMail(String emailAddress,
			String name, int hoursRemainingCapacity, int hoursRemainingWork,
			StringBuffer sb) throws IOException {
		SendMail.sendMessage(
				emailAddress,
				"mlileng@merkleinc.com",
				"Odin Capacity Status - You are underallocated",
				"<h1>Odin Underallocation Status</h1>" + "<p>Hi "
						+ name
						+ ". It looks like you may have hours available, and not enough tasks on your plate."
						+ "<ul>"
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
						+ "<ul>" + sb.toString() + "</ul>"
						+ "<p>Thank you,<br>"
						+ "--agile odin (odin@lileng.com)");
	}

	private static void sendOverallocatedMail(String emailAddress, String name,
			int hoursRemainingCapacity, int hoursRemainingWork, StringBuffer sb)
			throws IOException {
		SendMail.sendMessage(
				emailAddress,
				"mlileng@merkleinc.com",
				"Odin Capacity Status - You are overallocated",
				"<h1>Odin Overallocation Status</h1>"
						+ "<p>Hi "
						+ name
						+ ". It looks like you may have more tasks than available hours left in the current sprint. "
						+ "<ul>"
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
						+ "<ul>" + sb.toString() + "</ul>"
						+ "<p>Thank you,<br>"
						+ "--agile odin (odin@lileng.com)");
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
