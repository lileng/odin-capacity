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

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

public class DBUtil {
	private static final String PERSISTENCE_UNIT_NAME = "individuals";
	private static EntityManagerFactory factory;

	protected static Logger log = Logger.getLogger("DBUtil");

	public static EntityManager getEntityManager() {
		if (factory == null) {
			log.info("Setup new DB connection");
			Properties props = new Properties();
			String env = System.getenv("ODIN_ENV");
			factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		} else {
			log.debug("Using existing DB connection");
		}
		return factory.createEntityManager();
	}
}
