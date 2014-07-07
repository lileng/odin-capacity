import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import odin.domain.Individual;
import odin.domain.Sprint;

import org.apache.log4j.Logger;

import demand.JiraHarvester;

/**
 * <ol>
 * <li>Looks up an active sprint and participants of the sprint. 
 * <li>Queries JIRA for tasks in this sprint for a specific 
 * individual. 
 * <li>Compares hours of capacity for an individual with the 
 * remaining hours estimated for the tasks assigned to this individual 
 * for this specific sprint.
 * <li>Sends email to the individual if remaining hours > reported capacity 
 * for the remaining time.
 * </ol>
 * @author mlileng
 *
 */
public class CapacityDriver {
	protected static Logger logger = Logger.getLogger(CapacityDriver.class);

	public static void main(String[] args) throws IOException {
		logger.info("Starting CapacityDriver");
		printEnvMap();
		printClassPath();
		process();
		logger.info("Stopping CapacityDriver");
	}

	private static void process() throws IOException {
		logger.info("process");
		List<Sprint> activeSprints = Sprint.getActiveSprints();
		List<Individual> activeIndividuals = null;
		
		for (Sprint sprint : activeSprints) {
			activeIndividuals = Sprint.getActiveParticipantsNotContactedToday(sprint.getSprintName());
			for(Individual i: activeIndividuals){			
				processIndividual(i.getUserID(), sprint.getSprintName(), i.getEmailAddress(), i.getFirstName());
			}
		}

	}

	public static void processIndividual(String username, String sprint,
			String emailAddress, String name) throws IOException {
		int hoursRemainingCapacity = Sprint.getRemainingAvailability(sprint,
				username);
		int minutesRemainingWork = 0;;
		int hoursRemainingWork = 0;
		StringBuffer sb = new StringBuffer();
		
		Set<Hashtable<String, String>> collectedData = JiraHarvester.collect(null,sprint, username);
		
		for (Hashtable<String, String> keyValue : collectedData) {
			if (keyValue.get("remainingEstimateMinutes") != null && !keyValue.get("remainingEstimateMinutes").equals("null")) {		
				minutesRemainingWork = minutesRemainingWork + Integer.parseInt(keyValue
						.get("remainingEstimateMinutes"));
				if (!keyValue.get("remainingEstimateMinutes").equals("0")) {
					sb.append("<li>[");
					sb.append(keyValue.get("key"));
					sb.append("] - ");
					sb.append(keyValue.get("summary"));
				}
			}
		}
		
		if(minutesRemainingWork > 0){
			hoursRemainingWork = minutesRemainingWork / 60;
			logger.info("hoursRemainingWork=" + hoursRemainingWork);
		}
			
		if (hoursRemainingCapacity < hoursRemainingWork) {
			// Send mail that you're in trouble
			logger.info("HoursRemainingCapacity < hoursRemainingWork");

			SendMail.sendMessage(
					emailAddress, 
					"Odin Capacity Status",
					"<h1>Odin Capacity Status</h1>"
							+ "<p>Hi "
							+ name
							+ ". It looks like you may have more tasks than available hours left in the current sprint."
							+ "<ul>"
							+ "<li>Your estimated remaining capacity in hours: "
							+ hoursRemainingCapacity
							+ "<li>Hours for your estimated tasks: "
							+ hoursRemainingWork
							+ "</ul>"
							+ "<p>The tasks with hours still remaining are the following:"
							+ "<ul>" + sb.toString() + "</ul>"
							+ "<p>Thank you,<br>"
							+ "--agile odin (odin@lileng.com)");
			Individual.recordUserContactedNow(username);

		}
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
