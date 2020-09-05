package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ReadEntriesFromDirectory implements ReadEntries {

	private final List<File> files;

	public ReadEntriesFromDirectory(final List<File> files) {
		this.files = files;		
	}
	
	public ReadEntriesFromDirectory(final String absolutePath) throws IOException {
		this(new FilesOfExtension(absolutePath, "fastq", "fq", "gz").items());
	}
	
	public List<ReadEntry> items() throws IOException {
		
		final List<ReadEntry> items = new ArrayList<>();
		final List<File> filesCopy = files.stream().collect(Collectors.toList());
		
		while (!filesCopy.isEmpty()) {
			
			final File file = filesCopy.get(0);
			filesCopy.remove(file);
			
			final Name nameOfFile = new NameOfFile(file);		
			final Name nameOfFileWithoutExtension = new NameOfFileWithoutExtension(nameOfFile);
			final Name extensionOfFile = new ExtensionOfFile(nameOfFile);
			
			if(!nameOfFileWithoutExtension.value().endsWith("1") && !nameOfFileWithoutExtension.value().endsWith("2")) {
				items.add(new SingleRead(file)); 				
			} else {
				// check other one
				final String root;
				if(nameOfFileWithoutExtension.value().endsWith("1")) {
					root = nameOfFileWithoutExtension.value().replaceAll("1$", "2"); 										
				} else {
					root = nameOfFileWithoutExtension.value().replaceAll("2$", "1"); 
				}
				
				final Optional<File> optFile = filesCopy.stream().filter(c -> new NameOfFileWithoutExtension(new NameOfFile(c)).value().startsWith(root) && new ExtensionOfFile(new NameOfFile(c)).value().equals(extensionOfFile.value())).findFirst();
				
				if(optFile.isPresent()) {
					items.add(new PairRead(file, optFile.get()));
					filesCopy.remove(optFile.get());
				} else {
					items.add(new SingleRead(file));
				}
			}			
		}
		
		return items;
	}

	public static String twoStrings(String s1, String s2){

	    HashSet<Character> stringOne =  new HashSet<>();
	    HashSet<Character> stringTwo = new HashSet<>();  
	    int stringOneLength = s1.length();
	    int stringTwoLength = s2.length();
	    for(int i=0; i<stringOneLength || i<stringTwoLength; i++) {
	        if(i < stringOneLength)
	            stringOne.add(s1.charAt(i));
	        if(i < stringTwoLength)
	            stringTwo.add(s2.charAt(i));
	    }
	    stringOne.retainAll(stringTwo);

	    return stringOne.toString();
	}	
}
