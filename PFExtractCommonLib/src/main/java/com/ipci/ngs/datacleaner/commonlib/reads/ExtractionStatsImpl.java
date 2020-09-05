package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class ExtractionStatsImpl implements ExtractionStats {
	
	private final Workspace workspace;
	
	public ExtractionStatsImpl(final Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public List<ReadStats> execute() {
		
		final List<ReadStats> stats = new ArrayList<ReadStats>();
		
		try {
			// input stats
			executeInputStats(stats);
			
			// clip stats
			executeClipStats(stats);
			
			// filter quality stats
			executeFilterQualityStats(stats);
			
			// remove Ns stats
			executeRemoveNsStats(stats);
			
			// apply min length
			executeMinLengthStats(stats);
			
			// paired reads
			executePairedReadStats(stats);
			
			// unmapp gh
			executeUnmappedGHStats(stats);
			
			// map gh
			executeMappedGHStats(stats);
			
			// map 3D7
			executeMap3D7Stats(stats);
			
			// unmap 3D7
			executeUnmap3D7Stats(stats);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		
		return stats;
	}
	
	private void executeInputStats(List<ReadStats> stats) throws IOException {
		
		final Path folderPath = Paths.get(String.format("%s/res/input", workspace.path()));
		if(!Files.exists(folderPath)) {
			return;
		}
		
		final List<File> files = new FilesOfExtension(folderPath, "json").items();
		if(files.isEmpty())
			return;
		
		final ReadEntry originEntry = workspace.settingsFile().origin();
		if(originEntry.type() == ReadEntryType.SE) {
						
			final JSONObject json = jsonStatFromFile(files.get(0));
			stats.add(
				new ReadStatsImpl("R", "Begin", (long)json.get("scaffolds"), 100.0)
			);
		} else {
			final JSONObject jsonR1 = jsonStatFromFile(files.get(0));
			stats.add(
				new ReadStatsImpl("R1", "Begin", (long)jsonR1.get("scaffolds"), 100.0)
			);
			
			final JSONObject jsonR2 = jsonStatFromFile(files.get(0));
			stats.add(
				new ReadStatsImpl("R2", "Begin", (long)jsonR2.get("scaffolds"), 100.0)
			);
		}
	}
	
	private void executeClipStats(List<ReadStats> stats) throws IOException {
		executeStats("clipped", "Clip", stats);
	}
	
	private void executeFilterQualityStats(List<ReadStats> stats) throws IOException {
		executeStats("quality-filtered", "Filter quality", stats);
	}
	
	private void executeRemoveNsStats(List<ReadStats> stats) throws IOException {		
		executeStats("ns-removed", "Remove Ns", stats);
	}
	
	private void executeMinLengthStats(List<ReadStats> stats) throws IOException {		
		executeStats("min-length", "Min length", stats);
	}
	
	private void executePairedReadStats(List<ReadStats> stats) throws IOException {		
		executeStats("paired-read", "Pair read", stats);
	}
	
	private void executeUnmappedGHStats(List<ReadStats> stats) throws IOException {		
		executeStats("mapped-gh", "Unmap GH", stats);
	}
	
	private void executeMappedGHStats(List<ReadStats> stats) throws IOException {		
		for (int i = 0; i < stats.size(); i++) {
			final ReadStats stat = stats.get(i);
			if(stat.step().equals("Unmap GH")) {
				int j = i - 1;
				if(j < 0)
					return;
				
				if(stats.get(j).step().equals("Unmap GH")) {
					j = j - 1;
					
					if(j < 0)
						return;
				}
				
				final ReadStats stat2 = stats.get(j);
				
				final ReadStats inputStats = stats.get(0);
				final long numberOfReads = stat2.numberOfReads() - stat.numberOfReads();
				stats.add(new ReadStatsImpl(stat.name(), "Map GH", numberOfReads, percentOf(numberOfReads, inputStats.numberOfReads())));			
			}
		}
	}
	
	private void executeUnmap3D7Stats(List<ReadStats> stats) throws IOException {
		
		for (int i = 0; i < stats.size(); i++) {
			final ReadStats stat = stats.get(i);
			if(stat.step().equals("Map 3D7")) {

				ReadStats stat2 = null;
				for (int j = i; j >= 0; j--) {
					final ReadStats statj = stats.get(j);
					if(statj.step().equals("Unmap GH") && statj.name().equals(stat.name())) {
						stat2 = stats.get(j);
						break;
					}
				}
				
				if(stat2 == null)
					return;
				
				final ReadStats inputStats = stats.get(0);
				final long numberOfReads = stat2.numberOfReads() - stat.numberOfReads();
				stats.add(new ReadStatsImpl(stat.name(), "Unmap 3D7", numberOfReads, percentOf(numberOfReads, inputStats.numberOfReads())));		
			}
		}
	}
	
	private void executeMap3D7Stats(List<ReadStats> stats) throws IOException {		
		executeStats("output", "Map 3D7", stats);
	}
	
	private void executeStats(String folderName, String step, List<ReadStats> stats) throws IOException {
		
		if(stats.isEmpty())
			return;
		
		final ReadStats inputStats = stats.get(0);
		
		final Path folderPath = Paths.get(String.format("%s/res/%s", workspace.path(), folderName));
		if(!Files.exists(folderPath)) {
			return;
		}
		
		final List<File> files = new FilesOfExtension(folderPath, "json").items();
		if(files.isEmpty())
			return;
		
		final ReadEntry originEntry = workspace.settingsFile().origin();
		if(originEntry.type() == ReadEntryType.SE) {
						
			final JSONObject json = jsonStatFromFile(files.get(0));
			final long numberOfReads = (long)json.get("scaffolds");
			
			stats.add(
				new ReadStatsImpl("R", step, numberOfReads, percentOf(numberOfReads, inputStats.numberOfReads()))
			);
		} else {
			
			final JSONObject jsonR1 = jsonStatFromFile(files.get(0));
			final long numberOfReads1 = (long)jsonR1.get("scaffolds");
			
			stats.add(
				new ReadStatsImpl("R1", step, numberOfReads1, percentOf(numberOfReads1, inputStats.numberOfReads()))
			);
			
			final JSONObject jsonR2 = jsonStatFromFile(files.get(0));
			final long numberOfReads2 = (long)jsonR2.get("scaffolds");
			
			stats.add(
				new ReadStatsImpl("R2", step, numberOfReads2, percentOf(numberOfReads2, inputStats.numberOfReads()))
			);
		}
	}
	
	private JSONObject jsonStatFromFile(File file) throws IOException {
		
		final JSONParser parser = new JSONParser();
		
		try {
			try(FileReader reader = new FileReader(file)) {
				Object obj = parser.parse(reader);
				return (JSONObject)obj;
			}			
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}
	
	private double percentOf(long value, long total) {
		if(total == 0.0)
			return -1.0;
		
		return ((double)value / total) * 100;
	}
}
