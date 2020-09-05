package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface WorkspaceOutput extends Serializable {
	String folder();
	String name();
	String path();
	double weightInGo();
	ZonedDateTime lastModificationDate();
}
