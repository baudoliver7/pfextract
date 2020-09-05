package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;

public final class NameOfFile implements Name {

	private final String name;
	
	public NameOfFile(final File file) {
		this(file.getName());
	}
	
	public NameOfFile(final String name) {
		this.name = name;
	}
	
	@Override
	public String value() {
		return name;
	}
}
