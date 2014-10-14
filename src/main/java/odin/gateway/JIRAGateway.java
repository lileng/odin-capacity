package odin.gateway;

import java.io.IOException;
import java.net.URI;

import odin.config.Configuration;
import odin.util.JEncrypt;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

public class JIRAGateway {
	protected static Logger logger = Logger.getLogger(JIRAGateway.class);
	static JiraRestClient restClient = null;

	public static JiraRestClient getRestClient() {
		logger.debug("getRestClient");
		if (restClient == null) {
			String usr = new String(JEncrypt.decode(Configuration
					.getDefaultValue("gateway.jira.username").getBytes()));
			logger.info("url="
					+ Configuration.getDefaultValue("gateway.jira.url"));

			String pw = new String(JEncrypt.decode(Configuration
					.getDefaultValue("gateway.jira.password").getBytes()));
			// logger.debug("pw=" + pw);

			AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
			restClient = factory.createWithBasicHttpAuthentication(URI
					.create(Configuration.getDefaultValue("gateway.jira.url")),
					usr, pw);
		}
		return restClient;

	}
	public static void closeRestClient() {
		 if (restClient != null) {
             try {
            	 logger.info("Closing restClient...");
				restClient.close();
				restClient = null;
			} catch (IOException e) {
				logger.warn(e);
			}
         }
	}

}
