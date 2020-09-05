package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.io.Serializable;

public interface PipelineProgressNotification extends Serializable {
	String workspaceId();
	String action();
	int progressPercent();
}
