package com.quintrix.jepsen.erik;

public class TimeFilter {
	public static boolean filterRegion(String prospect, String filter) {
		if (prospect.length() < filter.length()) return false;
		for (int i = 0; i < filter.length(); i++) 
			if (!(new Byte(filter.getBytes()[i]).equals(prospect.getBytes()[i]))) return false;
		return true;
	}
}
