package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.IOException;
import java.util.Set;

public interface WorkspaceReferences {
	Set<WorkspaceReference> items() throws IOException;
}
