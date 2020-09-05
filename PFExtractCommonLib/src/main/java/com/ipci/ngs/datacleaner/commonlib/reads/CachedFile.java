package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;

public final class CachedFile extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final boolean canExecute;
	private final boolean canRead;
	private final File parentFile;
	private final String absolutePath;
	private final String name;
	
	public CachedFile(File file) {
		super(file.getAbsolutePath());
		
		this.canExecute = file.canExecute();
		this.canRead = file.canRead();
		if(file.getParentFile() == null)
			this.parentFile = null;
		else
			this.parentFile = new CachedFile(file.getParentFile());
		
		this.absolutePath = file.getAbsolutePath();
		this.name = file.getName();
	}
	
	@Override
	public boolean canExecute() {
		return canExecute;
	}
	
	@Override
	public boolean canRead() {
		return canRead;
	}
	
	@Override
	public File getParentFile() {
		return parentFile;
	}
	
	@Override
	public String getAbsolutePath() {
		return absolutePath;
	}
	
	@Override
	public String getName() {
		return name;
	}

}
