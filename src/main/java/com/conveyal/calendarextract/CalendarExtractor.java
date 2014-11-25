package com.conveyal.calendarextract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Calendar;
import com.conveyal.gtfs.model.CalendarDate;
import com.conveyal.gtfs.model.Service;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;

/**
 * Extract service for a given day from a GTFS feed and create calendars from it.
 * @author mattwigway
 */
public class CalendarExtractor {
	private GTFSFeed feed;
	private Map<String, Service> services;
	
	public CalendarExtractor (GTFSFeed feed) {
		this.feed = feed;
		this.services = new HashMap<String, Service>();
	}
	
	public GTFSFeed getFeed() {
		return feed;
	}
	
	/**
	 * Extract a calendar date and create calendar entries for it.
	 * @param date The date to extract
	 * @param start The first day the created calendar should be active
	 * @param end The last day the created calendar should be active
	 * @param monday 1 if the calendar is active on Monday, 0 otherwise
	 * @param tuesday As before.
	 * @param wednesday
	 * @param thursday
	 * @param friday
	 * @param saturday
	 * @param sunday
	 */
	public void extractCalendar (DateTime date, DateTime start, DateTime end, int monday, int tuesday,
			int wednesday, int thursday, int friday, int saturday, int sunday) {
		List<CalendarDate> calendarDate = new ArrayList<CalendarDate>();
		
		// Find all of the active calendar dates
		for (Service service : feed.services.values()) {
			if (service.activeOn(date)) {
				Calendar cal = new Calendar();
				cal.monday = monday;
				cal.tuesday = tuesday;
				cal.wednesday = wednesday;
				cal.thursday = thursday;
				cal.friday = friday;
				cal.saturday = saturday;
				cal.sunday = sunday;
				
				cal.start_date = dateTimeToInt(start);
				cal.end_date = dateTimeToInt(end);
				
				cal.service = new Service(service.service_id);
				cal.service.calendar = cal;
				
				// TODO: handle collisions when extracting multiple calendars
				// The way this can happen is that if you extract more than one calendar as a prototype day,
				// and a particular service ID is active both days.
				// Fixing this requires merging and splitting calendars, and is thus nontrivial.
				if (services.containsKey(service.service_id)) {
					throw new UnsupportedOperationException("Service ID " + service.service_id +
							" encountered multiple times during extraction.");
				}
						
				
				services.put(service.service_id, cal.service);
			}
		}		
	}
	
	/**
	 * Extract a calendar date and create calendar entries for it.
	 * @param date The date to extract
	 * @param start The first day the created calendar should be active
	 * @param end The last day the created calendar should be active
	 * @param monday Is the calendar active on Monday?
	 * @param tuesday As before.
	 * @param wednesday
	 * @param thursday
	 * @param friday
	 * @param saturday
	 * @param sunday
	 */
	public void extractCalendar (DateTime date, DateTime start, DateTime end, boolean monday, boolean tuesday,
			boolean wednesday, boolean thursday, boolean friday, boolean saturday, boolean sunday) {
		extractCalendar(date, start, end, b2i(monday), b2i(tuesday), b2i(wednesday), b2i(thursday), b2i(friday),
				b2i(saturday), b2i(sunday));	
	}
	
	/**
	 * Apply the created calendars, wiping out all other service information in the GTFS.
	 */
	public void apply () {
		feed.services.clear();
		Set<String> serviceIds = services.keySet();
		
		for (String id : serviceIds) {
			feed.services.put(id, services.get(id));
		}
		
		// get rid of trips that no longer exist
		HashSet<String> removedTrips = new HashSet<String>();
		Iterator<Trip> ti = feed.trips.values().iterator();
		while (ti.hasNext()) {
			Trip t = ti.next();
								
			if (!serviceIds.contains(t.service.service_id)) {
				ti.remove();
				// MapDB is very slow at deletion; it's faster to save the trip IDs we want to delete
				// and then loop over every stop time and delete the ones we don't want than to delete trips one at a time
				// When you think about it, this sort of makes sense, since MapDB is disk-backed; this does not require lots of
				// seeking
				removedTrips.add(t.trip_id);
			}
		}
		
		Iterator<Tuple2> stopTimeIterator = feed.stop_times.keySet().iterator();
		while (stopTimeIterator.hasNext()) {
			Tuple2 n = stopTimeIterator.next();
			
			if (removedTrips.contains(n.a)) {
				stopTimeIterator.remove();
			}
		}
	}
	
	/**
	 * Convert a boolean to a 1/0 int (as used in GTFS for days active)
	 */
	private static int b2i(boolean b) {
		return b ? 1 : 0;
	}
	
	/**
	 * Convert a datetime to an int YYYYMMDD
	 */
	private static int dateTimeToInt (DateTime d) {
		return d.getYear() * 10000 + d.getMonthOfYear() * 100 + d.getDayOfMonth();
	}
}
