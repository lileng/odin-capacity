package odin.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import odin.util.DBUtil;

@Entity
public class Observation {
	
	public static void recordObservation(String userName, String type, String description, String key1, int value1, String key2, int value2){
		EntityManager em = DBUtil.getEntityManager();
		Observation observation = new Observation();

		em.getTransaction().begin();
		
		observation.setUsername(userName);
		observation.setType(type);
		observation.setRegisteredDate(new Date());
		observation.setDescription(description);
		observation.setKey1(key1);
		observation.setValue1(value1);
		observation.setKey2(key2);
		observation.setValue2(value2);
		em.persist(observation);
		em.getTransaction().commit();
		em.close();
	}

	@Id
	@GeneratedValue
	private Long id;
	
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Temporal(TemporalType.DATE)
	private Date registeredDate;
	
	private String type;
	private String description;

	private String key1;
	private int value1;
	
	private String key2;
	private int value2;
	

	public String getKey1() {
		return key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public int getValue1() {
		return value1;
	}

	public void setValue1(int value1) {
		this.value1 = value1;
	}

	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public int getValue2() {
		return value2;
	}

	public void setValue2(int value2) {
		this.value2 = value2;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getRegisteredDate() {
		return registeredDate;
	}

	public void setRegisteredDate(Date registeredDate) {
		this.registeredDate = registeredDate;
	}
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return "Week [registeredDate=" + sdf.format(registeredDate) + ", type="
				+ type + ", description=" + description + "]";
	}

}
