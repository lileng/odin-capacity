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
package odin.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import odin.config.Configuration;
import odin.domain.Availability;
import odin.domain.Individual;
import odin.domain.Sprint;
import odin.gateway.SendMail;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class ReadGoogleSpreadsheet {
	protected static Logger logger = Logger
			.getLogger(ReadGoogleSpreadsheet.class);
	static String[] notificationList = Configuration.getDefaultValue(
			"odin.notify.prod.to").split(" ");

	public static void main(String[] args) throws Exception {
		List<Sprint> activeSprints = Sprint.getActiveSprints();

		for (Sprint sprint : activeSprints) {
			process(sprint.getSprintName());
		}
	}

	private static void process(String sprintName) throws MalformedURLException,
			IOException, ServiceException, GeneralSecurityException,
			ParseException, Exception {
		logger.info("Getting worksheet (same name as sprint): " + sprintName);

		StringBuffer returnBody = new StringBuffer();
		returnBody.append("<p>"+ReadGoogleSpreadsheet.class.getName() + " Completed");
		
		URL SPREADSHEET_FEED_URL = new URL(
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		ListFeed listFeedSpreadsheet = GoogleOAuthIntegration
				.getSpreadsheetService().getFeed(SPREADSHEET_FEED_URL,
						ListFeed.class);

		WorksheetEntry worksheet = getWorkSheet(sprintName);
		logger.info("Worksheet title: " + worksheet.getTitle().getPlainText());
		returnBody.append("<ul><li>Worksheet title: " + worksheet.getTitle().getPlainText() + "</li></ul>");

		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = GoogleOAuthIntegration.getSpreadsheetService().getFeed(listFeedUrl, ListFeed.class);

	//	StringBuffer sb = new StringBuffer();
		// Iterate through each row, printing its cell values.
		for (ListEntry row : listFeed.getEntries()) {
			String username = null;
			// Print the first column's cell value
			// Iterate over the remaining columns, and print each cell value
			for (String tag : row.getCustomElements().getTags()) {
				logger.info("CustomElement tag value: " + row.getCustomElements().getValue(tag));
				if (tag.equals("username"))
					username = row.getCustomElements().getValue(tag);
				if (tag.startsWith("week")) {
					// Get week number from Google spreadsheet tag
					String weekNumber = tag.substring(4);

					// Get last date of that week.
					SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.WEEK_OF_YEAR,
							new Integer(weekNumber).intValue());
					cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
					String wkEndDtS = sdf.format(cal.getTime());
					logger.info("wkEndDtS=" + wkEndDtS);

					// get hours
					int hours = 0;
					if (row.getCustomElements().getValue(tag) != null)
						hours = new Integer(row.getCustomElements().getValue(
								tag)).intValue();

					// Update availability in local database
					Availability.setAvailability(wkEndDtS, username, hours);
				}
			}
		}
		OdinResponse res = new OdinResponse();
		res.setStatusCode(0);
		res.setReasonPhrase("OK");
		res.setMessageBody(returnBody.toString());
		sendJobMessage(res);
	}

	static WorksheetEntry getWorkSheet(String sheetName) throws IOException,
			ServiceException, GeneralSecurityException {

		// Make a request to the API and get all spreadsheets.
		URL SPREADSHEET_FEED_URL = new URL(
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		SpreadsheetFeed feed = GoogleOAuthIntegration.getSpreadsheetService().getFeed(SPREADSHEET_FEED_URL,
				SpreadsheetFeed.class);
		List<com.google.gdata.data.spreadsheet.SpreadsheetEntry> spreadsheets = feed.getEntries();

		if (spreadsheets.size() == 0) {
			// TODO: There were no spreadsheets, act accordingly.
		}

		// TODO: Choose a spreadsheet more intelligently based on your
		// app's needs.
		com.google.gdata.data.spreadsheet.SpreadsheetEntry spreadsheet = spreadsheets.get(0);
		logger.info(spreadsheet.getTitle().getPlainText());

		// Make a request to the API to fetch information about all
		// worksheets in the spreadsheet.
		List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();

		// Iterate through each worksheet in the spreadsheet.
		for (WorksheetEntry worksheet : worksheets) {
			// Get the worksheet's title, row count, and column count.
			String title = worksheet.getTitle().getPlainText();
			int rowCount = worksheet.getRowCount();
			int colCount = worksheet.getColCount();
			if (title.equals(sheetName)) {
				return worksheet;
			}

			// Print the fetched information to the screen for this worksheet.
			logger.info("\t" + title + "- rows:" + rowCount + " cols: "
					+ colCount);
		}

		// Get the first worksheet of the first spreadsheet.
		// TODO: Choose a worksheet more intelligently based on your
		// app's needs.
		WorksheetFeed worksheetFeed = GoogleOAuthIntegration.getSpreadsheetService().getFeed(
				spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);

		WorksheetEntry worksheet = worksheets.get(0);

		// Get the worksheet's title, row count, and column count.
		String title = worksheet.getTitle().getPlainText();
		int rowCount = worksheet.getRowCount();
		int colCount = worksheet.getColCount();

		// Print the fetched information to the screen for this worksheet.
		logger.info("\tSpreadsheet info: " + title + "- rows:" + rowCount
				+ " cols: " + colCount);

		return worksheet;
	}
	
	private static void sendJobMessage(OdinResponse res) throws Exception {
		String header = "The Job " + ReadGoogleSpreadsheet.class.getName() + " Completed Normally";
		if (res.getStatusCode() != 0) {
			header = "The Job "+ ReadGoogleSpreadsheet.class.getName() + " Completed Abnormally";
		}
		try {
			SendMail.sendBacklogManagementMessage(notificationList, null,
					header, res.toString());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Unable to send email notification", e);
		}
	}

}