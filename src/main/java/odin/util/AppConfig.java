package odin.util;

import java.io.IOException;
import java.util.Properties;

import odin.gateway.JiraHarvester;


public class AppConfig {
	static Properties defaultProps;

	public static Properties getAppConfig() throws IOException {
		if (defaultProps == null) {
			String propsFile = getPropsFileName();
			defaultProps = new Properties();
			defaultProps.load(JiraHarvester.class.getClassLoader()
					.getResourceAsStream(propsFile));
		}
		return defaultProps;

	}

	private static String getPropsFileName() {
		String propsFile = "";
		String env = System.getenv("ODIN_ENV");
		if(env != null && env.equals("DEV"))
			propsFile = "app.DEV.properties";
		else
			propsFile = "app.properties";
		return propsFile;
	}

}
