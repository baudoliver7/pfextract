package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.util.List;

import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;

public final class PipelineCommandItemImpl implements PipelineCommandItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final PipelineStep step;
	private final List<Object> values;
	
	public PipelineCommandItemImpl(final PipelineStep step, final List<Object> values) {
		this.step = step;
		this.values = values;
	}
	
	@Override
	public PipelineStep step() {
		return step;
	}

	@Override
	public List<Object> values() {
		return values;
	}
	
	@Override
	public boolean equals(Object other) {
		
		if(other == null)
			return false;
		
		if(other == this)
			return true;
		
		if(!(other instanceof PipelineCommandItem))
			return false;
		
		final PipelineCommandItem item = (PipelineCommandItem)other;
		if(item.step() == step)
			return true;
			
		return false;
	}
	
	@Override
	public int hashCode() {
		return step.hashCode() * 78 + 78911123;
	}
}
