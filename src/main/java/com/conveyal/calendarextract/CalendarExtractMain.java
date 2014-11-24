package com.conveyal.calendarextract;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Agency;

public class CalendarExtractMain 
{
	// ensure date is formatted correctly
	private static Pattern datePat = Pattern.compile("([12][0-9]{3})-([0-9]{1,2})-([0-9]{1,2})");
	
	// ensure days of week are correct and ordered properly. 
	private static Pattern dayPat = Pattern.compile("M?T?W?R?F?S?U?");
	
	private static DateTimeZone tz;
	
    public static void main( String[] args )
    {
        if (args.length != 6) {
        	usage();
        	return;
        }
        
        // figure out infile and outfile
        File in = new File(args[4]);
        if (!in.exists()) {
        	System.err.println("Input file " + in.toString() + " does not exist!");
        	return;
        }
                
        // figure out the dates
        
        // load the input GTFS
        GTFSFeed inGtfs = GTFSFeed.fromFile(in.getAbsolutePath());
        
        // figure out the date
    
        DateTime date = parseDate(args[0]);
        DateTime start = parseDate(args[2]);
        DateTime end = parseDate(args[3]);
        
        String days = args[1].toUpperCase();
        
        // figure out the referenced days
        if (!dayPat.matcher(days).matches()) {
        	usage();
        	return;
        }
        
        boolean monday = days.contains("M");
        boolean tuesday = days.contains("T");
        boolean wednesday = days.contains("W");
        boolean thursday = days.contains("R");
        boolean friday = days.contains("F");
        boolean saturday = days.contains("S");
        boolean sunday = days.contains("U");
        
        CalendarExtractor e = new CalendarExtractor(inGtfs);
        
        e.extractCalendar(date, start, end, monday, tuesday, wednesday, thursday, friday, saturday, sunday);
        
        e.apply();
        
        inGtfs.toFile(args[5]);
    }
    
    public static void usage () {
    	System.err.println("Convert calendar_dates from prototype days into calendars");
    	System.err.println("usage: calendar-extract <prototype date> MTWRFSU <start date> <end date> inGtfs outGtfs ");
    	System.err.println("  Specify a prototype day and the first letter of the days on which you want a calendar based on that day to run.");
    	System.err.println("  Dates are specified as YYYY-MM-DD");
    	System.err.println("  Service days are specified using the following acronyms:");
    	System.err.println("   M - Monday");
    	System.err.println("   T - Tuesday");
    	System.err.println("   W - Wednesday");
    	System.err.println("   R - Thurday");
    	System.err.println("   F - Friday");
    	System.err.println("   S - Saturday");
    	System.err.println("   U - Sunday");
    	System.err.println("Days must be specified in this order.");
    	return;
    }
    
    /**
     * Parse a datetime in the format YYYY-MM-DD.
     * @param ret
     * @return
     */
    public static DateTime parseDate (String date) {
        // parse the input
        Matcher dm = datePat.matcher(date);
        
        if (!dm.matches())
        	return null;
        
        int year = Integer.parseInt(dm.group(1));
        int month = Integer.parseInt(dm.group(2));
        int day = Integer.parseInt(dm.group(3));
        
        return new DateTime(year, month, day, 0, 0);
    }
}
