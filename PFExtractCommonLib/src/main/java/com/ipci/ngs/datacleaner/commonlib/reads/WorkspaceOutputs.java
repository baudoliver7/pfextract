package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.IOException;
import java.util.List;

public interface WorkspaceOutputs {
	List<WorkspaceOutput> items() throws IOException;
}
