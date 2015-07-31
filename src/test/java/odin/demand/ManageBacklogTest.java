package odin.demand;

import static org.junit.Assert.*;
import odin.util.OdinResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ManageBacklogTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testUpdateStatus(){
	//	ManageBacklog.updateStatus("ONE-3290");
		OdinResponse res = new OdinResponse();
		ManageBacklog.ensureTasksInProductBacklogNewStatus(res);
		
	}

}
