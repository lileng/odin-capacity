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
package odin.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import odin.util.DBUtil;

@Entity
public class Individual {

	@OneToMany(mappedBy = "individual")
	private List<Availability> availability;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String firstName;

	private String lastName;

	private String fullName;

	private String userID;

	private String timeZone;

	private String emailAddress;

	@Temporal(TemporalType.DATE)
	private Date contacted;

	public Date getContacted() {
		return contacted;
	}

	public void setContacted(Date contacted) {
		this.contacted = contacted;
	}

	public static void recordUserContactedNow(String userName) {
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Individual> q = em.createQuery(
				"select i from Individual i WHERE i.userID=:userName",
				Individual.class);
		q.setParameter("userName", userName);
		Individual individual = q.getSingleResult();
		em.getTransaction().begin();
		individual.setContacted(new Date());
		em.persist(individual);
		em.getTransaction().commit();
		em.close();
	}

	@Override
	public String toString() {
		return "Individual [firstName=" + firstName + ", lastName=" + lastName
				+ "]";
	}
}