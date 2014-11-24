# Calendar Extract

Create a calendar-based GTFS feed from a calendar-date-based GTFS feed, using one day as a 'typical' day. Still a work in progress.

## Usage:

usage: calendar-extract <prototype date> MTWRFSU <start date> <end date> inGtfs outGtfs
Specify a prototype day and the first letter of the days on which you want a calendar based on that day to run.
Dates are specified as YYYY-MM-DD
Service days are specified using the following acronyms:
M - Monday
T - Tuesday
W - Wednesday
R - Thurday
F - Friday
S - Saturday
U - Sunday
Days must be specified in this order.

So, for example, to extract the service from November 24, 2014 and make it run Monday thru Friday from November 1, 2014 to January 1, 2015:

    calendar-extract 2014-11-24 MTWRF 2014-11-01 2015-01-01 google_transit.zip out.zip
