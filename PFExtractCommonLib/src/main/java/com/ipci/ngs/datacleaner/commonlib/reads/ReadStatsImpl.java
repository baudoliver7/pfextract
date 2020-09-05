package com.ipci.ngs.datacleaner.commonlib.reads;

public final class ReadStatsImpl implements ReadStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final String step;
	private final long numberOfReads;
	private final double percent;
	
	public ReadStatsImpl(final String name, final String step, final long numberOfReads, final double percent) {
		this.name = name;
		this.step = step;
		this.numberOfReads = numberOfReads;
		this.percent = percent;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public String step() {
		return step;
	}

	@Override
	public long numberOfReads() {
		return numberOfReads;
	}

	@Override
	public double percent() {
		return percent;
	}

}
