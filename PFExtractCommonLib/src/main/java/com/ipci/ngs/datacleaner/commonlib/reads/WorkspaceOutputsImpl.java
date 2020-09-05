package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public final class WorkspaceOutputsImpl implements WorkspaceOutputs {

	private final Workspace workspace;
	
	public WorkspaceOutputsImpl(final Workspace workspace) {
		this.workspace = workspace;
	}
	
	@Override
	public List<WorkspaceOutput> items() throws IOException {
		
		final Path resPath = Paths.get(FilenameUtils.concat(workspace.path(), "res"));
		if(!resPath.toFile().exists())
			return Arrays.asList();
		
		final List<WorkspaceOutput> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(resPath)) {	

			paths.forEach(path -> {
				if(path.toFile().isDirectory() && !path.toFile().getName().equals("res")) { 
					List<File> items;
					try {
						items = new FilesOfExtension(path.toString(), "fastq", "fq", "gz", "bam", "sam").items();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
					for (File file : items) {
						double weightInGo = file.length() / (double)(1024 * 1024 * 1024);
						Instant i = Instant.ofEpochMilli(file.lastModified());
						ZonedDateTime z = ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
						files.add(new WorkspaceOutputImpl(path.toFile().getName(), file.getName(), file.getAbsolutePath(), weightInGo, z));
					}
				}				
			});
		} 
		
		return files;
	}

}
