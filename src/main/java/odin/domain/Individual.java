package odin.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Persistence;
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
				"select i from Individual i WHERE i.userID=:userName", Individual.class);
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