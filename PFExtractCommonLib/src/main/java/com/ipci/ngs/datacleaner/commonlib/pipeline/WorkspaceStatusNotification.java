package com.ipci.ngs.datacleaner.commonlib.pipeline;

import java.io.Serializable;

import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;

public interface WorkspaceStatusNotification extends Serializable {
	String workspaceId();
	String status();
	JobState state();
}
