package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.io.Serializable;
import java.util.List;

import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;

public interface PipelineCommandItem extends Serializable {
	PipelineStep step();
	List<Object> values();
}
