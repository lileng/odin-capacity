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

import odin.config.Configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.sendgrid.SendGrid;

public class SendMail {
	protected static Logger LOG = Logger.getLogger(SendMail.class);

	public static void main(String[] args) throws Exception {
		String cctest[] = { "test@example.com" };
		String name = "Bob";
		String hoursRemainingCapacity = "10";
		String hoursRemainingWork = "80";
		StringBuffer sb = new StringBuffer();
		sb.append("<li>[JIRA-123] - An example task</li>");
		sb.append("<li>[JIRA-124] - Second example task with much much more text in it which can potentiall cros lines</li>");
		sb.append("<li>[JIRA-125] - A third example task</li>");
		sendCapacityMessage(
				"odin@lileng.com",
				cctest,
				"Odin Capacity Status - You are underallocated",
				"<h1>You are Underallocated</h1>" + "<p>Hi "
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
						+ "<ul>" + sb.toString() + "</ul>");
	}

	public static void sendCapacityMessage(String toEmailAddress,
			String ccEmailAddress[], String subject, String content)
			throws Exception {
		LOG.info("Sending email notification to: " + toEmailAddress);
		SendGrid sendgrid = new SendGrid(Configuration.getDefaultValue("sendgrid.api_key"));
		SendGrid.Email email = new SendGrid.Email();
		email.addTo(toEmailAddress);
		email.addCc(ccEmailAddress);
		email.setFrom("odin@lileng.com");
		email.setSubject(subject);
		email.setHtml(content);
		email.setTemplateId(Configuration.getDefaultValue("sendgrid.capacity.template_id"));

		SendGrid.Response response = sendgrid.send(email);
		LOG.info("Email sent. Response: " + response.getMessage());

	}
	
	public static void sendBacklogManagementMessage(String[] notificationList,
			String ccEmailAddress[], String subject, String content)
			throws Exception {
		LOG.info("Sending email notification to: " + ToStringBuilder.reflectionToString(notificationList));
		SendGrid sendgrid = new SendGrid(Configuration.getDefaultValue("sendgrid.api_key"));
		SendGrid.Email email = new SendGrid.Email();
		email.addTo(notificationList);
		if(ccEmailAddress != null)
			email.addCc(ccEmailAddress);
		email.setFrom("odin@lileng.com");
		email.setSubject(subject);
		email.setHtml(content);
		email.setTemplateId(Configuration.getDefaultValue("sendgrid.backlog_mgmt.template_id"));

		SendGrid.Response response = sendgrid.send(email);
		LOG.info("Email sent. Response: " + response.getMessage());

	}
}