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
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.GoogleService;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import odin.config.Configuration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Adding Oauth2 support to read the Odin capacity spreadsheet in Odin.
 * 
 * 
 */
public class GoogleOAuthIntegration {
	protected static Logger LOG = Logger
			.getLogger(GoogleOAuthIntegration.class);

	    /** 
	     * Returns a spreasheetService object.
	     * @return SpreadsheetService
	     * @throws MalformedURLException
	     * @throws GeneralSecurityException
	     * @throws IOException
	     * @throws ServiceException
	     */
		public static SpreadsheetService getSpreadsheetService() throws MalformedURLException,
				GeneralSecurityException, IOException, ServiceException {
			SpreadsheetEntry spreadsheet = null;
			URL SPREADSHEET_FEED_URL;
	        SPREADSHEET_FEED_URL = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");

	        File p12 = new File(Configuration.getDefaultValue("google.spreadsheets.pk_location"));

	        HttpTransport httpTransport = new NetHttpTransport();
	        JacksonFactory jsonFactory = new JacksonFactory();
	        String[] SCOPESArray = {"https://spreadsheets.google.com/feeds", "https://spreadsheets.google.com/feeds/spreadsheets/private/full", "https://docs.google.com/feeds"};
	        final List SCOPES = Arrays.asList(SCOPESArray);
	        GoogleCredential credential = new GoogleCredential.Builder()
	                .setTransport(httpTransport)
	                .setJsonFactory(jsonFactory)
	                .setServiceAccountId(Configuration.getDefaultValue("google.spreadsheets.svc_account_id"))
	                .setServiceAccountScopes(SCOPES)
	                .setServiceAccountPrivateKeyFromP12File(p12)
	                .build();

	        SpreadsheetService service = new SpreadsheetService(Configuration.getDefaultValue("google.spreadsheets.app_name"));

	        service.setOAuth2Credentials(credential);
	        SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
	        List<SpreadsheetEntry> spreadsheets = feed.getEntries();
	        

	        if (spreadsheets.size() == 0) {
	        	LOG.info("No spreadsheets found.");
	        }

	        for (int i = 0; i < spreadsheets.size(); i++) {
	            if (spreadsheets.get(i).getTitle().getPlainText().startsWith("Odin")) {
	                spreadsheet = spreadsheets.get(i);
	                LOG.info("Spreadsheet starting with 'Odin' found to return (last found): " + spreadsheet.getTitle().getPlainText());
	            }
	        }
	        return service;
		}

	}