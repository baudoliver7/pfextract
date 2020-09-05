package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public final class WorkspaceReferencesImpl implements WorkspaceReferences {

	@SuppressWarnings("unused")
	private final Workspace workspace;
	
	public WorkspaceReferencesImpl(final Workspace workspace) {
		this.workspace = workspace;
	}
	
	@Override
	public Set<WorkspaceReference> items() throws IOException {
		
		final Path resPath = Paths.get(FilenameUtils.concat(System.getProperty("user.dir"), "reference"));
		
		if(!resPath.toFile().exists())
			return new HashSet<>();
		
		final Set<WorkspaceReference> files = new HashSet<>();
		
		try(Stream<Path> paths = Files.walk(resPath)) {
			paths.forEach(path -> {
				List<File> items;
				try {
					items = new FilesOfExtension(path.toString(), "fasta").items();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}				
				
				for (File file : items) {
					double weightInGo = file.length() / (double)(1024 * 1024 * 1024);
					files.add(new WorkspaceReferenceImpl(file.getName(), file.getPath(), weightInGo));
				}
			});
		}
		
		
		
		return files;
	}

}
