package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.io.Serializable;

import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;

public interface PipelineCommand extends Serializable {
	Workspace workspace();
	boolean hasStep(PipelineStep step);
	PipelineCommandItem getCommandOf(PipelineStep step);
	void put(PipelineStep step);
	void put(PipelineStep step, Object ... value);
	int count();
	void deleteIntermediateFiles(boolean enable);
	boolean canDeleteIntermediateFiles();
}
