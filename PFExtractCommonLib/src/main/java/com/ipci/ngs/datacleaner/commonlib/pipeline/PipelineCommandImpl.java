package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;

public final class PipelineCommandImpl implements PipelineCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Set<PipelineCommandItem> items;
	private final Workspace workspace;
	private boolean canDeleteIntermediateFiles;
	
	public PipelineCommandImpl(Workspace workspace) {
		items = new HashSet<>();
		this.workspace = workspace;
		this.canDeleteIntermediateFiles = true;
	}
	
	@Override
	public boolean hasStep(PipelineStep step) {
		for (PipelineCommandItem item : items) {
			if(item.step() == step)
				return true;
		}
		
		return false;
	}

	@Override
	public PipelineCommandItem getCommandOf(PipelineStep step) {
		
		for (PipelineCommandItem item : items) {
			if(item.step() == step)
				return item;
		}
		
		throw new IllegalArgumentException("Pipeline step not found !");
	}

	@Override
	public void put(PipelineStep step) {
		put(step, new ArrayList<>());
	}

	@Override
	public void put(PipelineStep step, Object ... value) {
		items.add(new PipelineCommandItemImpl(step, Arrays.stream(value).collect(Collectors.toList())));
	}

	@Override
	public Workspace workspace() {
		return workspace;
	}

	@Override
	public int count() {
		return items.size();
	}

	@Override
	public void deleteIntermediateFiles(boolean enable) {
		this.canDeleteIntermediateFiles = enable;
	}

	@Override
	public boolean canDeleteIntermediateFiles() {
		return this.canDeleteIntermediateFiles;
	}

}
