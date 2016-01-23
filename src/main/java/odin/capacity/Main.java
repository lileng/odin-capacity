package odin.capacity;

import org.apache.log4j.Logger;

import odin.demand.ManageBacklog;
import odin.gateway.ReportAllocation;
import odin.util.ReadGoogleSpreadsheet;

/**
 * Main starting point for Odin Capacity. Command line args kicking off 
 * the different tasks.
 *
 */
public class Main {
	protected static Logger LOG = Logger
			.getLogger(Main.class);

	public static void main(String[] args) {
		if(args.length == 0){
			LOG.error("Need correct argument to start application");
			System.exit(-1);
		}
		if(args[0].equals("ReadGoogleSpreadsheet")){
			LOG.info("Starting ReadGoogleSpreadsheet");
			try {
				ReadGoogleSpreadsheet.main(args);
			} catch (Exception e) {
				LOG.error("Error in ReadGoogleSpreadsheet: " + e);
				e.printStackTrace();
			}
			LOG.info("Stopping ReadGoogleSpreadsheet");
		} else if(args[0].equals("ManageBacklog")){
			LOG.info("Starting ManageBacklog");
			try {
				ManageBacklog.main(args);
			} catch (Exception e) {
				LOG.error("Error in ManageBacklog: " + e);
				e.printStackTrace();
			}
			LOG.info("Stopping ManageBacklog");
		} else if(args[0].equals("ReportAllocation")){
			LOG.info("Starting ReportAllocation");
			try {
				ReportAllocation.main(args);
			} catch (Exception e) {
				LOG.error("Error in ReportAllocation: " + e);
				e.printStackTrace();
			}
			LOG.info("Stopping ReportAllocation");
		}
		else
			LOG.warn("No work done. Could not understand input variables.");
	}

}
