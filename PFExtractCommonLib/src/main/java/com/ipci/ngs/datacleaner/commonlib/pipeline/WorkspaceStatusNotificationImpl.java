package com.ipci.ngs.datacleaner.commonlib.pipeline;

import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;

public final class WorkspaceStatusNotificationImpl implements WorkspaceStatusNotification {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String workspaceId;
	private final String status;
	private final JobState state;
	
	public WorkspaceStatusNotificationImpl(final String workspaceId, final String status, final JobState state) {
		this.workspaceId = workspaceId;
		this.status = status;
		this.state = state;
	}
	
	@Override
	public String workspaceId() {
		return workspaceId;
	}

	@Override
	public String status() {
		return status;
	}
	
	@Override
	public JobState state() {
		return state;
	}

}
