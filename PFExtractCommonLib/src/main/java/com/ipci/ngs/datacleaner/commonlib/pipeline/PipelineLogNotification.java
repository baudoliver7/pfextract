package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.io.Serializable;

public interface PipelineLogNotification extends Serializable {
	String workspaceId();
	String log();
}
