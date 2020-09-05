package com.ipci.ngs.datacleaner.commonlib.reads;

public final class PairedReadName extends NameWrapper {
	
	public PairedReadName(final Name origin) {
		super(
			() -> {
				final String regexR = ".*(_R1\\.|_R2\\.).*";
				final String regexRrp = "(_R1|_R2)\\.";
				final String regexRbis = ".*(R1\\.|R2\\.).*";
				final String regexRbisrp = "(R1|R2)\\.";
				final String regexnum = ".*(_1\\.|_2\\.).*";
				final String regexnumrp = "(_1|_2)\\.";				
				final String regexnumbis = ".*(1\\.|2\\.).*";
				final String regexnumbisrp = "(1|2)\\.";
				
				final String value = origin.value();
				
				final String name;
				if(value.matches(regexR)) {
					name = value.replaceAll(regexRrp, ".");
				} else if(value.matches(regexRbis)) {
					name = value.replaceAll(regexRbisrp, ".");
				} else if(value.matches(regexnum)) {
					name = value.replaceAll(regexnumrp, ".");
				} else if(value.matches(regexnumbis)) {
					name = value.replaceAll(regexnumbisrp, ".");
				} else {
					name = value;
				}
				
				return name;
			}
		);
	}
}
