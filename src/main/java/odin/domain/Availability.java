package odin.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import odin.util.DBUtil;

@Entity
@IdClass(AvailabilityId.class)
public class Availability {
	@Id
	@ManyToOne
	@JoinColumn(name = "individual_id")
	private Individual individual;

	@Id
	@ManyToOne
	@JoinColumn(columnDefinition = "integer", name = "week_id")
	private Week week;

	private int hours;

	final static long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;


	protected static Logger log = Logger.getLogger("Availability");

	public static void main(String[] args) throws Exception {
		// Prepare to set/get availability for
		// the week with this end date.
		String endDateS = "01/03/2014";
		String userName = "wsmytherin";
		int hours = 28;
		setAvailability(endDateS, userName, hours);
		
	}

	public static void setAvailability(String endDateS, String userName,
			int hours) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
		Date endDate = sdf.parse(endDateS);

		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(endDate);

		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

		setAvailability(cal, userName, hours);
		log.info("Availability for [" + userName + "] is: "
				+ getAvailability(cal, userName));
	}

	public Individual getIndividual() {
		return individual;
	}

	public void setIndividual(Individual individual) {
		this.individual = individual;
	}

	public Week getWeek() {
		return week;
	}

	public void setWeek(Week week) {
		this.week = week;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	@Override
	public String toString() {
		return "Availability [individual=" + individual + ", week=" + week
				+ ", hours=" + hours + "]";
	}

	private static void setAvailability(Calendar weekEnding, String userId,
			int hours) {
		EntityManager em = DBUtil.getEntityManager();

		// Getting Week object

		TypedQuery<Week> q = em.createQuery(
				"select w from Week w WHERE w.endDate=:arg1", Week.class);
		q.setParameter("arg1", weekEnding.getTime());
		Week week = q.getSingleResult();
		log.info(week.toString());

		// Getting Individual
		TypedQuery<Individual> q2 = em.createQuery(
				"select i from Individual i WHERE i.userID=:uid",
				Individual.class);
		q2.setParameter("uid", userId);
		Individual individual = q2.getSingleResult();
		log.info(individual.toString());

		TypedQuery<Availability> q3 = em
				.createQuery(
						"select a from Availability a WHERE a.individual=:individual AND a.week=:week",
						Availability.class);
		q3.setParameter("individual", individual);
		q3.setParameter("week", week);
		Availability availability = null;
		try {
			availability = q3.getSingleResult();
		} catch (NoResultException nre) {
			// no worries
		}

		if (availability != null) {
			log.info(availability.toString());
			em.getTransaction().begin();
			availability.setHours(hours);
			em.merge(availability);
			em.getTransaction().commit();
		} else {

			Availability a = new Availability();
			a.setIndividual(individual);
			a.setWeek(week);
			a.setHours(hours);

			em.getTransaction().begin();
			em.persist(a);
			em.getTransaction().commit();
		}

		em.close();

	}
	
	public static int getAvailability(String endDateS, String userId) {
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");
		Date endDate = null;
		try {
			endDate = sdf.parse(endDateS);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(endDate);

		cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return getAvailability(cal, userId);
	}

	public static int getAvailability(Calendar weekEnding, String userId) {
		EntityManager em = DBUtil.getEntityManager();

		// Getting Week object
		TypedQuery<Week> q = em.createQuery(
				"select w from Week w WHERE w.endDate=:arg1", Week.class);
		q.setParameter("arg1", weekEnding.getTime());
		Week week = q.getSingleResult();
		log.info(week.toString());

		// Getting Individual
		TypedQuery<Individual> q2 = em.createQuery(
				"select i from Individual i WHERE i.userID=:uid",
				Individual.class);
		q2.setParameter("uid", userId);
		Individual individual = null;
		try {
			individual = q2.getSingleResult();
			log.info(individual.toString());
		} catch (NoResultException nre) {
			log.warning("No individual found with userId: " + userId);
			return 0;
		}
		if (individual != null) {
			TypedQuery<Availability> q3 = em
					.createQuery(
							"select a from Availability a WHERE a.individual=:individual AND a.week=:week",
							Availability.class);
			q3.setParameter("individual", individual);
			q3.setParameter("week", week);
			Availability availability = null;
			try {
				availability = q3.getSingleResult();
			} catch (NoResultException nre) {
				log.warning("No availability found with userId: "
						+ individual.toString() + ", and week: "
						+ week.toString());
				return 0;
			}

			log.info(availability.toString());
			individual.setContacted(new Date());
			em.close();
			return availability.getHours();
		} else {
			return 0;
		}

	}

}
