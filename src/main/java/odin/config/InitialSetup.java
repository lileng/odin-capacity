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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import odin.domain.Sprint;
import odin.domain.Week;

/**
 * Sets up weeks and sprints.
 *
 */
public class InitialSetup {
	private static EntityManagerFactory factory;
	private static final String PERSISTENCE_UNIT_NAME = "individuals";
	protected static Logger log = Logger.getLogger("Configuration");

	public static void main(String[] args) throws Exception {
		setupSprint();

	}

	public static void setupWeeks() throws Exception {

		int startWeek;
		int finishWeek;
		int diff;
		SimpleDateFormat sdf;
		Calendar calStart;
		Calendar calEnd;
		Date startDate;
		Date finishDate;
		String startDateS = "01/01/2014";
		String finishDateS = "31/12/2014";

		sdf = new SimpleDateFormat("dd/MM/yyyy");

		startDate = sdf.parse(startDateS);
		finishDate = sdf.parse(finishDateS);

		calStart = Calendar.getInstance();
		calEnd = Calendar.getInstance();

		calStart.setTime(startDate);
		startWeek = calStart.get(Calendar.WEEK_OF_YEAR);

		calEnd.setTime(finishDate);
		finishWeek = calEnd.get(Calendar.WEEK_OF_YEAR);
		if (finishWeek == 1)
			finishWeek = 53;

		diff = finishWeek - startWeek;
		Calendar cal = Calendar.getInstance(Locale.US);

		// Setting up DB connection
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = factory.createEntityManager();

		Week week = null;

		for (int i = 0; i < diff; i++) {
			if (i == 0)
				cal.setTime(startDate);
			else
				cal.add(Calendar.WEEK_OF_YEAR, 1);

			week = new Week();

			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			log.info("WEEK " + cal.get(Calendar.WEEK_OF_YEAR) + " start: "
					+ sdf.format(cal.getTime()));
			week.setStartDate(cal.getTime());

			cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			log.info("WEEK " + cal.get(Calendar.WEEK_OF_YEAR) + " end  : "
					+ sdf.format(cal.getTime()));
			week.setEndDate(cal.getTime());
			week.setName(cal.get(Calendar.YEAR) + "_"
					+ cal.get(Calendar.WEEK_OF_YEAR));

			em.getTransaction().begin();
			em.persist(week);
			em.getTransaction().commit();

			week = null;
		}
		em.close();
	}

	public static void setupSprint() throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		// Setting up DB connection
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		// Getting Week object
		TypedQuery<Week> q = em.createQuery(
				"select w from Week w WHERE w.name=:name", Week.class);
		q.setParameter("name", "2014_9");
		Week week = q.getSingleResult();
		log.info(week.toString());

		TypedQuery<Sprint> qs = em.createQuery(
				"select s from Sprint s WHERE s.sprintName=:sname",
				Sprint.class);
		qs.setParameter("sname", "Sprint 2014.02");
		Sprint sprint = null;
		try {
			sprint = qs.getSingleResult();
		} catch (NoResultException nre) {
			log.warning("Did not find any sprint. Creating a new one...");
			sprint = new Sprint();
			sprint.setSprintName("Sprint 2014.02");
			sprint.setStartDate(sdf.parse("01/02/2014"));
			sprint.setEndDate(sdf.parse("28/02/2014"));

			List<Week> weeks = new ArrayList<Week>();
			weeks.add(week);
			sprint.setWeeks(weeks);
		}

		week.setSprint(sprint);

		em.persist(sprint);
		em.persist(week);
		em.getTransaction().commit();

		em.close();
	}

}
