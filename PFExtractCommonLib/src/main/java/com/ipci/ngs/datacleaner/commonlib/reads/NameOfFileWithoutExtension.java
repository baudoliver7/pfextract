package com.ipci.ngs.datacleaner.commonlib.reads;

public final class NameOfFileWithoutExtension extends NameWrapper {

	public NameOfFileWithoutExtension(final Name origin) {
		super(
			() -> origin.value().replaceAll("\\." + new ExtensionOfFile(origin).value(), "")
		);
	}
}
