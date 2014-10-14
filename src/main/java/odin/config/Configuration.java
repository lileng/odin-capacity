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
package odin.config;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import odin.util.DBUtil;
import odin.util.JEncrypt; 

@Entity
public class Configuration {
	@Id
	@GeneratedValue
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private String configInstance;
	private String configKey;
	private String configValue;
	private String configDefaultValue;

	public String getConfigDefaultValue() {
		return configDefaultValue;
	}

	public void setConfigDefaultValue(String configDefaultValue) {
		this.configDefaultValue = configDefaultValue;
	}

	public String getConfigDescription() {
		return configDescription;
	}

	public void setConfigDescription(String configDescription) {
		this.configDescription = configDescription;
	}

	private String configDescription;

	public String getConfigInstance() {
		return configInstance;
	}

	public void setConfigInstance(String configInstance) {
		this.configInstance = configInstance;
	}

	public String getConfigKey() {
		return configKey;
	}

	public void setConfigKey(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigValue() {
		return configValue;
	}

	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}

	@Override
	public String toString() {
		return "Configuration [instance=" + getConfigInstance() + ", key="
				+ getConfigKey() + ", value=" + getConfigValue() + "]";
	}

	public static String getValue(String instance, String key) {
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Configuration> q = em
				.createQuery(
						"select c from Configuration c WHERE c.configInstance=:instance AND c.configKey=:key",
						Configuration.class);
		q.setParameter("instance", instance);
		q.setParameter("key", key);
		Configuration config = q.getSingleResult();
		String returnValue = config.getConfigValue();
		em.close();
		return returnValue;
	}

	public static String getDefaultValue(String key) {
		String returnValue = null;
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Configuration> q = em
				.createQuery(
						"select c from Configuration c WHERE c.configInstance=:instance AND c.configKey=:key",
						Configuration.class);
		q.setParameter("instance", "default");
		q.setParameter("key", key);
		try {
			Configuration config = q.getSingleResult();
			returnValue = config.getConfigValue();
			em.close();
		} catch (NoResultException nre) {
			// just return null
		}

		return returnValue;
	}

	public static void setValue(String instance, String key, String value,
			String description) {
		EntityManager em = DBUtil.getEntityManager();
		Configuration conf = new Configuration();

		em.getTransaction().begin();

		conf.setConfigInstance(instance);
		conf.setConfigKey(key);
		conf.setConfigValue(value);
		conf.setConfigDefaultValue(value);
		conf.setConfigDescription(description);

		em.persist(conf);
		em.getTransaction().commit();
		em.close();
	}

	public static void main(String[] args) {
		//Configuration.setValue("default", "gateway.sendmail.password",
		//		"$lappfi$k", "Password used on the email server.");
		String usr = new String(JEncrypt.decode(Configuration
				.getDefaultValue("gateway.jira.username").getBytes()));
		String pwd = new String(JEncrypt.decode(Configuration
				.getDefaultValue("gateway.jira.password").getBytes()));
		System.out.println("usr=" + usr);
		System.out.println("pwd=" + pwd);
	}

}
