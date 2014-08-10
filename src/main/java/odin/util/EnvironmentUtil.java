package odin.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class EnvironmentUtil {
	public static void printClassPath() {
		System.out.println("========================");
		System.out.println("     printClassPath");
		System.out.println("========================");
		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader) cl).getURLs();

		for (URL url : urls) {
			System.out.println(url.getFile());
		}
		System.out.println("----------------------");
		System.out.println("");
		System.out.println("");
		System.out.println("");
	}

	public static void printEnvMap() {
		System.out.println("========================");
		System.out.println("     printEnvMap");
		System.out.println("========================");
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			System.out.format("%s=%s%n", envName, env.get(envName));
		}
		System.out.println("----------------------");
		System.out.println("");
		System.out.println("");
		System.out.println("");
	}

}
