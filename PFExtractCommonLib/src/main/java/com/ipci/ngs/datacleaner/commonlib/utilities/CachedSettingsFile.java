package com.ipci.ngs.datacleaner.commonlib.utilities;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import com.ipci.ngs.datacleaner.commonlib.reads.CachedFile;
import com.ipci.ngs.datacleaner.commonlib.reads.CachedReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;

public final class CachedSettingsFile implements SettingsFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final ReadEntry origin;
	private final ReadEntry in;
	private final ReadEntry out;
	private final PipelineStep step;
	private final boolean exists;
	private final File file;
	private final String id;
	private final LocalDateTime date;
	private final JobState state;
	
	public CachedSettingsFile(final SettingsFile settingsFile) {
		this.origin = new CachedReadEntry(settingsFile.origin());	
		this.in = new CachedReadEntry(settingsFile.in());
		this.out = new CachedReadEntry(settingsFile.out());
		this.step = settingsFile.step();
		this.exists = settingsFile.exists();
		this.file = new CachedFile(settingsFile.toFile());
		this.date = settingsFile.date();
		this.id = settingsFile.id();
		this.state = settingsFile.state();
	}
	
	@Override
	public void load() throws IOException {
		throw new UnsupportedOperationException("CachedSettingsFile#load"); 
	}
	
	@Override
	public void setOrigin(final ReadEntry origin) {
		throw new UnsupportedOperationException("CachedSettingsFile#setOrigin"); 
	}

	@Override
	public ReadEntry origin() {
		return origin;
	}

	@Override
	public void setIn(final ReadEntry in) {
		throw new UnsupportedOperationException("CachedSettingsFile#setIn"); 
	}

	@Override
	public ReadEntry in() {
		return in;
	}

	@Override
	public void setOut(final ReadEntry out) {
		throw new UnsupportedOperationException("CachedSettingsFile#setOut");
	}

	@Override
	public ReadEntry out() {
		return out;
	}

	@Override
	public void setStep(PipelineStep step) {
		throw new UnsupportedOperationException("CachedSettingsFile#setStep");
	}

	@Override
	public PipelineStep step() {
		return step;
	}

	@Override
	public boolean exists() {
		return exists;
	}

	@Override
	public void save() throws IOException { 
		throw new UnsupportedOperationException("CachedSettingsFile#save");
	}

	@Override
	public File toFile() {
		return file;
	}

	@Override
	public void create() throws IOException {
		throw new UnsupportedOperationException("CachedSettingsFile#create");
	}

	@Override
	public void setDate(LocalDateTime date) {
		throw new UnsupportedOperationException("CachedSettingsFile#setDate");
	}

	@Override
	public LocalDateTime date() {
		return date;
	}

	@Override
	public void setId(String id) {
		throw new UnsupportedOperationException("CachedSettingsFile#setId");
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public void setState(JobState state) {
		throw new UnsupportedOperationException("CachedSettingsFile#setState");
	}

	@Override
	public JobState state() {
		return state;
	}
}
