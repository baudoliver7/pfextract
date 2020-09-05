package com.ipci.ngs.datacleaner.commonlib.reads;

public final class CachedSpecimen implements Specimen {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; 

	private final String name;
	private final double weight;
	private final String path;
	private final ReadEntryType type;
	
	public CachedSpecimen(final Specimen specimen) {
		this.name = specimen.name();
		this.weight = specimen.weight();
		this.path = specimen.path();
		this.type = specimen.type();
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public double weight() {
		return weight;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public ReadEntryType type() {
		return type;
	}

}
