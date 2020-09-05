package com.ipci.ngs.datacleaner.server.business;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import com.ipci.ngs.datacleaner.server.agent.WorkerAgent;

public final class LoggingOutputStream extends OutputStream {

    private final String logPath;
    private final WorkerAgent worker;
    private final StringBuffer text;
    
    public LoggingOutputStream(final String logPath, final String logDir, final WorkerAgent worker) throws IOException {
    	
    	this.logPath = logPath;    	
        this.worker = worker;
        final File dir = new File(logDir);
        final File file = new File(logPath);
        
        if(!dir.exists()) {
        	dir.mkdir();
        } else {
        	if(file.exists())
        		file.delete(); // reset log
        }
        
        if(!file.exists()) {
        	file.createNewFile();
        }
        
        this.text = new StringBuffer();
    }
	
	@Override
	public void write(int b) throws IOException {	
        
        try(PrintWriter printText = new PrintWriter(new FileWriter(logPath, true))){
        	printText.print((char)b);
        }       
        
 		text.append(String.valueOf((char)b));
        
	}
	
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		super.write(b, off, len);
		
		worker.notifyLog(text.toString());
		text.setLength(0);
	}

}
