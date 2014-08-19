/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package supply;


import java.text.SimpleDateFormat;
import java.util.Locale;

import odin.domain.Availability;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;

public class CapacityHarvester {
	protected static Logger logger = Logger.getLogger(CapacityHarvester.class);
	
	public static void main(String args[]) {
		logger.info(getCalculatedRemainingHours("bob"));
	}
	
	public static int getCalculatedRemainingHours(String username){
		// Find start and end date for current sprint
		// --> Lookup sprint setup
		
		// Business days To Sprint End
		DateTime sprintStartDate = new DateTime(2014, 02, 1, 0, 0, 0, 0 );
		DateTime sprintEndDate = new DateTime(2014, 02, 28, 17, 0);
		logger.info("sprintEndDate WeekOfWeekyear="+sprintEndDate.getWeekOfWeekyear());
		logger.info("sprintEndDate WeekOfWeekyear="+sprintEndDate.getWeekOfWeekyear());
		LocalDate today = new LocalDate();
		
		// business days left in current week
		logger.info("Current week="+today.getWeekOfWeekyear());
		if(today.getDayOfWeek()>5){
			logger.info("Not a business day. 0 hours left of availability as this is weekend.");
		}
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy"); 
		Period weekPeriod = new Period().withWeeks(1);
		Interval i = new Interval(sprintStartDate, weekPeriod);
		int hours = 0;
		while( i.getEnd().isBefore( sprintEndDate ) ){
		    logger.info( "week: " + i.getStart().getWeekOfWeekyear()
		            + " start: " + df.format( i.getStart().toDate() )
		            + " end: " + df.format( i.getEnd().minusMillis(1).toDate() ) );
		    i = new Interval( i.getStart().plus( weekPeriod), weekPeriod );
		    int availabilityHours = Availability.getAvailability(i.getStart().toCalendar(Locale.US), username);
		    logger.info("Reported availability hours for [" + username + "]: " + availabilityHours);
		    hours += availabilityHours;
		}  
		
		
		Days days = Days.daysBetween(today.toDateTimeAtStartOfDay(), sprintEndDate);
		
		
		int hoursRemaining = Hours.hoursBetween(today.toDateTimeAtCurrentTime(), sprintEndDate).getHours();
		if(hoursRemaining < 0) hoursRemaining = 0;
		logger.info("HoursToSprintEnd="+ hoursRemaining);
		logger.info("DayOfWeek="+today.getDayOfWeek());
		logger.info("WeekOfWeekyear="+today.getWeekOfWeekyear());
		logger.info("Hours from DB=" + hours);

		// --> Find week numbers
		// --> Check that current date is between start/end date of sprint
		
		// Lookup how many hours this user has for the sprint
		// --> lookup in HBase
		// --> 
		
		return hoursRemaining;
	}

}
