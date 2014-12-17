package odin.demand;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import odin.config.Configuration;
import odin.gateway.JIRAGateway;
import odin.gateway.SendMail;
import odin.util.JEncrypt;
import odin.util.OdinResponse;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class ManageBacklog {
	protected static Logger logger = Logger.getLogger(ManageBacklog.class);
	static String baseURL = Configuration.getDefaultValue("gateway.jira.url");
	static String usr = new String(JEncrypt.decode(Configuration
			.getDefaultValue("gateway.jira.username").getBytes()));

	static String pw = new String(JEncrypt.decode(Configuration
			.getDefaultValue("gateway.jira.password").getBytes()));
	static String[] notificationList = Configuration.getDefaultValue(
			"odin.notify.prod.to").split(" ");

	public static void main(String[] args) {
		OdinResponse res = new OdinResponse();
		try {
			updateSubtaskRanking(res);
			zeroRemainingHoursForDoneTasks(res);
			notifyAssigneeOfOldTickets(res);
			
			String header = "The Job ManageBacklog Completed Normally";
			if(res.getStatusCode()!=0){
				header = "The Job ManageBacklog Completed Abnormally";
			}
			try {
				SendMail.sendMessage(notificationList, null,
						header,
						res.toString());
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Unable to send email notification", e);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Unable to complete job", e);
			try {
				SendMail.sendMessage(notificationList, null,
						"The Job ManageBacklog Completed Abnormally",
						e.getMessage());
			} catch (IOException ee) {
				e.printStackTrace();
				logger.error("Unable to send email notification", ee);
			}
		}
	}

	public static void updateSubtaskRanking(OdinResponse res) {
		int updatedSubtasks = 0;
		String customRankingField1 = Configuration
				.getDefaultValue("task.calibration.jira.rankfield.1");
		String customRankingField2 = Configuration
				.getDefaultValue("task.calibration.jira.rankfield.2");
		List<String> parentKeys = getParentKeySelection();

		for (String parentKey : parentKeys) {

			// 1. Get rank from parent
			logger.info("Get rank from parentKey=" + parentKey);
			Issue issue = JIRAGateway.getRestClient().getIssueClient()
					.getIssue(parentKey).claim();
			int parentRank = 0;

			if (issue.getField(customRankingField1) != null) {
				IssueField field = issue.getField(customRankingField1);
				parentRank = (int) (double) field.getValue(); // TODO: Catch
				// ClassCastException or
				// NullPointerException

			} else if (issue.getField(customRankingField2) != null) {
				IssueField field = issue.getField(customRankingField2);
				parentRank = (int) (double) field.getValue(); // TODO: Catch
				// ClassCastException or
				// NullPointerException
			}
			logger.info("Parent key=" + parentKey + ", parentRank="
					+ parentRank);

			// 2. Get active subtasks
			String jql = "status not in (Done, Invalid) AND parent = "
					+ parentKey;
			int maxResults = 1000;
			int startAt = 0;
			logger.info("Executing JQL = " + jql);
			logger.info("maxResults=" + maxResults + ", startAt=" + startAt);
			Promise<SearchResult> searchResultPromise = null;

			searchResultPromise = JIRAGateway.getRestClient().getSearchClient()
					.searchJql(jql);

			SearchResult searchResult = searchResultPromise.claim();

			Iterable<? extends Issue> subtasks = searchResult.getIssues();
			for (Issue subtask : subtasks) {
				logger.info("subtask.key=" + subtask.getKey());
				// logger.info("getSummary = " + subtask.getSummary());

				// 3. Update active subtasks with new rank
				if (subtask.getField(customRankingField1) != null) {
					updateRank(subtask.getKey(), parentRank,
							customRankingField1);
				} else if (subtask.getField(customRankingField2) != null) {
					IssueField rank = subtask.getField(customRankingField2);
					int rankValue = 0;
					if(rank.getValue() != null) rankValue = ((Double) rank.getValue()).intValue();
					if (rankValue != parentRank) {
						updateRank(subtask.getKey(), parentRank,
								customRankingField2);
						updatedSubtasks++;
					}
				}
			}
		}
		res.setStatusCode(0);
		res.setReasonPhrase("OK");
		res.setMessageBody("<p>ManageBacklog.updateSubtaskRanking Completed Successfully. updatedSubtasks: "
				+ updatedSubtasks);

	}

	public static OdinResponse zeroRemainingHoursForDoneTasks(OdinResponse res) {
		// 1. Event (batch)
		// 2. Condition
		String jql = Configuration.getDefaultValue("task.calibration.jql.1"); // findDoneTasksWithRemaining

		int maxResults = 1000;
		int startAt = 0;
		logger.info("Executing JQL = " + jql);
		logger.info("maxResults=" + maxResults + ", startAt=" + startAt);
		Promise<SearchResult> searchResultPromise = null;

		searchResultPromise = JIRAGateway.getRestClient().getSearchClient()
				.searchJql(jql, maxResults, startAt, null);

		SearchResult searchResult = searchResultPromise.claim();

		Iterable<? extends Issue> issues = searchResult.getIssues();
		int totalToZero = 0;
		int totalZeroed = 0;
		for (Issue issue : issues) {
			int minutes = 0;
			if (issue.getField("timeestimate") != null) {
				minutes = (int) issue.getField("timeestimate").getValue();
			}
			String status = issue.getStatus().getName();
			totalToZero++;
			logger.info("Issue# " + totalToZero + ": issue.key="
					+ issue.getKey() + ". Status=" + status
					+ ". Remaining minutes=" + minutes);

			// 3. Perfom action
			totalZeroed = totalZeroed + setRemainingToZero(issue.getKey());
		}

		// 4. Communicate
		if (totalZeroed != totalToZero) {
			res.setStatusCode(-1);
			res.setMessageBody("<p>ManageBacklog.zeroRemainingHoursForDoneTasks Completed Abnormally: NOT ABLE TO UPDATE ALL TASKS<P>  <ul><li>Total issues found: "
					+ totalToZero
					+ "</li><li>Total issues updated to zero remaining: "
					+ totalZeroed + "</li></ul>");
		} else {
			res.setStatusCode(0);
			res.setMessageBody("<p>ManageBacklog.zeroRemainingHoursForDoneTasks Completed Successfully.<ul><li>Total issues found: " + totalToZero
					+ "</li><li>Total issues updated to zero remaining: "
					+ totalZeroed + "</li></ul>");
		}
		return res;
	}

	private static List<String> getParentKeySelection() {
		List<String> keys = new ArrayList<String>();
		String jql = Configuration
				.getDefaultValue("task.calibration.parent.selection");

		int maxResults = 1000;
		int startAt = 0;
		logger.info("Executing JQL = " + jql);
		logger.info("maxResults=" + maxResults + ", startAt=" + startAt);
		Promise<SearchResult> searchResultPromise = null;

		Set<String> fields = new HashSet<String>();
		// fields.add("key")
		searchResultPromise = JIRAGateway.getRestClient().getSearchClient()
				.searchJql(jql, maxResults, startAt, fields);

		SearchResult searchResult = searchResultPromise.claim();

		Iterable<? extends Issue> tasks = searchResult.getIssues();
		for (Issue task : tasks) {
			logger.info("task.key=" + task.getKey());
			keys.add(task.getKey());
		}
		return keys;
	}

	private static void updateRank(String key, int rank, String rankingField) {

		String url = baseURL + "/rest/api/2/issue/" + key;
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		// client.addFilter(new
		// com.sun.jersey.api.client.filter.LoggingFilter());
		client.addFilter(new HTTPBasicAuthFilter(usr, pw));
		String jsonRequest = "{\n   \"fields\": { \n    \"" + rankingField
				+ "\": " + rank + " \n   } \n }";
		logger.info("Preparing to update url=" + url + ", with JSON="
				+ jsonRequest);

		ClientResponse clientResponse = webResource
				.accept(MediaType.APPLICATION_JSON)
				.header("Content-Type", "application/json")
				.put(ClientResponse.class, jsonRequest);

		// String output = clientResponse.getEntity(String.class);
		// logger.info("Output from Server .... \n");
		// logger.info(output);
		if (clientResponse.getStatus() != 204) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ clientResponse.getStatus());
		}
	}

	private static int setRemainingToZero(String key) {

		String url = baseURL + "/rest/api/2/issue/" + key;
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		// client.addFilter(new
		// com.sun.jersey.api.client.filter.LoggingFilter());
		client.addFilter(new HTTPBasicAuthFilter(usr, pw));
		String jsonRequest = "{\n   \"fields\": { \n    \"timetracking\": {  \"remainingEstimate\": \"0\" } \n   } \n }";
		logger.info("Preparing to update url=" + url + ", with JSON="
				+ jsonRequest);

		ClientResponse clientResponse = webResource
				.accept(MediaType.APPLICATION_JSON)
				.header("Content-Type", "application/json")
				.put(ClientResponse.class, jsonRequest);

		// String output = clientResponse.getEntity(String.class);
		// logger.info("Output from Server .... \n");
		// logger.info(output);
		if (clientResponse.getStatus() != 204) {
			logger.warn(key + " failed update: HTTP error code : "
					+ clientResponse.getStatus());
			return 0;
		}
		logger.info(key + " updated successfully: HTTP code : "
				+ clientResponse.getStatus());
		return 1;
	}
	
	private static void notifyAssigneeOfOldTickets(OdinResponse res){
		// 1. Find current sprint
		// 2. Find tickets that are NOT associated to current sprint, "Sprint *", "Product Backlog" or unassigned
		//                  AND where Status is NOT "invalid" or "Done".
		//                  FROM projects in scope.
		// 3. Group tickets by assignee
		// 4. Get email addresses of assignee, manager and JIRA project owner
		// 5. Send email notification to assignee, with CC to manager and JIRA project owner.
		// 6. Insert observation into database
		// 7. Send notification to ODIN administrators.
		
	}
}
