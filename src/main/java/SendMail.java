import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import odin.util.AppConfig;

import org.apache.log4j.Logger;

public class SendMail {
	protected static Logger logger = Logger.getLogger(SendMail.class);

	public static void main(String[] args) throws IOException {
		sendMessage(
				"morten@lileng.com",
				"mlileng@merkleinc.com",
				"test subject",
				"<img src=\"http://capacity-odin.rhcloud.com/images/agile-odin.jpg\" alt=\"odin\" width=\"800\" height=\"75\" />");
	}

	public static void sendMessage(String toEmailAddress, String ccEmailAddress, String subject,
			String content) throws IOException {
		String emailOverride = System.getenv("ODIN_EMAILOVERRIDE");
		if(emailOverride != null) {
			logger.warn("ODIN_EMAILOVERRIDE set. Overriding the email address found (" + toEmailAddress + "), with the following: " + emailOverride);
			toEmailAddress = emailOverride;
		}
		final String username = AppConfig.getAppConfig().getProperty("sendmail.username");
		final String password = AppConfig.getAppConfig().getProperty("sendmail.password");;
		logger.info("Attempting to send email message to " + toEmailAddress);

		// Sender's email ID needs to be mentioned
		String from = "odin@lileng.com";

		// Assuming you are sending email from localhost
		String host = "smtp.gmail.com";

		// Get system properties
		Properties props = System.getProperties();

		// Setup mail server
		props.setProperty("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

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
			message.addRecipient(Message.RecipientType.CC, new InternetAddress(
					ccEmailAddress));

			// Set Subject: header field
			message.setSubject(subject);

			// Send the actual HTML message, as big as you like
			message.setContent(content, "text/html");

			// Send message
			Transport.send(message);
			logger.info("Sent message successfully to " + toEmailAddress);
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
}