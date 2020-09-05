package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class FilesOrdered implements CoupleOfFiles {

	private final CoupleOfFiles origin;
	
	public FilesOrdered(final CoupleOfFiles origin) {
		this.origin = origin;
	}
	
	@Override
	public List<File> items() throws IOException {
		return origin.items()
					 .stream()
					 .sorted(Comparator.comparing(File::getName))
					 .collect(Collectors.toList());
	}

}
