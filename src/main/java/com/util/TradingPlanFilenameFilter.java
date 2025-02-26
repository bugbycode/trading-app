package com.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class TradingPlanFilenameFilter implements FilenameFilter {

	private static final Pattern pattern = Pattern.compile("^[a-z]+_(long|short)_\\d+(\\.\\d+)?$");
	
	@Override
	public boolean accept(File dir, String name) {
		String filename = name.toLowerCase();
		return pattern.matcher(filename).matches();
	}

}
