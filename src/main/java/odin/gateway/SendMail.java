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
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;

import odin.config.Configuration;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class SendMail {
	protected static Logger logger = Logger.getLogger(SendMail.class);

	public static void main(String[] args) throws IOException {
		String cctest[] = { "mlileng@merkleinc.com" };
		sendMessage(
				"mol@lileng.com",
				cctest,
				"test subject",
				"<img src=\"http://capacity-odin.rhcloud.com/images/agile-odin.jpg\" alt=\"odin\" width=\"800\" height=\"75\" />");
	}

	public static void sendMessage(String toEmailAddress,
			String ccEmailAddress[], String subject, String content)
			throws IOException {
		String emailOverride = Configuration
				.getDefaultValue("gateway.email.override");
		if (emailOverride != null) {
			logger.warn("gateway.email.override set. Overriding the email address found "
					+ emailOverride);
			toEmailAddress = emailOverride;
		}
		final String username = Configuration
				.getDefaultValue("gateway.sendmail.username");
		final String password = Configuration
				.getDefaultValue("gateway.sendmail.password");
		logger.info("Attempting to send email message to " + toEmailAddress);

		// Sender's email ID needs to be mentioned
		String from = "odin@lileng.com";

		// Assuming you are sending email from localhost
		String host = Configuration.getDefaultValue("mail.smtp.host");

		// Get system properties
		Properties props = System.getProperties();

		// Setup mail server
		props.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					toEmailAddress));
			for (int i = 0; i < ccEmailAddress.length; i++) {
				message.addRecipient(Message.RecipientType.CC,
						new InternetAddress(ccEmailAddress[i]));
			}

			// Set Subject: header field
			message.setSubject(subject);

			// Send the actual HTML message, as big as you like
			message.setContent(content, "text/html");

			// Send message
			Transport.send(message);
			logger.info("Sent message successfully to " + toEmailAddress);
		} catch (MessagingException mex) {
			mex.printStackTrace();
			logger.error(mex);
		}
	}

	public static void sendMessage(String toEmailAddress[],
			String ccEmailAddress[], String subject, String content)
			throws IOException {

		logger.info("Attempting to send email message to " + ToStringBuilder.reflectionToString(toEmailAddress));
		

		// Sender's email ID needs to be mentioned
		String from = Configuration.getDefaultValue("odin.notify.prod.from");
		
		Client client = Client.create();
		Form form = new Form();
		form.add("api_user", Configuration.getDefaultValue("sendgrid.api_user"));
		form.add("api_key", Configuration.getDefaultValue("sendgrid.api_key"));
		// Set To: header field of the header.
		for (int i = 0; i < toEmailAddress.length; i++) {
					form.add("to[]", toEmailAddress[i]);
		}

		form.add("subject", subject);
		form.add("html", content);
		form.add("from", from);
		
		WebResource webResource = client.resource("https://api.sendgrid.com/api/mail.send.json");
		ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, form);
		String output = response.getEntity(String.class);
		 
		logger.info("Output from Server ....");
		logger.info(output);
		
		if (response.getStatus() != 200) {
			   throw new RuntimeException("Failed : HTTP error code : "
				+ response.getStatus());
			}
	 



			logger.info("Sent message successfully to " + ToStringBuilder.reflectionToString(toEmailAddress));
	}
}