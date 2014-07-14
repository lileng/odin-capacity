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
//			if(env != null && env.equals("DEV"))
//				props.put("eclipselink.persistencexml", "odin-persistence.DEV.xml");
//			else
//				props.put("eclipselink.persistencexml", "odin-persistence.xml");
			factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		} else {
			log.info("Using existing DB connection");
		}
		return factory.createEntityManager();
	}
}
