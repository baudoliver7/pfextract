package com.ipci.ngs.datacleaner.server.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.ipci.ngs.datacleaner.commonlib.agent.ConversationID;
import com.ipci.ngs.datacleaner.commonlib.reads.FilesOfExtension;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntriesFromDirectory;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.utilities.JavaProcess;
import com.ipci.ngs.datacleaner.server.business.CachedWorkspaces;
import com.ipci.ngs.datacleaner.server.business.Workspaces;
import com.ipci.ngs.datacleaner.server.business.WorkspacesImpl;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public final class WorkerBehaviour extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final transient Logger log = Logger.getLogger("log");
	private static final Properties settingsFile;
	
	static {
		try(InputStream stream = new FileInputStream("config/settings.properties")){
			settingsFile = new Properties();
			settingsFile.load(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public WorkerBehaviour(final Agent agent) {
		super(agent);
	}

	@Override
	public void action() {
		
		try {
			
			// listen if a message arrives   
            ACLMessage msg = myAgent.receive();
			            
            if(msg != null) {
            	
            	final ConversationID conversationId;            	
            	final ACLMessage remsg;
            	
            	conversationId = ConversationID.valueOf(msg.getConversationId());
            	
            	switch (msg.getPerformative()) {
            	
					case ACLMessage.INFORM:
						/*switch (conversationId) {
							case REQUEST_NOTIFICATION:
								((WorkerAgent)myAgent).notifyWithAllMessages();
								break;
	
							default:
								break;
						}*/
						break;
					case ACLMessage.REQUEST:						
						switch(conversationId) {
							case LIST_OF_READ_IN_PATH:
								final String path = msg.getContent();
								final List<ReadEntry> entries = new ReadEntriesFromDirectory(path).items();								
								remsg = msg.createReply();
								remsg.setPerformative(ACLMessage.INFORM);
								remsg.setContentObject(new ArrayList<>(entries)); 
								myAgent.send(remsg);
								break;
							case CREATE_WORKSPACES:
								@SuppressWarnings("unchecked") 
								final List<ReadEntry> specimens = (List<ReadEntry>)msg.getContentObject();
								
								final Workspaces workspaces = new WorkspacesImpl();
								for (ReadEntry entry : specimens) {
									workspaces.init(entry);
								}
								
								remsg = msg.createReply();
								remsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								remsg.setConversationId(ConversationID.CREATE_WORKSPACES.name()); 
								myAgent.send(remsg);
								break;
							case LOAD_WORKSPACES:			
								remsg = msg.createReply();
								remsg.setContentObject(new ArrayList<>(new CachedWorkspaces().items())); 
								remsg.setPerformative(ACLMessage.PROPAGATE);
								remsg.setConversationId(ConversationID.LOAD_WORKSPACES.name()); 
								myAgent.send(remsg);
								break;
							case VISUALIZE:
								final String filePath = msg.getContent();
								
								final String[] command = new String[] { "uk.ac.babraham.FastQC.FastQCApplication", filePath };					
								new JavaProcess(new String[] {String.format("-Xmx%s", settingsFile.getProperty("Xmx"))}, command).exec();
								
								final File result = new FilesOfExtension(new File(filePath).getParentFile().getPath(), "html").items().get(0);
								byte[] data = FileUtils.readFileToByteArray(result);
								
								remsg = msg.createReply();
								remsg.setByteSequenceContent(data);								
								remsg.setPerformative(ACLMessage.PROPAGATE);
								remsg.setConversationId(ConversationID.VISUALIZE.name()); 
								myAgent.send(remsg);
								break;
							case REQUEST_NOTIFICATION:
								((WorkerAgent)myAgent).notifyWithAllMessages();
								break;
							case REQUEST_NOTIFICATION_SPECIFIC:
								final AID client = (AID)msg.getContentObject();
								((WorkerAgent)myAgent).notifyWithAllMessages(client);
								break;
							default:
								break;
						}
						
						break;
					default:
						break;
				}
            	
            } else {
            	// if no message is arrived, block the behaviour
            	block();
            }
		} catch (Exception e) {
			log.trace(e.getLocalizedMessage(), e);
		}
	}
}
