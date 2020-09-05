package com.ipci.ngs.datacleaner.server.business;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;

public interface Workspaces {
	Workspace init(ReadEntry entry, String id, LocalDateTime date) throws IOException;
	Workspace init(ReadEntry entry) throws IOException;
	List<Workspace> items() throws IOException;
	Workspace get(String id) throws IOException;
}
