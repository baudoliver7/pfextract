package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.ipci.ngs.datacleaner.commonlib.utilities.CachedSettingsFile;
import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;
import com.ipci.ngs.datacleaner.commonlib.utilities.SettingsFile;

public final class CachedWorkspace implements Workspace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JobState state;
	private final String path;
	private final SettingsFile settingsFile;
	private final Specimen specimen;
	private final String id;
	private final String log;
	private final List<WorkspaceOutput> outputs;
	private final Set<WorkspaceReference> references;
	private final List<ReadStats> stats;
	
	public CachedWorkspace(final Workspace workspace) throws IOException {
		this.log = workspace.log();
		this.id = workspace.id();
		this.settingsFile = new CachedSettingsFile(workspace.settingsFile());
		this.specimen = new CachedSpecimen(workspace.specimen());
		this.path = workspace.path();
		this.state = workspace.state();
		this.outputs = workspace.outputs();
		this.references = workspace.references();
		this.stats = workspace.stats();
	}
	
	@Override
	public Specimen specimen() {
		return specimen;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public SettingsFile settingsFile() {
		return settingsFile;
	}

	@Override
	public JobState state() {
		return state;
	}

	@Override
	public void setState(JobState state) throws IOException {
		this.state = state;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String log() throws IOException {
		return log;
	}

	@Override
	public List<WorkspaceOutput> outputs() {
		return outputs;
	}

	@Override
	public Set<WorkspaceReference> references() {
		return references;
	}

	@Override
	public List<ReadStats> stats() {
		return stats;
	}
}
