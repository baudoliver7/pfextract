package com.ipci.ngs.datacleaner.server.agent;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

import com.ipci.ngs.datacleaner.commonlib.agent.ConversationID;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommandItem;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotificationImpl;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotificationImpl;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotificationImpl;
import com.ipci.ngs.datacleaner.commonlib.reads.CoupleOfFiles;
import com.ipci.ngs.datacleaner.commonlib.reads.ExtensionOfFile;
import com.ipci.ngs.datacleaner.commonlib.reads.FilesOfExtension;
import com.ipci.ngs.datacleaner.commonlib.reads.FilesWithoutExtension;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFile;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFileWithoutExtension;
import com.ipci.ngs.datacleaner.commonlib.reads.PairRead;
import com.ipci.ngs.datacleaner.commonlib.reads.PairedReadName;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntryMetadata;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntryType;
import com.ipci.ngs.datacleaner.commonlib.reads.SingleRead;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.utilities.JavaJarProcess;
import com.ipci.ngs.datacleaner.commonlib.utilities.JavaProcess;
import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;
import com.ipci.ngs.datacleaner.commonlib.utilities.SettingsFile;
import com.ipci.ngs.datacleaner.commonlib.utilities.ThreadPrintStream;
import com.ipci.ngs.datacleaner.commonlib.utilities.TimeUtils;
import com.ipci.ngs.datacleaner.server.business.LoggingOutputStream;
import com.ipci.ngs.datacleaner.server.business.WorkspacesImpl;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public final class WorkerAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String RES_FOLDER = "res";
	public static final String FASTQ_FOLDER = "fastq";
	public static final String SAM_FOLDER = "sam";
	public static final String REF = "reference";
	public static final String CLIPPED = "clipped";
	public static final String QUALITY_FILTERED = "quality-filtered";
	public static final String NS_REMOVED = "ns-removed";
	public static final String MIN_LENGTH = "min-length";
	public static final String PAIRED_READ = "paired-read";
	public static final String MAPPED = "mapped";
	public static final String MAPPED_GH = "mapped-gh";
	public static final String MAPPED_3D7 = "mapped-3d7";
	public static final String ADD_READGROUPS = "add-readgroups";
	public static final String SORT_BAM = "sort-bam";
	public static final String MARK_DUPLICATED = "mark-duplicated";
	
	private final transient Logger log = Logger.getLogger("log");
	private PipelineCommand command;
	private SettingsFile workspaceSettings;
	private ReadEntry sequenceFile;
	private Workspace workspace;
	
	private int progressPercent;
	private String action;
	private DFAgentDescription dfd;
	
	private static final Properties settingsFile;
	private static final PrintStream standardOut;
	private static final PrintStream standardErr;
	
	static {
		try(InputStream stream = new FileInputStream("config/settings.properties")){
			settingsFile = new Properties();
			settingsFile.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		// Save the existing System.out
	    standardOut = System.out;
	    standardErr = System.err;

	    // Create a ThreadPrintStream and install it as System.out
	    ThreadPrintStream threadOut = new ThreadPrintStream();
	    System.setOut(threadOut);
	    
	    ThreadPrintStream threadErr = new ThreadPrintStream();
	    System.setErr(threadErr);

	    // Use the original System.out as the current thread's System.out
	    threadOut.setOut(standardOut);
	    threadErr.setOut(standardErr);
	}
	
	@Override
	protected  void setup() {
		try {
        	// create the agent description of itself
        	final ServiceDescription sd = new ServiceDescription();
            sd.setType("worker");
            sd.setName("worker-agent");
            
            command = (PipelineCommand)getArguments()[0];
            workspace = new WorkspacesImpl().get(command.workspace().id());
            workspaceSettings = workspace.settingsFile();
            sequenceFile = workspace.settingsFile().out();
            
            dfd = new DFAgentDescription();
            dfd.setName(getAID());
            dfd.addServices(sd);
			DFService.register(this, dfd);
			
			// add a Behaviour to process incoming messages
			addBehaviour(new WorkerBehaviour(this));			
			
			addBehaviour
			( 
				new OneShotBehaviour() {
                      /**
					 * 
					 */
					private static final long serialVersionUID = 1L; 

					public void action() {						
						runPipeline();						
                    }
                } 
			);			
			
		} catch (FIPAException | IOException e) {  
			log.trace(e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	protected void takeDown() {		
		try {
			informImFinished();
			DFService.deregister(this, dfd);
		} catch (FIPAException e) {
			log.trace(e.getLocalizedMessage(), e); 
		}
	}

	public void notifyLog(String fragment) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);		
		msg.setContentObject(new PipelineLogNotificationImpl(workspace.id(), fragment)); 		
		msg.setConversationId(ConversationID.NOTIFY_LOG.name());
		msg.addReceiver(hostAID()); 		
		send(msg);
	}
	
	private AID hostAID() {
		return new AID("host-agent", AID.ISLOCALNAME);
	}
	
	public void notifyProgress(int progressPercent) throws IOException {
		notifyProgress(Arrays.asList(), action, progressPercent); 
	}

	public void notifyProgress(AID client, int progressPercent) throws IOException {
		notifyProgress(client, action, progressPercent); 
	}
	
	public void notifyProgress(String action, int progressPercent) throws IOException {	
		notifyProgress(Arrays.asList(), action, progressPercent);
	}
	
	public void notifyProgress(AID client, String action, int progressPercent) throws IOException {	
		notifyProgress(Arrays.asList(client), action, progressPercent);
	}
	
	public void notifyProgress(List<AID> clients, String action, int progressPercent) throws IOException {		
		
		this.action = action;
		this.progressPercent = progressPercent;
		
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);		
		msg.setContentObject(new PipelineProgressNotificationImpl(workspace.id(), action, progressPercent)); 		
		msg.setConversationId(ConversationID.NOTIFY_PROGRESS.name());
		if(clients.isEmpty())
			msg.addReceiver(hostAID());
		else {
			for (AID aid : clients) {
				msg.addReceiver(aid);
			}
		}
		
		send(msg);
		
		if(action.contains("error")) {
			notifyWorkspaceStatus(clients, JobState.FAILED);
		} else {
			if(progressPercent >= 0 && progressPercent < 100) {
				notifyWorkspaceStatus(clients, JobState.RUNNING);
			} else {
				notifyWorkspaceStatus(clients, JobState.FINISHED);
			}
		}			
	}
	
	public void notifyWorkspaceStatus(JobState state) throws IOException {
		notifyWorkspaceStatus(Arrays.asList(), state);
	}
	
	public void notifyWorkspaceStatus(List<AID> clients, JobState state) throws IOException {
		
		workspace.setState(state);	
		
		final String status;
		
		if(state == JobState.RUNNING) {
			status = String.format("%s (%d", state.name(), progressPercent) + "%)";
		} else {
			status = state.name();
		}
		
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);		
		msg.setContentObject(new WorkspaceStatusNotificationImpl(workspace.id(), status, state)); 		
		msg.setConversationId(ConversationID.NOTIFY_WORKSPACE_STATUS.name());
		if(clients.isEmpty())
			msg.addReceiver(hostAID());
		else {
			for (AID aid : clients) {
				msg.addReceiver(aid);
			}
		}
		
		send(msg);
	}
	
	public void runPipeline() {
			
		new Thread(() -> {

			long startTime = System.currentTimeMillis();
			
			try {				
				
				final String logPath = FilenameUtils.concat(workspace.path(), "logs/log.txt");
				final String logDir = FilenameUtils.concat(workspace.path(), "logs");
				PrintStream printStream = new PrintStream(new LoggingOutputStream(logPath, logDir, this));
				((ThreadPrintStream)System.out).setOut(printStream);
				((ThreadPrintStream)System.err).setOut(printStream);
			
				System.err.println("-------------------------- Starting ---------------------------------");
				
				notifyProgress("Preparing environnement", 0);
				
				if(command.hasStep(PipelineStep.INDEX_REFERENCE)) {
					
					displayStepTitle("Indexing reference");
					
					List<File> references = new FilesOfExtension(FilenameUtils.concat(System.getProperty("user.dir"), "reference"), "fasta").items();
					if(references.isEmpty())
						throw new IllegalArgumentException("There isn't reference file to index ! Please fasta reference file in reference folder !");
					
					final int step = (int)(100 / (double)references.size());
					for (File reference : references) {
						notifyProgress(String.format("Indexing %s", reference.getName()), progressPercent);
						indexReference(reference.getAbsolutePath());
						notifyProgress(String.format("Finishing indexing %s", reference.getName()), step + progressPercent);
					}
					
					notifyProgress("Finished", 100);
				} else if(command.hasStep(PipelineStep.VISUALIZE)){
					
					displayStepTitle("Generate report for visualization");
					
					final String path = (String)command.getCommandOf(PipelineStep.VISUALIZE).values().get(0);
					notifyProgress(String.format("Generating visualization for %s", new File(path).getName()), progressPercent);
					final File file = generateVisualization(path);					
					
					notifyProgress("Opening in browser", 85);
					System.err.println("We are opening in browser");
					
					
					try(InputStream stream = new FileInputStream("config/jade-container.properties")) {
						final Properties jadeConfig = new Properties();
						jadeConfig.load(stream);
						final String serverIp = jadeConfig.getProperty("host");
						if(serverIp.trim().equals("127.0.0.1")) {
							Desktop desktop = Desktop.getDesktop();
				        	desktop.browse(file.toURI());
						} else {
							final byte[] data = convertFileToBytes(file);
							sendVisualization(data);
						}
						
						notifyProgress("Finished", 100);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					
				} else {
				
					final int step = (int)(100 / (double)command.count());
					
					if(workspaceSettings.step() != PipelineStep.NONE) {
						throw new IllegalArgumentException("The current workspace is already used ! You must begin with a new workspace.");
					}
					
					if((command.hasStep(PipelineStep.MAPPED_3D7) || command.hasStep(PipelineStep.UNMAPPED_GH))) {
						displayStepTitle("Check that references are indexed");
						checkThatReferencesAreAllIndexed();
					}
					
					final String ghPath = FilenameUtils.concat(System.getProperty("user.dir"), "reference/gh/");
					final CoupleOfFiles ghDir = new FilesOfExtension(ghPath, "fasta");
					
					if(ghDir.items().isEmpty()) {
						throw new IllegalArgumentException(String.format("You must add an human genome reference at directory %s !", ghPath));
					}
					
					final String ghFilePath = ghDir.items().get(0).getAbsolutePath();
					
					final String trois3d7Path = FilenameUtils.concat(System.getProperty("user.dir"), "reference/3d7/");
					final CoupleOfFiles trois3d7Dir = new FilesOfExtension(trois3d7Path, "fasta");
					
					if(trois3d7Dir.items().isEmpty()) {
						throw new IllegalArgumentException(String.format("You must add an 3d7 reference at directory %s !", trois3d7Path));
					}
					
					final String trois3d7FilePath = trois3d7Dir.items().get(0).getAbsolutePath();
					
					final Path inputDirPath = Paths.get(fullPath("input"));
					if(!Files.exists(inputDirPath)) {
						Files.createDirectories(inputDirPath);
					}
					
					final List<File> fastqFiles = workspaceSettings.origin().files();
					for (File file : fastqFiles) {
						stats(file.getAbsolutePath(), String.format("%s/%s.json", inputDirPath, new NameOfFileWithoutExtension(new NameOfFile(file.getName())).value()));
					}
					
					if(command.hasStep(PipelineStep.CLIP)) {
						
						displayStepTitle("Clip");
						
						notifyProgress("Clipping", progressPercent + step);
					
						final PipelineCommandItem item = command.getCommandOf(PipelineStep.CLIP);
						clipCore((int)item.values().get(0), (int)item.values().get(1)); 
					}
					
					if(command.hasStep(PipelineStep.FILTER_QUALITY)) {
						
						displayStepTitle("Filter quality");
						
						notifyProgress("Filtering quality", progressPercent + step);
					
						final PipelineCommandItem item = command.getCommandOf(PipelineStep.FILTER_QUALITY);
						filterWithQualityCore((int)item.values().get(0));
					}
					
					if(command.hasStep(PipelineStep.REMOVE_NS)) {
						
						displayStepTitle("Remove Ns");
						
						notifyProgress("Removing NS", progressPercent + step);
						
						final PipelineCommandItem item = command.getCommandOf(PipelineStep.REMOVE_NS);
						removeNsCore((int)item.values().get(0));
					}
					
					if(command.hasStep(PipelineStep.MIN_LENGTH)) {
						
						displayStepTitle("Apply min length");
						
						notifyProgress("Applying min length", progressPercent + step);
						
						final PipelineCommandItem item = command.getCommandOf(PipelineStep.MIN_LENGTH);
						applyMinLength((int)item.values().get(0));
					}
					
					if(command.hasStep(PipelineStep.PAIRED_READS) && sequenceFile.type() == ReadEntryType.PE) {
						
						displayStepTitle("Pair reads");
						
						notifyProgress("Pairing reads", progressPercent + step);
					
						pairedReadsCore();
					}
					
					if(command.hasStep(PipelineStep.UNMAPPED_GH)) {
						
						displayStepTitle("Unmapp on GH reference");
						
						notifyProgress("Unmapping on GH reference", progressPercent + step);
					
						map(ghFilePath, MAPPED_GH);
						
						notifyProgress("Unmapping on GH reference - Converting BAM file to FASTQ", progressPercent);
						
						final String unmapfilePath = workspaceSettings.out().first().getAbsolutePath();
						convertBamToFastqCore(unmapfilePath, FilenameUtils.getFullPath(unmapfilePath));
					}
					
					if(command.hasStep(PipelineStep.MAPPED_3D7)) {
						
						displayStepTitle("Map on 3D7 reference");
						
						notifyProgress("Mapping on 3D7 reference", progressPercent + step);
					
						map(trois3d7FilePath, MAPPED_3D7);
					}
					
					if(command.hasStep(PipelineStep.MARK_DUPLICATED)) {		
						
						displayStepTitle("Mark duplicates");
						
						notifyProgress("Mark duplicated - Add read groups", progressPercent + step);
					
						addReadGroupsCore();
						
						notifyProgress("Mark duplicated - Sort BAM File", progressPercent);
						
						sortBamCore();
						
						notifyProgress("Mark duplicated - marking", progressPercent);
						
						markDuplicatedReadsCore();						
					}
					
					if(command.hasStep(PipelineStep.MAPPED_3D7) || command.hasStep(PipelineStep.MARK_DUPLICATED)) {
						notifyProgress("Converting BAM file to FASTQ", progressPercent);
						
						final String mapfilePath = workspaceSettings.out().first().getAbsolutePath();
						convertBamToFastqCore(mapfilePath, FilenameUtils.getFullPath(mapfilePath));
					}
					
					notifyProgress("Finalizing", progressPercent + step - 1);
					
					final String outputDirPath = fullPath("output");
					
					if(!Paths.get(outputDirPath).toFile().exists()) {
						Files.createDirectory(Paths.get(outputDirPath));
					}
					
					final List<File> outfastqFiles = workspaceSettings.out().files();
					final File outputDir = new File(outputDirPath);
					for (File file : outfastqFiles) {
						FileUtils.copyFileToDirectory(file, outputDir);
						stats(String.format("%s/%s", outputDir, file.getName()));
					}
					
					final Path previousWorkDir = outfastqFiles.get(0).getParentFile().toPath();
					removeIntermediateFiles(previousWorkDir);
				}								
				
				notifyProgress("Finished", 100);				
								
			} catch(Exception e) {
				log.trace(e.getLocalizedMessage(), e);
				try {
					System.err.println(e.getLocalizedMessage());
					notifyProgress("Finished with error", 100);
				} catch (IOException e1) {
					log.trace(e1.getLocalizedMessage(), e1);
				}		
			} finally {	
				
				long endTime = System.currentTimeMillis();
				long totalTime = endTime-startTime;			
				System.err.println("-------------------------- Finished ---------------------------------" + System.getProperty("line.separator"));
				System.err.println("Total elapsed time in execution is : " + TimeUtils.toFormattedString(totalTime, TimeUnit.MILLISECONDS));
				System.out.close();
				System.err.close();			
				doDelete();			
			}
		}).start();
	}
	
	public void clipCore (int left, int right) {
		
		validateStep(PipelineStep.CLIP);
		
		final String suffix = String.format("%sD%sF", right, left);
		
		if(sequenceFile.type() == ReadEntryType.SE) {
			final String originFileName = workspace.settingsFile().origin().first().getName();
			final String fullFileName = sequenceFile.files().get(0).getAbsolutePath();
			final String newFullFileName = targetFileName(originFileName, fullFileName, CLIPPED, suffix);
			
			String[] args = {
								"jgi.BBDuk", String.format("in=%s", fullFileName), String.format("out=%s", newFullFileName), 
								String.format("ftl=%s", left), String.format("ftr2=%s", right), "overwrite=true"
							}; 
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName);
			save11Step(fullFileName, newFullFileName, PipelineStep.CLIP);
		} else {
			final String fullFileName1 = sequenceFile.files().get(0).getAbsolutePath();
			final String originFileName1 = workspace.settingsFile().origin().first().getName();
			final String newFullFileName1 = targetFileName(originFileName1, fullFileName1, CLIPPED, suffix);
			
			final String fullFileName2 = sequenceFile.files().get(1).getAbsolutePath();
			final String originFileName2 = workspace.settingsFile().origin().second().getName();
			final String newFullFileName2 = targetFileName(originFileName2, fullFileName2, CLIPPED, suffix);
			
			String[] args = {
								"jgi.BBDuk", String.format("in=%s", fullFileName1), String.format("in2=%s", fullFileName2), 
								String.format("out=%s", newFullFileName1), String.format("out2=%s", newFullFileName2), 
								String.format("ftl=%s", left), String.format("ftr2=%s", right), "overwrite=true"
							}; 
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName1);
			stats(newFullFileName2);
			
			save22Step(fullFileName1,fullFileName2, newFullFileName1, newFullFileName2, PipelineStep.CLIP);
		}
	}
	
	public void filterWithQualityCore (int level) {
				
		validateStep(PipelineStep.FILTER_QUALITY);
		
		final List<File> files = workspaceSettings.out().files();
		
		final String suffix = String.format("Q%s", level);
		
		if(sequenceFile.type() == ReadEntryType.SE) {
			final String originFileName = workspace.settingsFile().origin().first().getName();
			final String fullFileName = files.get(0).getAbsolutePath();
			final String newFullFileName = targetFileName(originFileName, fullFileName, QUALITY_FILTERED, suffix);
			
			String[] args = {
								"jgi.BBDuk", String.format("in=%s", fullFileName), String.format("out=%s", newFullFileName), 
								String.format("maq=%s", level), "overwrite=true"
							}; 
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName);
			
			save11Step(fullFileName, newFullFileName, PipelineStep.FILTER_QUALITY);
		} else {
			final String fullFileName1 = files.get(0).getAbsolutePath();
			final String originFileName1 = workspace.settingsFile().origin().first().getName();
			final String newFullFileName1 = targetFileName(originFileName1, fullFileName1, QUALITY_FILTERED, suffix);
			
			final String fullFileName2 = files.get(1).getAbsolutePath();
			final String originFileName2 = workspace.settingsFile().origin().second().getName();
			final String newFullFileName2 = targetFileName(originFileName2, fullFileName2, QUALITY_FILTERED, suffix);
			
			String[] args = {
								"jgi.BBDuk", String.format("in=%s", fullFileName1), String.format("in2=%s", fullFileName2), 
								String.format("out=%s", newFullFileName1), String.format("out2=%s", newFullFileName2), 
								String.format("maq=%s", level), "overwrite=true"
							};
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName1);
			stats(newFullFileName2);
			
			save22Step(fullFileName1, fullFileName2, newFullFileName1, newFullFileName2, PipelineStep.FILTER_QUALITY);
		}
		
		final Path previousWorkDir = files.get(0).getParentFile().toPath();
		removeIntermediateFiles(previousWorkDir);
	}
	
	public void removeNsCore (int max) {
		
		validateStep(PipelineStep.REMOVE_NS);
		
		final List<File> files = workspaceSettings.out().files();
		
		final String suffix = String.format("N%s", max);
		
		if(sequenceFile.type() == ReadEntryType.SE) {
			final String fullFileName = files.get(0).getAbsolutePath();
			final String originFileName = workspace.settingsFile().origin().first().getName();
			final String newFullFileName = targetFileName(originFileName, fullFileName, NS_REMOVED, suffix);
			
			String[] args = {"jgi.BBDuk", String.format("in=%s", fullFileName), String.format("out=%s", newFullFileName), String.format("maxns=%s", max), "overwrite=true"}; 
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName);
			
			save11Step(fullFileName, newFullFileName, PipelineStep.REMOVE_NS);
			
		} else {
			final String fullFileName1 = files.get(0).getAbsolutePath();
			final String originFileName1 = workspace.settingsFile().origin().first().getName();
			final String newFullFileName1 = targetFileName(originFileName1, fullFileName1, NS_REMOVED, suffix);
			
			final String fullFileName2 = files.get(1).getAbsolutePath();
			final String originFileName2 = workspace.settingsFile().origin().second().getName();
			final String newFullFileName2 = targetFileName(originFileName2, fullFileName2, NS_REMOVED, suffix);
			
			String[] args = {"jgi.BBDuk", String.format("in=%s", fullFileName1), String.format("in2=%s", fullFileName2), String.format("out=%s", newFullFileName1), String.format("out2=%s", newFullFileName2), String.format("maxns=%s", max), "overwrite=true"};
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName1);
			stats(newFullFileName2);
			
			save22Step(fullFileName1, fullFileName2, newFullFileName1, newFullFileName2, PipelineStep.REMOVE_NS);
		}
		
		final Path previousWorkDir = files.get(0).getParentFile().toPath();
		removeIntermediateFiles(previousWorkDir);
	}
	
	public void applyMinLength (int length) {
		
		validateStep(PipelineStep.MIN_LENGTH);
		
		final List<File> files = workspaceSettings.out().files();
		
		final String suffix = String.format("ML%s", length);
		
		if(sequenceFile.type() == ReadEntryType.SE) {
			final String fullFileName = files.get(0).getAbsolutePath();
			final String originFileName = workspace.settingsFile().origin().first().getName();
			final String newFullFileName = targetFileName(originFileName, fullFileName, MIN_LENGTH, suffix);
			
			String[] args = {"jgi.BBDuk", String.format("in=%s", fullFileName), String.format("out=%s", newFullFileName), String.format("minlen=%s", length), "overwrite=t"}; 
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName);
			
			save11Step(fullFileName, newFullFileName, PipelineStep.MIN_LENGTH);
			
		} else {
			final String fullFileName1 = files.get(0).getAbsolutePath();
			final String originFileName1 = workspace.settingsFile().origin().first().getName();
			final String newFullFileName1 = targetFileName(originFileName1, fullFileName1, MIN_LENGTH, suffix);
			
			final String fullFileName2 = files.get(1).getAbsolutePath();
			final String originFileName2 = workspace.settingsFile().origin().second().getName();
			final String newFullFileName2 = targetFileName(originFileName2, fullFileName2, MIN_LENGTH, suffix);
			
			String[] args = {"jgi.BBDuk", String.format("in=%s", fullFileName1), String.format("in2=%s", fullFileName2), String.format("out=%s", newFullFileName1), String.format("out2=%s", newFullFileName2), String.format("minlen=%s", length), "overwrite=t"};
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName1);
			stats(newFullFileName2);
			
			save22Step(fullFileName1, fullFileName2, newFullFileName1, newFullFileName2, PipelineStep.MIN_LENGTH);
		}
		
		final Path previousWorkDir = files.get(0).getParentFile().toPath();
		removeIntermediateFiles(previousWorkDir);
	}
	
	public void pairedReadsCore() {
		
		final List<File> files = workspaceSettings.out().files();
		
		if(sequenceFile.files().size() != files.size())
			throw new IllegalArgumentException("Number of files in folder must be same as sequence files number !"); 
		
		final ReadEntry read;
		if(files.size() == 1) {
			read = new SingleRead(files.get(0));
		} else {				
			read = new PairRead(files.get(0), files.get(1)); 
		}
		
		pairedReadsCore(read);
		
		final Path previousWorkDir = files.get(0).getParentFile().toPath();
		removeIntermediateFiles(previousWorkDir);
	}
	
	public File[] pairedReadsCore(ReadEntry readEntry) {
		
		validateStep(PipelineStep.PAIRED_READS);
		
		final List<File> filesToOpen = new ArrayList<>();
		
		final String[] args;
		final String singletonFileName;
		
		final Map<String, String> inOuts = new HashMap<>();
		
		if(readEntry.type() == ReadEntryType.SE) {
			final File file = readEntry.files().get(0);
			final String fullFileName = file.getAbsolutePath();
			final String originFileName = workspace.settingsFile().origin().first().getName();
			final String newFullFileName = targetFileName(originFileName, fullFileName, PAIRED_READ, "P"); 
			singletonFileName = targetFileName(originFileName, newFullFileName, PAIRED_READ, "PS");
			
			args = new String[] {"jgi.SplitPairsAndSingles", String.format("in=%s", fullFileName), String.format("out=%s", newFullFileName), String.format("outs=%s", singletonFileName), "repair"};
			
			filesToOpen.add(new File(newFullFileName));
			filesToOpen.add(new File(singletonFileName));
			
			inOuts.put(fullFileName, newFullFileName);
			
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName);
		} else {
			final File file1 = readEntry.files().get(0);
			final String fullFileName1 = file1.getAbsolutePath();
			final String originFileName1 = workspace.settingsFile().origin().first().getName();
			final String newFullFileName1 = targetFileName(originFileName1, fullFileName1, PAIRED_READ, "P");
			
			final File file2 = readEntry.files().get(1);
			final String fullFileName2 = file2.getAbsolutePath();
			final String originFileName2 = workspace.settingsFile().origin().second().getName();
			final String newFullFileName2 = targetFileName(originFileName2, fullFileName2, PAIRED_READ, "P");
			
			singletonFileName = new PairedReadName(
									new NameOfFile(targetFileName(originFileName1, fullFileName1, PAIRED_READ, "PS"))
								).value();  
			
			args = new String[] {"jgi.SplitPairsAndSingles", String.format("in1=%s", fullFileName1), String.format("in2=%s", fullFileName2), String.format("out1=%s", newFullFileName1), String.format("out2=%s", newFullFileName2), String.format("outs=%s", singletonFileName), "repair", "overwrite=true"};
			
			filesToOpen.add(new File(newFullFileName1));
			filesToOpen.add(new File(newFullFileName2));
			
			inOuts.put(fullFileName1, newFullFileName1);
			inOuts.put(fullFileName2, newFullFileName2);
			
			new JavaProcess(new String[] {xmxCommand()}, args).exec();
			
			stats(newFullFileName1);
			stats(newFullFileName2);
		}			
		
		
		
		for (Entry<String, String> entry : inOuts.entrySet()) {
			saveAutoStep(entry.getKey(), entry.getValue(), PipelineStep.PAIRED_READS);
		}
		
		File[] filesToOpenArray = new File[filesToOpen.size()];
		return filesToOpen.toArray(filesToOpenArray);
	}

	private void addReadGroupsCore() {
	
		validateStep(PipelineStep.ADD_READGROUPS);
		
		try {
			final String workingDir = fullPath(ADD_READGROUPS);
			
			if(!Files.exists(Paths.get(workingDir)))
				Files.createDirectory(Paths.get(workingDir));
			
			final String filePath = workspaceSettings.out().first().getAbsolutePath();			
			final String ouputFileBasename = new NameOfFileWithoutExtension(new NameOfFile(workspaceSettings.out().first())).value();
			final String ouptFilename = FilenameUtils.concat(workingDir, String.format("%sRG.bam", ouputFileBasename));
			Files.deleteIfExists(Paths.get(ouptFilename));
			
			final String readGroupID;
			if(sequenceFile.type() == ReadEntryType.SE)
				readGroupID = new NameOfFileWithoutExtension(new NameOfFile(sequenceFile.first())).value();
			else
				readGroupID = new PairedReadName(new NameOfFileWithoutExtension(new NameOfFile(sequenceFile.first()))).value();
			
			final String[] args= new String[] {"picard.jar", "AddOrReplaceReadGroups", String.format("I=%s", filePath), String.format("O=%s", ouptFilename), String.format("RGID=%s", readGroupID), "RGPL=Illumina", "RGPU=PU", String.format("RGSM=%s", readGroupID), "RGLB=LB" };
			new JavaJarProcess(new String[] {xmxCommand()}, args).exec();
	
			save11Step(filePath, ouptFilename, PipelineStep.ADD_READGROUPS);
			
			final Path previousWorkDir = new File(filePath).getParentFile().toPath();
			removeIntermediateFiles(previousWorkDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}
	
	private void sortBamCore() {
		
		validateStep(PipelineStep.SORT_BAM);
		
		try {
			final String workingDir = fullPath(SORT_BAM);
			if(!Files.exists(Paths.get(workingDir)))
				Files.createDirectory(Paths.get(workingDir));
			
			final String filePath = workspaceSettings.out().first().getAbsolutePath();			
			final String ouputFileBasename = new NameOfFileWithoutExtension(new NameOfFile(workspaceSettings.out().first())).value();
			final String ouptFilename = FilenameUtils.concat(workingDir, String.format("%sSORTED.bam", ouputFileBasename));
			Files.deleteIfExists(Paths.get(ouptFilename));
			
			final String[] args= new String[] {
									"picard.jar", "SortSam", String.format("I=%s", filePath), 
									String.format("O=%s", ouptFilename), "SORT_ORDER=coordinate"
								};
			new JavaJarProcess(new String[] {xmxCommand()}, args).exec();

			save11Step(filePath, ouptFilename, PipelineStep.SORT_BAM);
			
			final Path previousWorkDir = new File(filePath).getParentFile().toPath();
			removeIntermediateFiles(previousWorkDir);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}
	
	private void markDuplicatedReadsCore() {
		
		validateStep(PipelineStep.MARK_DUPLICATED);
		
		try {
			final String workingDir = fullPath(MARK_DUPLICATED);
			if(!Files.exists(Paths.get(workingDir)))
				Files.createDirectory(Paths.get(workingDir));
			
			final String filePath = workspaceSettings.out().first().getAbsolutePath();			
			final String ouputFileBasename = new NameOfFileWithoutExtension(new NameOfFile(workspaceSettings.out().first())).value();
			final String ouptFilename = FilenameUtils.concat(workingDir, String.format("%sMARKDUP.bam", ouputFileBasename));
			final String ouptFilenameTxt = FilenameUtils.concat(workingDir, String.format("%sMARKDUP.txt", ouputFileBasename));
			Files.deleteIfExists(Paths.get(ouptFilename));
			Files.deleteIfExists(Paths.get(ouptFilenameTxt));
			
			final String[] args= new String[] {
									"picard.jar", "MarkDuplicates", String.format("I=%s", filePath), 
									String.format("O=%s", ouptFilename), String.format("M=%s", ouptFilenameTxt), 
									"VALIDATION_STRINGENCY=SILENT"
								 };
			new JavaJarProcess(new String[] {xmxCommand()}, args).exec();

			save11Step(filePath, ouptFilename, PipelineStep.MARK_DUPLICATED);
			
			final Path previousWorkDir = new File(filePath).getParentFile().toPath();
			removeIntermediateFiles(previousWorkDir);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}
	
	public void map(final String referenceFilePath, final String targetFolder) {
		
		validateStep(PipelineStep.MAPPED);
		validateStep(PipelineStep.UNMAPPED_GH);
		validateStep(PipelineStep.MAPPED_3D7);
		
		final String workingDir = fullPath(targetFolder); 
		final List<File> files = workspaceSettings.out().files();
		
		if(sequenceFile.files().size() != files.size())
			throw new IllegalArgumentException("Number of paired files must be same as sequence files number !"); 
		
		final ReadEntry read;
		if(files.size() == 1) {
			read = new SingleRead(files.get(0));
		} else {
			read = new PairRead(files.get(0), files.get(1)); 
		}
		
		String[] args;
		final String outputFilePath;
		if(read.type() == ReadEntryType.SE) {
			final File file = read.files().get(0);
			final String outputMapFilePath; 
			final String outputUnMapFilePath;			
			if(targetFolder.equals(MAPPED_GH)) {
				outputMapFilePath = String.format("%s/%sMAPGH.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputUnMapFilePath = String.format("%s/%sUMAPGH.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputFilePath = outputUnMapFilePath;
			} else {
				outputMapFilePath = String.format("%s/%sMAP3D7.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputUnMapFilePath = String.format("%s/%sUMAP3D7.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputFilePath = outputMapFilePath;
			}
			
			if(SystemUtils.IS_OS_WINDOWS) {
				args = new String[] {"align2.BBMap", "build=1", "overwrite=true", String.format("in=%s", file.getAbsolutePath()), String.format("outm=%s", outputMapFilePath), String.format("outu=%s", outputUnMapFilePath), String.format("ref=%s", referenceFilePath), String.format("path=%s", FilenameUtils.getFullPath(referenceFilePath)) };
			} else {
				args = new String[] {"align2.BBMap", "build=1", "overwrite=true", String.format("in=%s", file.getAbsolutePath()), String.format("outm=%s", outputMapFilePath), String.format("outu=%s", outputUnMapFilePath), String.format("ref=%s", referenceFilePath), String.format("path=%s", FilenameUtils.getFullPath(referenceFilePath)), "usejni=t" };
			}			
			
		} else {
			final File file1 = read.files().get(0);
			final File file2 = read.files().get(1);
			final String outputMapFilePath;
			final String outputUnMapFilePath;
			
			if(targetFolder.equals(MAPPED_GH)) {
				outputMapFilePath = String.format("%s/%sMAPGH.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputUnMapFilePath = String.format("%s/%sUMAPGH.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputFilePath = outputUnMapFilePath;
			} else {
				outputMapFilePath = String.format("%s/%sMAP3D7.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputUnMapFilePath = String.format("%s/%sUMAP3D7.bam", workingDir, new NameOfFileWithoutExtension(new NameOfFile(read.name())).value());
				outputFilePath = outputMapFilePath;
			}
			
			if(SystemUtils.IS_OS_WINDOWS) {
				args = new String[] {"align2.BBMap", "build=1", "overwrite=true", String.format("in=%s", file1.getAbsolutePath()), String.format("in2=%s", file2.getAbsolutePath()), String.format("outm=%s", outputMapFilePath), String.format("outu=%s", outputUnMapFilePath), String.format("ref=%s", referenceFilePath), String.format("path=%s", FilenameUtils.getFullPath(referenceFilePath)) };
			} else {
				args = new String[] {"align2.BBMap", "build=1", "overwrite=true", String.format("in=%s", file1.getAbsolutePath()), String.format("in2=%s", file2.getAbsolutePath()), String.format("outm=%s", outputMapFilePath), String.format("outu=%s", outputUnMapFilePath), String.format("ref=%s", referenceFilePath), String.format("path=%s", FilenameUtils.getFullPath(referenceFilePath)), "usejni=t"};
			}
		}
		
		final String jniPath = FilenameUtils.concat(System.getProperty("user.dir"), "jni/");
		new JavaProcess(new String[] {xmxCommand(), String.format("-Djava.library.path=%s", jniPath) }, args).exec();
		
		final boolean isMappedGHOr3D7 = targetFolder.equals(MAPPED_GH) || targetFolder.equals(MAPPED_3D7);
		if(isMappedGHOr3D7) {
			final PipelineStep step;
			if(targetFolder.equals(MAPPED_GH)) {
				step = PipelineStep.UNMAPPED_GH;
			} else {
				step = PipelineStep.MAPPED_3D7;
			}
			
			if(read.type() == ReadEntryType.SE) {
				save11Step(read.first().getAbsolutePath(), outputFilePath, step);
			} else {
				save21Step(read.first().getAbsolutePath(), read.second().getAbsolutePath(), outputFilePath, step);
			}
		}
		
		final Path previousWorkDir = files.get(0).getParentFile().toPath();
		removeIntermediateFiles(previousWorkDir);
	}
	
	public void convertBamToFastqCore(String filePath, String bamPath) {
		
		validateStep(PipelineStep.BAM_TO_FASTQ);
		
		try {
			final String fullExtension = new ExtensionOfFile(new NameOfFile(sequenceFile.files().get(0))).value();
			
			final String[] args;
			
			final List<String> outputFilenames = new ArrayList<>();
			if(sequenceFile.type() == ReadEntryType.SE) {
				final String ouputFileBasename = new NameOfFileWithoutExtension(new NameOfFile(filePath)).value();
				final String ouptFilename = FilenameUtils.concat(bamPath, String.format("%s.%s", ouputFileBasename, fullExtension));
				Files.deleteIfExists(Paths.get(ouptFilename));
				
				args = new String[] {"picard.jar", "SamToFastq", String.format("INPUT=%s", filePath), String.format("FASTQ=%s", ouptFilename), "VALIDATION_STRINGENCY=SILENT" };
				outputFilenames.add(ouptFilename);
				
				new JavaJarProcess(new String[] {xmxCommand()}, args).exec();
				
				stats(ouptFilename); 
				
				save11Step(filePath, outputFilenames.get(0), PipelineStep.BAM_TO_FASTQ); 
			} else {
				final String ouputFileBasename = new NameOfFileWithoutExtension(new NameOfFile(filePath)).value();
				final String ouptFilename1 = FilenameUtils.concat(bamPath, String.format("%s_R1.%s", ouputFileBasename, fullExtension));
				final String ouptFilename2 = FilenameUtils.concat(bamPath, String.format("%s_R2.%s", ouputFileBasename, fullExtension));
				
				Files.deleteIfExists(Paths.get(ouptFilename1));
				Files.deleteIfExists(Paths.get(ouptFilename2));
				
				args = new String[] {"picard.jar", "SamToFastq", String.format("INPUT=%s", filePath), String.format("FASTQ=%s", ouptFilename1), String.format("SECOND_END_FASTQ=%s", ouptFilename2), "VALIDATION_STRINGENCY=SILENT" };
				outputFilenames.add(ouptFilename1);
				outputFilenames.add(ouptFilename2);
				
				new JavaJarProcess(new String[] {xmxCommand()}, args).exec();
				
				stats(ouptFilename1);
				stats(ouptFilename2);
				
				save12Step(filePath, outputFilenames.get(0), outputFilenames.get(1), PipelineStep.BAM_TO_FASTQ); 
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	private void validateStep(PipelineStep step) {
		
		if(workspaceSettings.step() == PipelineStep.BAM_TO_FASTQ && (step == PipelineStep.CLIP || step == PipelineStep.FILTER_QUALITY ||step == PipelineStep.PAIRED_READS)) {
			throw new IllegalArgumentException("You can not return back after that you mapped specimen !");
		}
		
		if(step == PipelineStep.SORT_BAM && workspaceSettings.step() != PipelineStep.ADD_READGROUPS) {
			throw new IllegalArgumentException("You have to ADD READGROUPS before sorting BAM file !");
		}
		
		if(step == PipelineStep.ADD_READGROUPS && !FilenameUtils.getExtension(workspaceSettings.out().first().getName()).equals("bam")) {
			throw new IllegalArgumentException("You have to get a BAM file before running ADD READGROUPS step !");
		}
		
		if(step == PipelineStep.MARK_DUPLICATED && workspaceSettings.step() != PipelineStep.SORT_BAM) {
			throw new IllegalArgumentException("You have to Sort BAM file before marking duplicated reads !");
		}
	}
	
	public String fullPath(final String targetFolder) {
		return FilenameUtils.concat(workspace.path(), String.format("%s/%s", RES_FOLDER, targetFolder));
	}
	
	private String targetFileName(final String originFileName, final String fullFileName, final String subFolder, final String key) {
		
		final String workingDir = workspace.path();
		final String[] fullNameSplitted = FilenameUtils.getName(fullFileName).split("\\.");
		final String realBaseName = fullNameSplitted[0];
		final String[] originFileNameBaseSplitted = originFileName.split("\\.");
		final String originFileNameBase = originFileNameBaseSplitted[0];
		final List<String> extensions = new ArrayList<>();
		for (int i = 1; i < fullNameSplitted.length; i++) {
			extensions.add(fullNameSplitted[i]); 
		}
		
		final String newFullFileName;
		
		if(originFileNameBase.endsWith("R1")) {
			newFullFileName = String.format("%s/%s/%s/%s%s_R1.%s", workingDir, RES_FOLDER, subFolder, realBaseName.replaceAll("_R1$", ""), key, String.join(".", extensions));
		} else if(originFileNameBase.endsWith("1")){
			newFullFileName = String.format("%s/%s/%s/%s%s_1.%s", workingDir, RES_FOLDER, subFolder, realBaseName.replaceAll("_1$", ""), key, String.join(".", extensions));
		} else if(originFileNameBase.endsWith("R2")){
			newFullFileName = String.format("%s/%s/%s/%s%s_R2.%s", workingDir, RES_FOLDER, subFolder, realBaseName.replaceAll("_R2$", ""), key, String.join(".", extensions));
		} else if(originFileNameBase.endsWith("2")){
			newFullFileName = String.format("%s/%s/%s/%s%s_2.%s", workingDir, RES_FOLDER, subFolder, realBaseName.replaceAll("_2$", ""), key, String.join(".", extensions));
		} else {
			newFullFileName = String.format("%s/%s/%s/%s%s.%s", workingDir, RES_FOLDER, subFolder, realBaseName, key, String.join(".", extensions));
		}
		
		return newFullFileName;
	}
	
	private String xmxCommand() {
		return "-Xmx" + settingsFile.getProperty("Xmx");
	}
	
	public void saveAutoStep(final String fullFileName, final String newFullFilename, PipelineStep step) {
		
		if(ReadEntryMetadata.isDirectRead(fullFileName)) {						
			final ReadEntry in = workspaceSettings.in();
			workspaceSettings.setIn(new PairRead(new File(fullFileName), in.second()));
			final ReadEntry out = workspaceSettings.out();
			workspaceSettings.setOut(new PairRead(new File(newFullFilename), out.second()));
		} else if(ReadEntryMetadata.isReverseRead(fullFileName)) {
			final ReadEntry in = workspaceSettings.in();
			workspaceSettings.setIn(new PairRead(in.first(), new File(fullFileName)));
			final ReadEntry out = workspaceSettings.out();
			workspaceSettings.setOut(new PairRead(out.first(), new File(newFullFilename)));
		} else {
			workspaceSettings.setIn(new SingleRead(new File(fullFileName)));
			workspaceSettings.setOut(new SingleRead(new File(newFullFilename)));
		}
		
		workspaceSettings.setStep(step); 
		
		try {
			workspaceSettings.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save11Step(final String fullFileName, final String newFullFilename, PipelineStep step) {
		
		workspaceSettings.setIn(new SingleRead(new File(fullFileName)));		
		workspaceSettings.setOut(new SingleRead(new File(newFullFilename)));
		
		workspaceSettings.setStep(step); 
		
		try {
			workspaceSettings.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void save21Step(final String filenameIn1, final String filenameIn2, final String filenameOut, PipelineStep step) {
		
		workspaceSettings.setIn(new PairRead(new File(filenameIn1), new File(filenameIn2)));
		
		workspaceSettings.setOut(new SingleRead(new File(filenameOut)));
		
		workspaceSettings.setStep(step); 
		
		try {
			workspaceSettings.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void save12Step(final String filenameIn, final String filenameOut1, final String filenameOut2, PipelineStep step) {
		
		workspaceSettings.setIn(new SingleRead(new File(filenameIn)));
		workspaceSettings.setOut(new PairRead(new File(filenameOut1), new File(filenameOut2)));
		
		workspaceSettings.setStep(step); 
		
		try {
			workspaceSettings.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save22Step(final String filenameIn1, final String filenameIn2, final String filenameOut1, final String filenameOut2, PipelineStep step) {
		
		workspaceSettings.setIn(new PairRead(new File(filenameIn1), new File(filenameIn2)));
		workspaceSettings.setOut(new PairRead(new File(filenameOut1), new File(filenameOut2)));
		
		workspaceSettings.setStep(step); 
		
		try {
			workspaceSettings.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void informImFinished() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);	
		msg.setConversationId(ConversationID.NOTIFY_WORKER_FINISHED.name());
		msg.addReceiver(hostAID()); 		
		send(msg);
	}
	
	public void sendVisualization(byte[] data) {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);	
		msg.setByteSequenceContent(data);
		msg.setConversationId(ConversationID.VISUALIZE.name());
		msg.addReceiver(hostAID()); 		
		send(msg);
	}
	
	public void notifyWithAllMessages() throws IOException {				
		notifyProgress(progressPercent);
	}
	
	public void notifyWithAllMessages(AID client) throws IOException {				
		notifyProgress(client, progressPercent);
	}
	
	public void indexReference(final String referenceFilePath) {
		
		final String[] myCommand;
		if(SystemUtils.IS_OS_WINDOWS) {
			myCommand = new String[] { "align2.BBMap", "build=1", String.format("ref=%s", referenceFilePath), "rebuild=f", String.format("path=%s", new File(referenceFilePath).getParentFile().getAbsolutePath()) };			
		} else {
			myCommand = new String[] { "align2.BBMap", "build=1", String.format("ref=%s", referenceFilePath), "rebuild=f", String.format("path=%s", new File(referenceFilePath).getParentFile().getAbsolutePath()), "usejni=t" };
		}
		
		final String jniPath = FilenameUtils.concat(System.getProperty("user.dir"), "jni/");
		new JavaProcess(new String[] {xmxCommand(), String.format("-Djava.library.path=%s", jniPath) }, myCommand).exec();
	}
	
	public void checkThatReferencesAreAllIndexed() {

		final Path referencePath = Paths.get(FilenameUtils.concat(System.getProperty("user.dir"), "reference"));
		 for (final File f : referencePath.toFile().listFiles()) {
            if (f.isDirectory() && Arrays.stream(f.listFiles()).noneMatch(c -> c.getName().endsWith("ref"))) {
            	throw new IllegalArgumentException(String.format("Reference at folder %s need to be indexed ! Run index reference command to index all necessary references.", f.getName()));            	
            }
	    }
	}
	
	public File generateVisualization(final String filePath) throws IOException {	
		final String[] command = new String[] { "uk.ac.babraham.FastQC.FastQCApplication", filePath };					
		new JavaProcess(new String[] {String.format("-Xmx%s", settingsFile.getProperty("Xmx"))}, command).exec();
		
		return new FilesOfExtension(new File(filePath).getParentFile().getPath(), "html").items().get(0);
	}
	
	public byte[] convertFileToBytes(final File file) throws IOException {	
		return FileUtils.readFileToByteArray(file);
	}
	
	private void stats(String filePath) {
		stats(filePath, String.format("%s.json", new NameOfFileWithoutExtension(new NameOfFile(filePath)).value()));
	}
	
	private void stats(String filePath, String statsFile) {
		final String[] args = new String[] {"jgi.AssemblyStats2", String.format("in=%s", filePath), "format=8", "extended=t", String.format("out=%s", statsFile)};
		new JavaProcess(new String[] {xmxCommand()}, args).exec();
	}
	
	private void displayStepTitle(String title) {
		System.err.println("-----------------------------------------------------------");
		System.err.println(title);
		System.err.println("-----------------------------------------------------------");
	}
	
	private void removeIntermediateFiles(Path dirPath) {

		if(!command.canDeleteIntermediateFiles())
			return;
		
		try {			
			final List<File> files;
			
			if(dirPath.endsWith("mark-duplicated") || (!command.hasStep(PipelineStep.MARK_DUPLICATED) && dirPath.endsWith("mapped-3d7"))) {
				files = new FilesWithoutExtension(dirPath, "bam", "json").items();
			} else {
				files = new FilesWithoutExtension(dirPath, "json").items();
			}
			
			for (File file : files) {
				FileUtils.deleteQuietly(file);			
			}
		} catch (IOException e) {
			
		}		
	}
}
