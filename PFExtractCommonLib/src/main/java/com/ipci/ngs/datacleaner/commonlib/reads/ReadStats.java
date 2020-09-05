package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.Serializable;

public interface ReadStats extends Serializable {

	String name();
	
	String step();
	
	long numberOfReads();
	
	double percent(); 
}
