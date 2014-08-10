package odin.config;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.TypedQuery;

import odin.util.DBUtil;

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
		return "Configuration [instance=" + getConfigInstance() + ", key=" + getConfigKey()
				+ ", value=" + getConfigValue() + "]";
	}
	
	public static String getValue(String instance ,String key) {
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Configuration> q = em.createQuery(
				"select c from Configuration c WHERE c.configInstance=:instance AND c.configKey=:key", Configuration.class);
		q.setParameter("instance", instance);
		q.setParameter("key", key);
		Configuration config = q.getSingleResult();
		String returnValue = config.getConfigValue();
		em.close();
		return returnValue;
	}
	public static String getDefaultValue(String key) {
		EntityManager em = DBUtil.getEntityManager();

		TypedQuery<Configuration> q = em.createQuery(
				"select c from Configuration c WHERE c.configInstance=:instance AND c.configKey=:key", Configuration.class);
		q.setParameter("instance", "default");
		q.setParameter("key", key);
		Configuration config = q.getSingleResult();
		String returnValue = config.getConfigValue();
		em.close();
		return returnValue;
	}
	
	
	public static void setValue(String instance ,String key, String value, String description) {
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
	
	public static void main(String[] args){
		Configuration.setValue("default", "gateway.sendmail.password", "$lappfi$k", "Password used on the email server.");
	}


}
