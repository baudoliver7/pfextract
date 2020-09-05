package com.ipci.ngs.datacleaner.commonlib.reads;

public final class ReadEntryMetadata {
	
	final static String regexR1 = ".*(_1\\.|_R1\\.).*";
	final static String regexR2 = ".*(_2\\.|_R2\\.).*";
	
	public static boolean isDirectRead(String filename) {
		return filename.matches(regexR1);
	}
	
	public static boolean isReverseRead(String filename) {
		return filename.matches(regexR2);
	}
	
	public static boolean isSingleRead(String filename) {
		return !isDirectRead(filename) && !isReverseRead(filename);
	}
}
