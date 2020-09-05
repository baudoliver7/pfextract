package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ReadFiles implements CoupleOfFiles {

	private final CoupleOfFiles origin;
	private final String regex;
	private final boolean match;
	
	public ReadFiles(final CoupleOfFiles origin) {
		this(origin, ".*(PS_1\\.|PS_2\\.|PS_R1\\.|PS_R2\\.).*", false);
	}
	
	public ReadFiles(final CoupleOfFiles origin, final String regex, final boolean match) {
		this.origin = origin;
		this.regex = regex;
		this.match = match;
	}
	
	@Override
	public List<File> items() throws IOException {
		final List<File> filteredFiles = new ArrayList<>();
		for (File file : origin.items()) {
			final boolean belong = file.getName().matches(regex) && match || !file.getName().matches(regex) && !match;
			if(belong) {
				filteredFiles.add(file);
			}
		}
		
		return filteredFiles;
	}

}
