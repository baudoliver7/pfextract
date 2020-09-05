package com.ipci.ngs.datacleaner.commonlib.reads;

public final class WorkspaceReferenceImpl implements WorkspaceReference {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final String path;
	private final double weightInGo;
	
	public WorkspaceReferenceImpl(final String name, final String path, final double weightInGo) {
		this.name = name;
		this.path = path;
		this.weightInGo = weightInGo;
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
	public boolean equals(Object other) {
		
		if(other == null)
			return false;
		
		if(other == this)
			return true;
		
		if(other instanceof WorkspaceReference) {
			final WorkspaceReference objOther = (WorkspaceReference) other;
			if(objOther.path().equals(path) && objOther.weightInGo() == weightInGo) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return 8549 + 78452211 * path.hashCode() + 78452211 * ((Double)weightInGo).hashCode();
	}

}
