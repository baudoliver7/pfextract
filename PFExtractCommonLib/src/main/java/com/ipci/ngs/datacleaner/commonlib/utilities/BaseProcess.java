package com.ipci.ngs.datacleaner.commonlib.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class BaseProcess {

	private final String[] command;
	
	public BaseProcess(final String[] command) {
		this.command = command;
	}
	
	public void exec() {
		try {
	         ProcessBuilder pb = new ProcessBuilder(command);
	         pb = pb.redirectErrorStream(true);
	         final Process p = pb.start();
	         BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
	         String line;
            while((line=br.readLine())!=null){
              System.err.println(line);
            }
            
            int exitVal = p.waitFor(); 
            if(exitVal != 0) {
            	throw new RuntimeException(String.format("Error : program exited with code %d with command '%s'", p.exitValue(), String.join(" ", command))); 
            }            	          
	      } catch (Exception ex) {
	         System.err.println(ex);
	         throw new RuntimeException(ex); 
	      }
	}
}
