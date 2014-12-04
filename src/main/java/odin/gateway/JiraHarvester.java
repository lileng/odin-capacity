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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.MediaType;

import odin.config.Configuration;
import odin.util.JEncrypt;
import odin.util.OdinResponse;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.util.concurrent.Promise;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * The harvester is responsible for collecting JIRA tasks from a given project.
 * 
 */
public class JiraHarvester {

	protected static Logger logger = Logger.getLogger(JiraHarvester.class);
	static String usr = new String(JEncrypt.decode(Configuration
			.getDefaultValue("gateway.jira.username").getBytes()));

	static String pw = new String(JEncrypt.decode(Configuration
			.getDefaultValue("gateway.jira.password").getBytes()));

	static String baseURL = Configuration.getDefaultValue("gateway.jira.url");

	public static int collectHoursRemainingWork(String project, String sprint,
			String assignee) throws IOException, InterruptedException,
			ExecutionException {
		int minutesRemainingWork = 0;
		int hoursRemainingWork = 0;
		Set<Hashtable<String, String>> collectedData = collect(project, sprint,
				assignee);

		for (Hashtable<String, String> keyValue : collectedData) {
			if (keyValue.get("remainingEstimateMinutes") != null
					&& !keyValue.get("remainingEstimateMinutes").equals("null")) {
				minutesRemainingWork = minutesRemainingWork
						+ Integer.parseInt(keyValue
								.get("remainingEstimateMinutes"));
			}
		}
		if (minutesRemainingWork > 0) {
			hoursRemainingWork = minutesRemainingWork / 60;
			logger.info("hoursRemainingWork=" + hoursRemainingWork);
		}
		return hoursRemainingWork;
	}

	public static Hashtable<String, String> getTasksWithRemainingWork(
			String project, String sprint, String assignee) throws IOException,
			InterruptedException, ExecutionException {
		Hashtable<String, String> kv = new Hashtable<String, String>();
		Set<Hashtable<String, String>> collectedData = collect(project, sprint,
				assignee);

		for (Hashtable<String, String> keyValue : collectedData) {
			if (keyValue.get("remainingEstimateMinutes") != null
					&& !keyValue.get("remainingEstimateMinutes").equals("null")
					&& !keyValue.get("remainingEstimateMinutes").equals("0")) {
				kv.put(keyValue.get("key"), keyValue.get("summary"));
			}
		}

		return kv;
	}

	private static List<String> getParentKeySelection() {
		List<String> keys = new ArrayList<String>();
		String jql = Configuration.getDefaultValue("task.calibration.parent.selection");

		int maxResults = 1000;
		int startAt = 0;
		logger.info("Executing JQL = " + jql);
		logger.info("maxResults=" + maxResults + ", startAt=" + startAt);
		Promise<SearchResult> searchResultPromise = null;
		
		Set<String> fields = new HashSet<String>();
		//fields.add("key")
		searchResultPromise = JIRAGateway.getRestClient().getSearchClient()
				.searchJql(jql,maxResults, startAt, fields);

		SearchResult searchResult = searchResultPromise.claim();

		Iterable<? extends Issue> tasks = searchResult.getIssues();
		for (Issue task : tasks) {
			logger.info("task.key=" + task.getKey());
			keys.add(task.getKey());
		}
		return keys;
	}

