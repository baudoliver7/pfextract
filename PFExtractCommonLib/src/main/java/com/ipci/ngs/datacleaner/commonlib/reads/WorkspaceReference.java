package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.Serializable;

public interface WorkspaceReference extends Serializable {
	String name();
	String path();
	double weightInGo();
}
