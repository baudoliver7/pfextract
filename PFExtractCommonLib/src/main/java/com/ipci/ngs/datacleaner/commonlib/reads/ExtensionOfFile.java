package com.ipci.ngs.datacleaner.commonlib.reads;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public final class ExtensionOfFile extends NameWrapper {
	
	public ExtensionOfFile(final Name origin) {
		super(
			() -> {
				String baseName = FilenameUtils.removeExtension(origin.value());
				String extension = FilenameUtils.getExtension(origin.value());	
				while(StringUtils.isNotEmpty(FilenameUtils.getExtension(baseName))) {
					extension = String.format("%s.%s", FilenameUtils.getExtension(baseName), extension);
					baseName = FilenameUtils.getBaseName(baseName);
				}
				
				return extension;
			}
		);
	}
}
