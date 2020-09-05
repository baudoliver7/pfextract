package com.ipci.ngs.datacleaner.commonlib.pipeline;

public final class PipelineLogNotificationImpl implements PipelineLogNotification {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String workspaceId;
	private final String log;
	
	public PipelineLogNotificationImpl(final String workspaceId, final String log) {
		this.workspaceId = workspaceId;
		this.log = log;
	}
	
	@Override
	public String workspaceId() {
		return workspaceId;
	}

	@Override
	public String log() {
		return log;
	}

}
