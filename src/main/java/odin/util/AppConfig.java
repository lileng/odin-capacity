package odin.util;

import java.io.IOException;
import java.util.Properties;

import demand.JiraHarvester;

public class AppConfig {
	static Properties defaultProps;

	public static Properties getAppConfig() throws IOException {
		if (defaultProps == null) {
			defaultProps = new Properties();
			defaultProps.load(JiraHarvester.class.getClassLoader()
					.getResourceAsStream("app.properties"));
		}
		return defaultProps;

	}

}
