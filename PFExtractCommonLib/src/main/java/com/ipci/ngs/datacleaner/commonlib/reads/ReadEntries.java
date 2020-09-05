package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.IOException;
import java.util.List;

public interface ReadEntries {
	List<ReadEntry> items() throws IOException;
}
