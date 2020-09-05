package com.ipci.ngs.datacleaner.commonlib.utilities;

public final class JavaProcess {
	
	private final String[] command;
	private final String[] javaArguments;
	
	public JavaProcess(final String[] javaArguments, final String[] command) {
		this.javaArguments = javaArguments;
		this.command = command;
	}
	
	public void exec() {
		
		final String[] javaCommand = new String[3 + command.length + javaArguments.length];
		 
		 javaCommand[0] = "java";
		 int i = 0;
		 for (String arg : javaArguments) {
			 javaCommand[++i] = arg;
		 }
		 
		 javaCommand[++i] = "-classpath";
		 javaCommand[++i] = "./lib/*";
		 
		 for (int j = 1; j <= command.length; j++) {
			 javaCommand[i + j] = command[j - 1];
		 }
		 
		 new BaseProcess(javaCommand).exec();
	}
}
