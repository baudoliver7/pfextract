package com.ipci.ngs.datacleaner.commonlib.utilities;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;

public interface SettingsFile extends Serializable {
	
	void setId(String id);
	String id();
	
	void setDate(LocalDateTime date);
	LocalDateTime date();
	
	void setOrigin(ReadEntry origin);
	ReadEntry origin();
	
	void setIn(ReadEntry in);
	ReadEntry in();
	
	void setOut(ReadEntry out);
	ReadEntry out();
	
	void setStep(PipelineStep step);
	PipelineStep step();
	
	void setState(JobState state);
	JobState state();
	
	boolean exists();
	void load() throws IOException;
	
	void create() throws IOException;
	void save() throws IOException;
	
	File toFile();
}
