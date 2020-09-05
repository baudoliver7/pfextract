package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FilesOfExtension implements CoupleOfFiles {

	private final String[] extensions;
	private final Path path;
	
	public FilesOfExtension(final String path) {
		this(path, "*");
	}
	
	public FilesOfExtension(final String path, final String... extensions) {
		this(Paths.get(path), extensions);
	}
	
	public FilesOfExtension(final Path path) {
		this(path, "*");
	}
	
	public FilesOfExtension(final Path path, final String... extensions) {
		this.extensions = extensions;
		this.path = path;
	}
	
	@Override
	public List<File> items() throws IOException {
		final List<File> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(path)) {	

			Predicate<Path> filter;
			if(!(extensions.length == 1 && extensions[0].equals("*"))) {
				filter = c -> false;
				for (String extension : extensions) {
					filter = filter.or(c -> c.toFile().getName().endsWith(extension));
				}
			} else {
				filter = c -> true;
			}
			
			filter = filter.and(c -> c.toFile().isFile());

			files.addAll(
			   paths.filter(filter)
			        .map(c -> c.toFile())
		         	.sorted()
		         	.collect(Collectors.toList())
			);
		} 
		
		return files;
	}

}
