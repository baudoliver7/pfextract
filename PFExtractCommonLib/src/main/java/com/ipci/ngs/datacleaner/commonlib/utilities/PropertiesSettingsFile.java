package com.ipci.ngs.datacleaner.commonlib.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import com.ipci.ngs.datacleaner.commonlib.reads.PairRead;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.SamRead;
import com.ipci.ngs.datacleaner.commonlib.reads.SingleRead;

public final class PropertiesSettingsFile implements SettingsFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String path;
	private final File file;
	private Properties prop = null;
	
	public PropertiesSettingsFile(final String path) {
		this.path = path;
		this.file = new File(path);		
	}
	
	private void validateState() {
		if(prop == null)
			throw new IllegalArgumentException("Settings file not loaded !");
	}
	
	private void validateExists() {	
		if(!exists())
			throw new IllegalArgumentException(String.format("Settings file not exists (%s) !", path));
	}
	
	public void load() throws IOException {
		
		validateExists();
		
		try (InputStream input = new FileInputStream(path)) {		    
			 prop = new Properties();
	         prop.load(input);
       } catch (IOException e) {
          throw new RuntimeException(e);
       }
	}
	
	@Override
	public void setOrigin(final ReadEntry origin) {
		setEntry("origin", origin);
	}

	@Override
	public ReadEntry origin() {
		return getEntry("origin");
	}

	@Override
	public void setIn(final ReadEntry in) {
		setEntry("in", in);
	}

	@Override
	public ReadEntry in() {
		return getEntry("in");
	}

	@Override
	public void setOut(final ReadEntry out) {
		setEntry("out", out);
	}

	@Override
	public ReadEntry out() {
		return getEntry("out");
	}

	@Override
	public void setStep(PipelineStep step) {
		put("step", step.name());
	}

	@Override
	public PipelineStep step() {
		return PipelineStep.valueOf(get("step"));
	}
	
	private void put(String key, String value) {
		validateState();
		prop.setProperty(key, value);
	}
	
	private void remove(String key) {
		validateState();
		
		if(prop.containsKey(key))
			prop.remove(key);
	}
	
	private boolean contains(String key) {
		validateState();		
		return prop.containsKey(key);
	}
	
	private String get(String key) {		
		validateState();
		return prop.getProperty(key);
	}
	
	private void setEntry(String key, ReadEntry in) {
		
		final String key2 = String.format("%s2", key);
		
		put(key, in.first().getAbsolutePath());
		if(in.files().size() == 2) {
			put(key2, in.second().getAbsolutePath());
		} else {
			remove(key2);
		}
	}
	
	private ReadEntry getEntry(String key) {
		if(!contains(key))
			throw new IllegalArgumentException(String.format("Unable to read %s key !", key));
		
		final String key2 = String.format("%s2", key);
		
		if(contains(key2)) {
			return new PairRead(new File(get(key)), new File(get(key2)));
		} else {
			final File origin = new File(get(key));
			if(FilenameUtils.isExtension(file.getAbsolutePath(), "sam")) {
				return new SamRead(origin);
			} else {
				return new SingleRead(origin);
			}
		}
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public void save() throws IOException { 
		
		validateState();
		
		validateExists();
		
		try (OutputStream output = new FileOutputStream(path) ) {
            // save properties to project root folder
            prop.store(output, null);
        }
	}

	@Override
	public File toFile() {
		return new File(path);
	}

	@Override
	public void create() throws IOException {
		if(!exists()) {
			file.createNewFile();
			load();
		}
	}

	@Override
	public void setDate(LocalDateTime date) {
		put("date", date.toString());
	}

	@Override
	public LocalDateTime date() {
		return LocalDateTime.parse(get("date"));
	}

	@Override
	public void setId(String id) {
		put("id", id);
	}

	@Override
	public String id() {
		return get("id");
	}

	@Override
	public void setState(JobState state) {
		put("state", state.name());
	}

	@Override
	public JobState state() {
		return JobState.valueOf(get("state"));
	}
}
