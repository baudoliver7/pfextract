package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;
import com.ipci.ngs.datacleaner.commonlib.utilities.SettingsFile;

public interface Workspace extends Serializable {
	String id();
	Specimen specimen();
	SettingsFile settingsFile();
	String path();
	void setState(JobState state) throws IOException;
	JobState state();
	String log() throws IOException;
	List<WorkspaceOutput> outputs();
	List<ReadStats> stats();
	Set<WorkspaceReference> references();
}
