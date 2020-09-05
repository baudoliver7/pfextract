package com.ipci.ngs.datacleaner.commonlib.reads;

public class NameWrapper implements Name {
	
	private final Name origin;
	
	public NameWrapper(final Name origin) {
		this.origin = origin;
	}
	
	@Override
	public String value() {
		return origin.value();
	}

}
