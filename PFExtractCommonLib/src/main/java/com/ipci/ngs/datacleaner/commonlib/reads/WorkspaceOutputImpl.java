package com.ipci.ngs.datacleaner.commonlib.reads;

import java.time.ZonedDateTime;

public final class WorkspaceOutputImpl implements WorkspaceOutput {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String folder;
	private final String name;
	private final String path;
	private final double weightInGo;
	private final ZonedDateTime lastModificationDate;
	
	public WorkspaceOutputImpl(final String folder, final String name, final String path, final double weightInGo, final ZonedDateTime lastModificationDate) {
		this.folder = folder;
		this.name = name;
		this.path = path;
		this.weightInGo = weightInGo;
		this.lastModificationDate = lastModificationDate;
	}
	
	@Override
	public String folder() {
		return folder;
	}

	@Override
	public String name() {
		return name;
	}
	
	@Override
	public String path() {
		return path;
	}

	@Override
	public double weightInGo() {
		return weightInGo;
	}

	@Override
	public ZonedDateTime lastModificationDate() {
		return lastModificationDate;
	}

}
