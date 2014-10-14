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
import java.util.Properties;

import odin.gateway.JiraHarvester;


public class AppConfig {
	static Properties defaultProps;

	public static Properties getAppConfig() throws IOException {
		if (defaultProps == null) {
			String propsFile = getPropsFileName();
			defaultProps = new Properties();
			defaultProps.load(JiraHarvester.class.getClassLoader()
					.getResourceAsStream(propsFile));
		}
		return defaultProps;

	}

	private static String getPropsFileName() {
		String propsFile = "";
		String env = System.getenv("ODIN_ENV");
		if(env != null && env.equals("DEV"))
			propsFile = "app.DEV.properties";
		else
			propsFile = "app.properties";
		return propsFile;
	}

}
