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

public final class FilesWithoutExtension implements CoupleOfFiles {

	private final String[] extensions;
	private final Path path;
	
	public FilesWithoutExtension(final String path, final String... extensions) {
		this(Paths.get(path), extensions);
	}
	
	public FilesWithoutExtension(final Path path, final String... extensions) {
		this.extensions = extensions;
		this.path = path;
	}
	
	@Override
	public List<File> items() throws IOException {
		final List<File> files = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(path)) {	

			Predicate<Path> filter = c -> true;
			for (String extension : extensions) {
				filter = filter.and(c -> !c.toFile().getName().endsWith(extension));
			};
			
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
