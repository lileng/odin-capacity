package odin.gateway;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import odin.config.Configuration;
import odin.util.AppConfig;
import odin.util.JEncrypt;

import org.apache.log4j.Logger;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;

/**
 * 
 * @author mol
 * 
 */
public class JiraHarvester {
	private static final String String = null;

	protected static Logger logger = Logger.getLogger(JiraHarvester.class);

	private static String url, usr, pw;

	public static void main(String args[]) {
		printClassPath();
		JiraHarvester harvester = new JiraHarvester();
		try {
			Object harvestedObjects = harvester.collect(null, "Sprint 2013.12",
					"mlileng");
			// JiraIssueHbaseGateway.insert(harvestedObjects);
		} catch (IOException e) {
			logger.fatal(e);
		}
	}

	public static int collectHoursRemainingWork(String project, String sprint,
			String assignee) throws IOException {
		int minutesRemainingWork = 0;
		int hoursRemainingWork = 0;
		Set<Hashtable<String, String>> collectedData = collect(project, sprint, assignee);

		for (Hashtable<String, String> keyValue : collectedData) {
			if (keyValue.get("remainingEstimateMinutes") != null && !keyValue.get("remainingEstimateMinutes").equals("null")) {		
				minutesRemainingWork = minutesRemainingWork + Integer.parseInt(keyValue
						.get("remainingEstimateMinutes"));
			}
		}
		if(minutesRemainingWork > 0){
			hoursRemainingWork = minutesRemainingWork / 60;
			logger.info("hoursRemainingWork=" + hoursRemainingWork);
		}
		return hoursRemainingWork;
	}
	
	public static Hashtable<String, String> getTasksWithRemainingWork(String project, String sprint,
			String assignee) throws IOException {
		Hashtable<String, String> kv = new Hashtable<String, String>();
		Set<Hashtable<String, String>> collectedData = collect(project, sprint, assignee);

		for (Hashtable<String, String> keyValue : collectedData) {
			if (keyValue.get("remainingEstimateMinutes") != null 
					&& !keyValue.get("remainingEstimateMinutes").equals("null")
					&& !keyValue.get("remainingEstimateMinutes").equals("0")) {
				kv.put(keyValue.get("key"), keyValue.get("summary"));
			}
		}
	
		return kv;
	}

	public static Set<Hashtable<String, String>> collect(String project,
			String sprint, String assignee) throws IOException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final NullProgressMonitor pm = new NullProgressMonitor();
		
		logger.info("Get projects in Jira");
		// First get all projects that the user knows about in JIRA
		Iterable<BasicProject> projects = getRestClient().getProjectClient()
				.getAllProjects(pm);
		for (BasicProject bp : projects) {
			logger.info("Project name=" + bp.getName());
		}
		SearchResult searchResult = null;

		// Queries:
		// project = ONE ORDER BY key ASC
		// project = ONE and updatedDate > startOfDay() ORDER BY key ASC
		String jql = "fixVersion = \"" + sprint + "\" AND  assignee = "
				+ assignee + " ORDER BY key ASC";
		int maxResults = 1000;
		int startAt = 1;
		logger.info("Executing JQL = " + jql);
		logger.info("maxResults=" + maxResults + ", startAt=" + startAt);
		searchResult = getRestClient().getSearchClient().searchJql(jql,
				maxResults, startAt, pm);
		Iterable<? extends BasicIssue> issues = searchResult.getIssues();

		HashSet<Hashtable<String, String>> set = new HashSet<Hashtable<String, String>>();
		int issueCounter = 0;

		for (BasicIssue bi : issues) {
			logger.info("Mapping key=" + bi.getKey());
			Hashtable<String, String> kv = new Hashtable<String, String>();
			kv.put("key", bi.getKey());
			String key = bi.getKey();
			Issue issue = getRestClient().getIssueClient().getIssue(key, pm);
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

	private static JiraRestClient getRestClient() {
		logger.debug("getRestClient");
		final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		JiraRestClient restClient = null;
		URI jiraServerUri;
		try {
			jiraServerUri = new URI(Configuration.getDefaultValue("gateway.jira.url"));
			String usr = new String(JEncrypt.decode(Configuration.getDefaultValue("gateway.jira.username").getBytes()));
			logger.info("url=" + Configuration.getDefaultValue("gateway.jira.url") );
			
			String pw = new String(JEncrypt.decode(Configuration.getDefaultValue("gateway.jira.password").getBytes()));
		//	logger.debug("pw=" + pw);
			
			restClient = factory.createWithBasicHttpAuthentication(
					jiraServerUri, usr,pw);
			logger.debug("restClient ready");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return restClient;
	}

}