package com.ipci.ngs.datacleaner.commonlib.utilities;

public final class JavaJarProcess {
	
	private final String[] command;
	private final String[] javaArguments;
	
	public JavaJarProcess(final String[] javaArguments, final String[] command) {
		this.javaArguments = javaArguments;
		this.command = command;
	}
	
	public void exec() {
		
		final String[] javaCommand = new String[3 + command.length + javaArguments.length - 1];
		 
		 javaCommand[0] = "java";
		 int i = 0;
		 for (String arg : javaArguments) {
			 javaCommand[++i] = arg;
		 }
		 
		 javaCommand[++i] = "-jar";
		 javaCommand[++i] = "./lib/" + command[0];
		 
		 for (int j = 2; j <= command.length; j++) {
			 javaCommand[i + j - 1] = command[j - 1];
		 }
		 
		 new BaseProcess(javaCommand).exec();
	}
}