	public static OdinResponse updateSubtaskRanking() {
		OdinResponse response = new OdinResponse();
		String customRankingField1 = Configuration.getDefaultValue("task.calibration.jira.rankfield.1");
		String customRankingField2 = Configuration.getDefaultValue("task.calibration.jira.rankfield.2");
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
				if (issue.getField(customRankingField1) != null) {
					updateRank(subtask.getKey(), parentRank, customRankingField1);
				} else if(issue.getField(customRankingField2) != null) {
					updateRank(subtask.getKey(), parentRank, customRankingField2);
				}
			}
			response.setReasonPhrase("OK");

		}
		return response;
	}

	private static void updateRank(String key, int rank, String rankingField) {

		String url = baseURL + "/rest/api/2/issue/" + key;
		Client client = Client.create();
		WebResource webResource = client.resource(url);

		// client.addFilter(new
		// com.sun.jersey.api.client.filter.LoggingFilter());
		client.addFilter(new HTTPBasicAuthFilter(usr, pw));
		String jsonRequest = "{\n   \"fields\": { \n    \"" + rankingField + "\": "
				+ rank + " \n   } \n }";
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

	public static Set<Hashtable<String, String>> collect(String project,
			String sprint, String assignee) throws IOException,
			InterruptedException, ExecutionException {

		logger.info("Get projects in Jira");
		// First get all projects that the user knows about in JIRA
		Iterable<BasicProject> projects = JIRAGateway.getRestClient()
				.getProjectClient().getAllProjects().claim();
		for (BasicProject bp : projects) {
			logger.info("Project name=" + bp.getName());
		}
		Promise<SearchResult> searchResultPromise = null;

		Set<String> fields = new HashSet<>();
		fields.add("key");

		String jql = "fixVersion = \"" + sprint + "\" AND  assignee = "
				+ assignee + " ORDER BY key ASC";
		int maxResults = 1000;
		int startAt = 1;
		logger.info("Executing JQL = " + jql);
		logger.info("maxResults=" + maxResults + ", startAt=" + startAt);
		searchResultPromise = JIRAGateway.getRestClient().getSearchClient()
				.searchJql(jql, maxResults, startAt, null);

		SearchResult searchResult = searchResultPromise.get();

		if (searchResult != null) {
			Iterable<? extends Issue> issues = searchResult.getIssues();
			Set<Hashtable<String, String>> issuesForUser = getIssuesForUser(issues);
			JIRAGateway.closeRestClient();
			return issuesForUser;
		} else
			return null;

	}

	private static Set getIssuesForUser(Iterable<? extends Issue> issues) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		HashSet<Hashtable<String, String>> set = new HashSet<Hashtable<String, String>>();
		int issueCounter = 0;

		for (BasicIssue bi : issues) {
			logger.info("Mapping key=" + bi.getKey());
			Hashtable<String, String> kv = new Hashtable<String, String>();
			kv.put("key", bi.getKey());
			String key = bi.getKey();
			Issue issue = JIRAGateway.getRestClient().getIssueClient()
					.getIssue(key).claim();
			BasicUser bu = issue.getAssignee();
			kv.put("summary", issue.getSummary());
			if (bu != null) {
				kv.put("assignee", bu.getName());
			} else
				kv.put("assignee", "unassigned");

			BasicUser bur = issue.getReporter();
			if (bur != null) {
				kv.put("reporter", bur.getName());
			} else
				kv.put("reporter", "unknown");

			kv.put("status", issue.getStatus().getName());
			kv.put("creationDate", sdf.format(issue.getCreationDate().toDate()));
			if (issue.getDueDate() != null)
				kv.put("dueDate", sdf.format(issue.getDueDate().toDate()));
			String fixVersions = "";
			for (Version v : issue.getFixVersions()) {
				fixVersions += "{" + v.getName() + "},";
			}
			kv.put("fixVersions", fixVersions);
			kv.put("issueType", issue.getIssueType().getName());

			Set<String> labels = issue.getLabels();
			String labelString = "";
			if (labels.iterator().hasNext()) {
				labelString += "{" + labels.iterator().next() + "},";
			}
			kv.put("labels", labelString);
			if (issue.getTimeTracking() != null) {
				kv.put("originalEstimateMinutes", issue.getTimeTracking()
						.getOriginalEstimateMinutes() + "");
				kv.put("remainingEstimateMinutes", issue.getTimeTracking()
						.getRemainingEstimateMinutes() + "");
				kv.put("timeSpentMinutes", issue.getTimeTracking()
						.getTimeSpentMinutes() + "");
			}
			kv.put("updateDate", sdf.format(issue.getUpdateDate().toDate()));
			set.add(kv);
			issueCounter++;
		}
		logger.info("Iterated # of issues = " + issueCounter);
		return set;
	}

	private static void printClassPath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader) cl).getURLs();

		for (URL url : urls) {
			System.out.println(url.getFile());
		}
	}

}