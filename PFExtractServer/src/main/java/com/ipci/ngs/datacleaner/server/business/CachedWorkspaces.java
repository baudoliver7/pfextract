package com.ipci.ngs.datacleaner.server.business;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.ipci.ngs.datacleaner.commonlib.reads.CachedWorkspace;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;

public final class CachedWorkspaces implements Workspaces {
	
	
	private	final Workspaces workspaces;
	
	public CachedWorkspaces() {
		this(new WorkspacesImpl());
	}
	
	public CachedWorkspaces(final Workspaces workspaces) {
		this.workspaces = workspaces;
	}
	
	@Override
	public Workspace init(ReadEntry entry) throws IOException {
		return new CachedWorkspace(workspaces.init(entry));
	}

	@Override
	public List<Workspace> items() throws IOException {
		
		final List<Workspace> items = new ArrayList<>();

		for (Workspace w : workspaces.items()) {
			items.add(new CachedWorkspace(w));
		}
		
		return items;
	}

	@Override
	public Workspace get(String id) throws IOException {
		return new CachedWorkspace(workspaces.get(id));
	}

	@Override
	public Workspace init(ReadEntry entry, String id, LocalDateTime date) throws IOException {
		return new CachedWorkspace(workspaces.init(entry, id, date));
	}

}
