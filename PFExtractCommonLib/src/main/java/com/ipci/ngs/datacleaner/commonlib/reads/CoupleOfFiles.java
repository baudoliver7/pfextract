package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CoupleOfFiles {
	List<File> items() throws IOException;
}
