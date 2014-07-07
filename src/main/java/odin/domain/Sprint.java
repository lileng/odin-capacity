package odin.domain;

import java.text.SimpleDateFormat;
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

import org.apache.log4j.Logger;
import org.joda.time.Days;
import org.joda.time.LocalDate;

@Entity
public class Sprint {
	protected static Logger log = Logger.getLogger("Sprint");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String sourceQuery;

	private Boolean active;

	@Temporal(TemporalType.DATE)
	private Date startDate;

	@Temporal(TemporalType.DATE)
	private Date endDate;

	private String teamName;

	private String teamEmailAddress;

	private String projectName;

	private String oldETC;

	private String newETC;

	private Integer sumOriginalEstimateMinutes;

	private String sprintName;

	private String description;

	@OneToMany(mappedBy = "sprint")
	private List<Week> weeks;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSourceQuery() {
		return sourceQuery;
	}

	public void setSourceQuery(String sourceQuery) {
		this.sourceQuery = sourceQuery;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getTeamEmailAddress() {
		return teamEmailAddress;
	}

	public void setTeamEmailAddress(String teamEmailAddress) {
		this.teamEmailAddress = teamEmailAddress;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getOldETC() {
		return oldETC;
	}

	public void setOldETC(String oldETC) {
		this.oldETC = oldETC;
	}

	public String getNewETC() {
		return newETC;
	}

	public void setNewETC(String newETC) {
		this.newETC = newETC;
	}

	public Integer getSumOriginalEstimateMinutes() {
		return sumOriginalEstimateMinutes;
	}

	public void setSumOriginalEstimateMinutes(Integer sumOriginalEstimateMinutes) {
		this.sumOriginalEstimateMinutes = sumOriginalEstimateMinutes;
	}

	public String getSprintName() {
		return sprintName;
	}

	public void setSprintName(String sprintName) {
		this.sprintName = sprintName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return "Sprint [name=" + sprintName + ", startDate="
				+ sdf.format(startDate) + ", endDate=" + sdf.format(endDate)
				+ "]";
	}

	public List<Week> getWeeks() {
		return weeks;
	}

	public void setWeeks(List<Week> weeks) {
		this.weeks = weeks;
	}

	public static int getRemainingAvailability(String sprintString,
			String username) {
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Sprint> q = em.createQuery(
				"select s from Sprint s WHERE s.sprintName = :sprintString",
				Sprint.class).setParameter("sprintString", sprintString);
		Sprint sprint = q.getSingleResult();
		// em.detach(sprint);
		LocalDate today = new LocalDate();
		int availability = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy");

		for (Week week : sprint.getWeeks()) {
			LocalDate weekEndDate = LocalDate.fromDateFields(week.getEndDate());
			int days = Days.daysBetween(today, weekEndDate).getDays();
			System.out.println(days);
			if (days >= 0) {
				if (days < 6) {
					// Calculate hours left on a part week.
					int remainingHours = Availability.getAvailability(
							sdf.format(weekEndDate.toDate()), username)
					/ 5 * days;
					availability = availability + remainingHours;
					System.out.println("Part week. Calculated availability for week ending " + sdf.format(week.getEndDate()) + " is " + remainingHours);
				} else
					availability = availability
							+ Availability.getAvailability(
									sdf.format(weekEndDate.toDate()), username);
			}
		}

		em.close();
		return availability;
	}

	public static List<Individual> getActiveParticipantsNotContactedToday(String sprintName) {
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Individual> q = em.createQuery(
				"select i from Individual i WHERE i.contacted = NULL OR i.contacted != :today",
				Individual.class);
		q.setParameter("today", new Date());
		List<Individual> individuals = q.getResultList();
	//	em.detach(individuals);
		em.close();
		return individuals;

	}
	
	public static List<Sprint> getActiveSprints() {
		EntityManager em = DBUtil.getEntityManager();

		log.info("Getting active sprints");
		TypedQuery<Sprint> q = em.createQuery("select s from Sprint s WHERE s.active=:status",
				Sprint.class);
		q.setParameter("status", true);
		List<Sprint> sprints = q.getResultList();
	//	em.detach(sprints);
		em.close();
		return sprints;

	}	


}
