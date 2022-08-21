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
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	int[] randomOrder;
    	DateTimeFormatter formatter;
    	DateTimeFormatterBuilder builder;
    	List<String> randomizedTimeZones;
    	Random rng;
    	ZonedDateTime zonedNow, nowPlusEight;
    	
        final Duration timeSpan = Duration.ofHours(8);
        final String[] TIME_ZONES = {
        		"Africa/Cairo",
        		"America/Chicago",
        		"America/Denver",
        		"America/New_York",
        		"America/Los_Angeles",
        		"Antarctica/McMurdo",
        		"Asia/Hong_Kong",
        		"Asia/Kolkata",
        		"Asia/Seoul",
        		"Asia/Tokyo",
        		"Atlantic/Reykjavik",
        		"Australia/Melbourne",
        		"Europe/Kiev",  // Yes, thanks to a Russian invasion, we all know it's spelled Kyiv, but this is how Java spells it. :P
        		"Europe/London",
        		"Europe/Moscow"
        };
        
    	rng = new Random();
        zonedNow = ZonedDateTime.now();
        nowPlusEight = zonedNow.plus(timeSpan);
        randomOrder = new int[TIME_ZONES.length];
        randomizedTimeZones = new ArrayList<>(TIME_ZONES.length);
        builder = new DateTimeFormatterBuilder();
        
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
        for (int i = 0; i < TIME_ZONES.length; i++) randomOrder[i] = -1;
        for (int i = 0; i < TIME_ZONES.length; i++) {
        	int nextNum = rng.nextInt(TIME_ZONES.length);
        	while (doesContain(randomOrder, nextNum)) nextNum = rng.nextInt(TIME_ZONES.length);
        	randomOrder[i] = nextNum;
        }
        for (int i = 0; i < TIME_ZONES.length; i++) randomizedTimeZones.add(i, TIME_ZONES[randomOrder[i]]);
        
        System.out.printf("The current date and time in this time zone is %s.\n",
        		zonedNow.format(formatter));
        System.out.println();
        System.out.println("Here is that same time in a few time zones. Their order has been randomized.");
        for (String thisZone: randomizedTimeZones) {
        	System.out.printf("%s -- %s\n",
        			thisZone,
        			zonedNow.withZoneSameInstant(ZoneId.of(thisZone)).format(formatter));
        }
        System.out.println();
        System.out.printf("There is/are %d Antarctic time zone(s) in this list.\n",
        		randomizedTimeZones
        		.stream()
        		.filter(m -> {
        			final String key = "Antarctica/";
        			if (m.length() < key.length()) return false;
        			byte[] keyBytes = key.getBytes();
        			byte[] targetBytes = m.getBytes();
        			for (int i = 0; i < keyBytes.length; i++) {
        				Byte thisByte = keyBytes[i];
        				if (!thisByte.equals(targetBytes[i])) return false;
        			}
        			return true;
        		})
        		.count());
        System.out.printf("The first Asian time zone in this randomized list is %s.\n",
        		randomizedTimeZones
        		.stream()
        		.filter(timeZone -> {
        			final Byte[] asiaTz = new Byte[] {'A', 's', 'i', 'a', '/'};
        			byte[] prospect = timeZone.getBytes();
        			for (int i = 0; i < asiaTz.length; i++) {
        				if (!asiaTz[i].equals(prospect[i])) return false;
        			}
        			return true;
        		})
        		.findFirst()
        		.get());
        System.out.println();
        System.out.println("Here are those same time zones eight hours later.");
        randomizedTimeZones
        	.parallelStream()
        	.map(timeZone -> ZoneId.of(timeZone))
        	.forEach(timeZone -> System
        			.out
        			.printf("%s -- %s\n",
        					timeZone.getId(),
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
}
