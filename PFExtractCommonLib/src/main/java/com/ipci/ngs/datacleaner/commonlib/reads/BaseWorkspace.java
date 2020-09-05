package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;
import com.ipci.ngs.datacleaner.commonlib.utilities.PropertiesSettingsFile;
import com.ipci.ngs.datacleaner.commonlib.utilities.SettingsFile;

public final class BaseWorkspace implements Workspace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String path;
	private final SettingsFile settingsFile;
	private final Specimen specimen;
	
	public BaseWorkspace(final String path) throws IOException {
		this.path = path;
		this.settingsFile = new PropertiesSettingsFile(FilenameUtils.concat(path, "settings.properties")); 
		this.settingsFile.load();
		this.specimen = new BaseSpecimen(settingsFile);
	}
	
	@Override
	public Specimen specimen() {
		return specimen;
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public SettingsFile settingsFile() {
		return settingsFile;
	}

	@Override
	public JobState state() {
		return settingsFile.state();
	}

	@Override
	public void setState(JobState state) throws IOException {
		settingsFile.setState(state);
		settingsFile.save();
	}

	@Override
	public String id() {
		return settingsFile.id();
	}

	@Override
	public String log() throws IOException {
		
		final Path logPath = Paths.get(FilenameUtils.concat(this.path, "logs/log.txt"));
		if(logPath.toFile().exists()) {
			List<String> allLines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
			return String.join("\n", allLines);
		}
		
		return StringUtils.EMPTY;
	}

	@Override
	public List<WorkspaceOutput> outputs() {
		try {
			return new WorkspaceOutputsImpl(this).items();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<WorkspaceReference> references() {	
		try {
			return new WorkspaceReferencesImpl(this).items();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<ReadStats> stats() {
		return new ExtractionStatsImpl(this).execute();
	}
}
