package com.quintrix.jepsen.erik;


import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class App 
{
    public static void main( String[] args )
    {
    	int zoneCount, regionCount;
    	int[] randomOrder, regionNumbers;
    	DateTimeFormatter formatter;
    	DateTimeFormatterBuilder builder;
    	List<String> timeZones, timeRegions, randomizedTimeZones;
    	Random rng;
    	ZonedDateTime zonedNow, nowPlusEight;
    	
        final Duration timeSpan = Duration.ofHours(8);
        timeZones = new ArrayList<>();
        timeZones.add("Africa/Cairo");    // To provide contrast, this list is alphabetized.
        timeZones.add("America/Chicago"); // It will not be by the time the user sees it.
        timeZones.add("America/Denver");
        timeZones.add("America/New_York");
        timeZones.add("America/Los_Angeles");
        timeZones.add("Antarctica/McMurdo");
        timeZones.add("Asia/Hong_Kong");
        timeZones.add("Asia/Kolkata");
        timeZones.add("Asia/Seoul");
        timeZones.add("Asia/Tokyo");
        timeZones.add("Atlantic/Reykjavik");
        timeZones.add("Australia/Melbourne");
        timeZones.add("Europe/Kiev");    // Yes, thanks to a Russian invasion, we all now know
        timeZones.add("Europe/London");  // it's spelled Kyiv, but this is how Java spells it. :P
        timeZones.add("Europe/Moscow");
        timeRegions = new ArrayList<>();
        for (String thisZone: timeZones) {
        	String prospect = thisZone.split("/")[0];
        	if (!doesContain(timeRegions, prospect)) timeRegions.add(prospect);
        }

        rng = new Random();
        zonedNow = ZonedDateTime.now();
        nowPlusEight = zonedNow.plus(timeSpan);
        zoneCount = timeZones.stream().mapToInt(e -> 1).sum();
        regionCount = timeRegions.stream().mapToInt(e -> 1).sum();
        randomOrder = new int[zoneCount];
        randomizedTimeZones = new ArrayList<>(zoneCount);
        regionNumbers = new int[2];
        builder = new DateTimeFormatterBuilder();
        
        // I could do this in a single line with a String pattern, or as chained method calls
        // to builder, but this is easier to understand, IMHO. 
        builder.appendZoneOrOffsetId();
        builder.appendLiteral(" -- ");
        builder.appendValue(ChronoField.HOUR_OF_DAY, 2);
        builder.appendLiteral(':');
        builder.appendValue(ChronoField.MINUTE_OF_HOUR, 2);
        builder.appendLiteral(':');
        builder.appendValue(ChronoField.SECOND_OF_MINUTE, 2);
        builder.appendLiteral(" on ");
        builder.appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL);
        builder.appendLiteral(", ");
        builder.appendValue(ChronoField.DAY_OF_MONTH, 2);
        builder.appendLiteral(' ');
        builder.appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL);
        builder.appendLiteral(' ');
        builder.appendValue(ChronoField.YEAR, 4, 19, SignStyle.EXCEEDS_PAD);
        formatter = builder.toFormatter();

        rng.setSeed(System.currentTimeMillis());
        regionNumbers[0] = rng.nextInt(regionCount);
        regionNumbers[1] = rng.nextInt(regionCount);
        
        for (int i = 0; i < timeZones.stream().count(); i++) randomOrder[i] = -1;
        for (int i = 0; i < timeZones.stream().count(); i++) {
        	int nextNum = rng.nextInt(zoneCount);
        	while (doesContain(randomOrder, nextNum)) nextNum = rng.nextInt(zoneCount);
        	randomOrder[i] = nextNum;
        }
        for (int i = 0; i < timeZones.stream().count(); i++) randomizedTimeZones.add(i, timeZones.get(randomOrder[i]));
        
        System.out.println("The current date and time in this time zone is" + System.lineSeparator() +
        		zonedNow.format(formatter));
        System.out.println();
        System.out.println("Here is that same time in a few time zones. Their order has been randomized.");
        for (String thisZone: randomizedTimeZones) {
        	System.out.println(
        			zonedNow
    				.withZoneSameInstant(ZoneId.of(thisZone))
    				.format(formatter));
        }
        System.out.println();
        System.out.printf("There is/are %d time zone(s) in %s in this list." + System.lineSeparator(),
        		randomizedTimeZones
        		.stream()
        		.filter(m -> TimeFilter.filterRegion(m, timeRegions.get(regionNumbers[0])))
        		.count(),
        		timeRegions.get(regionNumbers[0]));
        		// The following line could replace the .count(); above and get the same result as an integer.
        		// .mapToInt(e -> 1).reduce(0, Integer::sum));
        System.out.println();
        System.out.printf("The first time zone in the %s region in this randomized list is %s."
        		+ System.lineSeparator(),
        		timeRegions.get(regionNumbers[1]),
        		randomizedTimeZones
        		.stream()
        		.filter(m -> TimeFilter.filterRegion(m, timeRegions.get(regionNumbers[1])))
        		.findFirst()
        		.get());
        System.out.println();
        System.out.println("Here are those same time zones eight hours later. This list may appear in a different order");
        System.out.println("due to it being processed in parallel and each thread racing for the output stream resource.");
        randomizedTimeZones
        	.parallelStream()
        	.map(timeZone -> ZoneId.of(timeZone))
        	.forEach(timeZone -> System
        			.out
        			.println(
        					nowPlusEight
    						.withZoneSameInstant(timeZone)
    						.format(formatter)));
    }
    
    private static boolean doesContain(int[] array, int target) {
    	for (int i = 0; i < array.length; i++) {
    		if (array[i] == target) return true;
    	}
    	return false;
    }
    private static boolean doesContain(List<String> stringList, String target)
    {
    	if (stringList.stream().mapToInt(e -> 1).sum() == 0) return false;
    	for (String maybeThis: stringList) {
    		if (maybeThis.equals(target)) return true;
    	}
    	return false;
    }
    
}
