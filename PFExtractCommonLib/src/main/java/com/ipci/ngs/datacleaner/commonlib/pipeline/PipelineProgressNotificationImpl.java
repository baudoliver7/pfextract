package com.ipci.ngs.datacleaner.commonlib.pipeline;

public final class PipelineProgressNotificationImpl implements PipelineProgressNotification {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String workspaceId;
	private final String action;
	private final int progressPercent;
	
	public PipelineProgressNotificationImpl(final String workspaceId, final String action, final int progressPercent) {
		this.workspaceId = workspaceId;
		this.action = action;
		this.progressPercent = progressPercent;
	}
	
	@Override
	public String workspaceId() {
		return workspaceId;
	}

	@Override
	public String action() {
		return action;
	}

	@Override
	public int progressPercent() {
		return progressPercent;
	}

}
