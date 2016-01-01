package odin.domain;

import org.junit.Test;

public class AvailabilityTest {

	@Test
	public void testGetAvailability() {
		String endDateS = "12/19/2015"; // M/d/yy
		String userId = "mlileng";
		int i = Availability.getAvailability(endDateS, userId);
		System.out.println("Availability for '" + userId + "': " + i);
		//fail("Not yet implemented");
	}
	
	@Test
	public void testGetNullAvailability() {
		String endDateS = "1/19/2016"; // M/d/yy
		String userId = "mlileng";
		int i = Availability.getAvailability(endDateS, userId);
		System.out.println("Availability for '" + userId + "': " + i);
	}
}
