package com.ipci.ngs.datacleaner.server.agent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ipci.ngs.datacleaner.commonlib.agent.ConversationID;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotification;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntriesFromDirectory;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.server.business.CachedWorkspaces;
import com.ipci.ngs.datacleaner.server.business.Workspaces;
import com.ipci.ngs.datacleaner.server.business.WorkspacesImpl;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public final class HostBehaviour extends CyclicBehaviour {

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
	
	public HostBehaviour(final Agent agent) {
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
            	final Workspaces workspaces = new WorkspacesImpl();
            	
            	conversationId = ConversationID.valueOf(msg.getConversationId());
            	
            	switch (msg.getPerformative()) {            	    
					case ACLMessage.INFORM:
						switch(conversationId) {
							case NOTIFY_NEW_CLIENT:
								((HostAgent)myAgent).addClient(msg.getSender());
								break;
							case NOTIFY_CLIENT_LEFT:
								((HostAgent)myAgent).removeClient(msg.getSender());
								break;
							case NOTIFY_WORKER_FINISHED:
								((HostAgent)myAgent).removeWorker(msg.getSender());
								break;							
							default:
								break;
						}
						break;
					case ACLMessage.PROPAGATE:
						switch(conversationId) {
							case NOTIFY_LOG:
								final PipelineLogNotification notLog = (PipelineLogNotification)msg.getContentObject();
								((HostAgent)myAgent).notifyLog(notLog);
								break;
							case NOTIFY_PROGRESS:
								PipelineProgressNotification notProgress = (PipelineProgressNotification)msg.getContentObject();
								((HostAgent)myAgent).notifyProgress(notProgress);
								break;
							case NOTIFY_WORKSPACE_STATUS:
								WorkspaceStatusNotification wpStatusNotification = (WorkspaceStatusNotification)msg.getContentObject();
								((HostAgent)myAgent).notifyWorkspaceStatus(wpStatusNotification);
								break;
							case VISUALIZE:
								((HostAgent)myAgent).sendVisualization(msg.getByteSequenceContent());
								break;
							default:
								break;
						}
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
								
								for (ReadEntry entry : specimens) {
									workspaces.init(entry);
								}
								
								remsg = msg.createReply();
								remsg.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								remsg.setConversationId(ConversationID.CREATE_WORKSPACES.name()); 
								myAgent.send(remsg);
								break;
							case LOAD_WORKSPACES:			
								((HostAgent)myAgent).firstSendWorkspaces(msg.getSender());								
								break;							
							case RUN_PIPELINE:
								final PipelineCommand pipelineCommand = (PipelineCommand)msg.getContentObject();								
								((HostAgent)myAgent).runPipeline(pipelineCommand);								
								break;
							case REMOVE_WORKSPACE:
								@SuppressWarnings("unchecked") 
								final List<Workspace> workspacesToDelete = (List<Workspace>)msg.getContentObject();
								
								((HostAgent)myAgent).removeWorkspaces(workspacesToDelete);
								((HostAgent)myAgent).sendWorkspaces(msg.getSender());
								break;
								
							case CLEAN_WORKSPACE:
								final String wid = msg.getContent();								
								final Workspace workspace = new CachedWorkspaces().get(wid);
								final LocalDateTime date = workspace.settingsFile().date();
								((HostAgent)myAgent).removeWorkspaces(Arrays.asList(workspace));
								final Workspace cleanedWorkspace = new CachedWorkspaces().init(workspace.settingsFile().origin(), wid, date);
								
								((HostAgent)myAgent).notifyRefreshWorkspace(msg.getSender(), cleanedWorkspace);
								break;
							case REFRESH_WORKSPACE_OUTPUT:
								final String widToRefresh = msg.getContent();
								final Workspace workspaceToRefresh = new CachedWorkspaces().get(widToRefresh);
								
								((HostAgent)myAgent).notifyRefreshWorkspace(msg.getSender(), workspaceToRefresh);
								break;
							case REQUEST_NOTIFICATION_SPECIFIC:
								final String widToNotify = msg.getContent();
								((HostAgent)myAgent).requestNotification(msg.getSender(), widToNotify);
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
			((HostAgent)myAgent).notifyError(e);
			log.trace(e.getLocalizedMessage(), e);
		}
	}
}
