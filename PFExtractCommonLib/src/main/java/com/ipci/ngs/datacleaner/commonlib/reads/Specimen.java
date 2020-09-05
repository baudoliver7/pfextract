package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.Serializable;

public interface Specimen extends Serializable {
	String name();
	double weight();
	String path();
	ReadEntryType type();
}
