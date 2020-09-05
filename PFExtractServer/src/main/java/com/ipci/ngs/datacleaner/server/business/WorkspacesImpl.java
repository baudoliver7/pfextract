package com.ipci.ngs.datacleaner.server.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import com.ipci.ngs.datacleaner.commonlib.reads.BaseWorkspace;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFile;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFileWithoutExtension;
import com.ipci.ngs.datacleaner.commonlib.reads.PairedReadName;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntryType;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;
import com.ipci.ngs.datacleaner.commonlib.utilities.PropertiesSettingsFile;
import com.ipci.ngs.datacleaner.commonlib.utilities.SettingsFile;

public final class WorkspacesImpl implements Workspaces {
	
	private static final String WORKSPACES_PATH;
	
	static {
		try(InputStream stream = new FileInputStream("config/settings.properties")){
			final Properties config = new Properties();
			config.load(stream);
			WORKSPACES_PATH = config.getProperty("workspaces");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Workspace init(ReadEntry entry, String id, LocalDateTime date) throws IOException {
		
		// create directory
		final String originalPath;
		
		if(entry.type() == ReadEntryType.SE)
			originalPath = FilenameUtils.concat(
								WORKSPACES_PATH, 
								new NameOfFileWithoutExtension(
									new NameOfFile(
										entry.first()
									)
								).value()
						   );
		else
			originalPath = FilenameUtils.concat(
								WORKSPACES_PATH, 
								new NameOfFileWithoutExtension(
									new PairedReadName(
										new NameOfFile(entry.first())
									)
								).value() 
						   );
		
		int i = 0;
		String path = originalPath;
		while(new File(path).exists()) {
			path = originalPath + ++i;
		}
		
		new File(path).mkdir();
		
		final SettingsFile settingsFile = new PropertiesSettingsFile(FilenameUtils.concat(path, "settings.properties")); 
		settingsFile.create();
		
		settingsFile.setId(id);
		settingsFile.setDate(date);
		settingsFile.setOrigin(entry);
		settingsFile.setIn(entry);
		settingsFile.setOut(entry);
		
		settingsFile.setStep(PipelineStep.NONE);
		settingsFile.setState(JobState.READY);
		
		settingsFile.save();
		
		return new BaseWorkspace(path);
	}
	
	@Override
	public Workspace init(ReadEntry entry) throws IOException {		
		return init(entry, UUID.randomUUID().toString(), LocalDateTime.now());
	}

	@Override
	public List<Workspace> items() throws IOException {
		
		final List<Workspace> items = new ArrayList<>();
		File[] directories = new File(WORKSPACES_PATH).listFiles(File::isDirectory);
		for (File dir : directories) {
			final Workspace workspace = new BaseWorkspace(dir.getAbsolutePath());
			if(workspace.settingsFile().exists()) {
				items.add(workspace);
			}
		}
		
		Collections.sort(items, (c1, c2) -> c2.settingsFile().date().compareTo(c1.settingsFile().date()));
		
		return items;
	}

	@Override
	public Workspace get(String id) throws IOException {
		final Optional<Workspace> optWorkspace = items().stream().filter(c -> c.id().equals(id)).findFirst();
		if(!optWorkspace.isPresent())
			throw new IllegalArgumentException(String.format("Workspace with %s not found !", id));
		
		return optWorkspace.get();
	}

}
