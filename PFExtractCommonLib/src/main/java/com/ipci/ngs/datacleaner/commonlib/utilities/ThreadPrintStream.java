package com.ipci.ngs.datacleaner.commonlib.utilities;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public final class ThreadPrintStream extends PrintStream {

	private final ThreadLocal<PrintStream> out;
	
	public ThreadPrintStream() {
		super(new ByteArrayOutputStream(0));
		
		out = new ThreadLocal<>();
	}
	
	public void setOut(PrintStream out) {
		this.out.set(out);
	}
	
	public PrintStream getOut() {
		return this.out.get();
	}
	
	@Override 
	public boolean checkError() {
		return getOut().checkError();
	}

	@Override 
	public void write(byte[] buf, int off, int len) {
		getOut().write(buf, off, len);
	}

	@Override 
	public void write(int b) { 
		getOut().write(b); 
	}

	@Override 
	public void flush() { 
		getOut().flush(); 
	}
	
	@Override 
	public void close() { 
		getOut().close(); 
	}
}
