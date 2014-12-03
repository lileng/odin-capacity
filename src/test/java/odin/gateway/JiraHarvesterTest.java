package odin.gateway;

import javax.ws.rs.core.MediaType;

import odin.util.OdinResponse;

import org.junit.Test;
import static org.junit.Assert.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class JiraHarvesterTest {

	@Test
	public void testUpdateSubtaskRanking() {
		
		OdinResponse response = JiraHarvester.updateSubtaskRanking();
		System.out.println(response);
		assertTrue(response.toString(), response.getStatusCode() == 0);
		assertFalse(response.toString(), response.getStatusCode() == 500);
		//JiraHarvesterTest.restfulService();
	}

	private static void restfulService() {
		Client client = Client.create();
		WebResource webResource = client
				.resource("https://xxxxx/rest/api/2/issue/xxxxx");

		client.addFilter(new com.sun.jersey.api.client.filter.LoggingFilter());
		client.addFilter(new HTTPBasicAuthFilter("xxxx", "xxxxx"));
		String jsonRequest = "{\n   \"fields\": { \n    \"customfield_xxxx\": 2901 \n   } \n }";
		System.out.println(jsonRequest);
				
		
		ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).header("Content-Type", "application/json")
				.put(ClientResponse.class, jsonRequest);

		String output = clientResponse.getEntity(String.class);
		if (clientResponse.getStatus() != 200) {
			System.out.println("Output from Server .... \n");
			System.out.println(output);
			throw new RuntimeException("Failed : HTTP error code : "
					+ clientResponse.getStatus());
		}

		

		System.out.println("Output from Server .... \n");
		System.out.println(output);

	}
}
