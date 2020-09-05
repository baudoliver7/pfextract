package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public interface ReadEntry extends Serializable {
	String name();
	ReadEntryType type();
	List<File> files();
	File first();
	File second();
}
