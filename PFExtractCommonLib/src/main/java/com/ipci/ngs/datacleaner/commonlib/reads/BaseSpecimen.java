package com.ipci.ngs.datacleaner.commonlib.reads;

import com.ipci.ngs.datacleaner.commonlib.utilities.SettingsFile;

public final class BaseSpecimen implements Specimen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; 

	private final SettingsFile settingsFile;
	
	public BaseSpecimen(final SettingsFile settingsFile) {
		this.settingsFile = settingsFile;
	}
	
	@Override
	public String name() {
		return settingsFile.origin().name();
	}

	@Override
	public double weight() {
		
		final ReadEntry entry = settingsFile.origin();
		
		double bytes = entry.first().length();
		double weight = bytes / (1024 * 1024 * 1024);
		
		if(entry.type() == ReadEntryType.PE) {
			bytes = entry.second().length();
			weight += bytes / (1024 * 1024 * 1024);
		}
		
		return weight;
	}

	@Override
	public String path() {
		return settingsFile.toFile().getParent();
	}

	@Override
	public ReadEntryType type() {
		return settingsFile.origin().type();
	}

}
