package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public final class UtilNameOfFile {

	private final String filename;
	
	public UtilNameOfFile(File file) {
		filename = file.getName();
	}
	
	public UtilNameOfFile(String filename) {
		this(new File(filename));
	}
	
	public String nameWithoutExtension() {
		String baseName = FilenameUtils.removeExtension(filename);
		while(StringUtils.isNotEmpty(FilenameUtils.getExtension(baseName))) {
			baseName = FilenameUtils.getBaseName(baseName);
		}
		
		return baseName;
	}
	
	public String fullExtension() {
		String baseName = FilenameUtils.removeExtension(filename);
		String extension = FilenameUtils.getExtension(filename);	
		while(StringUtils.isNotEmpty(FilenameUtils.getExtension(baseName))) {
			extension = String.format("%s.%s", FilenameUtils.getExtension(baseName), extension);
			baseName = FilenameUtils.getBaseName(baseName);
		}
		
		return extension;
	}
	
	public String extension() {
		return FilenameUtils.getExtension(filename);
	}
}
