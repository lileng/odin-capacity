package odin.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class AvailabilityTest {

	@Test
	public void testGetAvailabilityStringString() {
		String endDateS = "12/19/2015"; // M/d/yy
		String userId = "mlileng";
		int i = Availability.getAvailability(endDateS, userId);
		System.out.println("Availability for '" + userId + "': " + i);
		//fail("Not yet implemented");
	}
}
