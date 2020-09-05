package com.ipci.ngs.datacleaner.client.agent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.ipci.ngs.datacleaner.client.ui.MainFrame;
import com.ipci.ngs.datacleaner.commonlib.agent.ConversationID;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotification;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntriesFromDirectory;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public final class ClientBehaviour extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final transient Logger log = Logger.getLogger("log");
	private final MainFrame frame;
	
	public ClientBehaviour(final Agent agent, final MainFrame frame) {
		super(agent);
		this.frame = frame;
	}

	@Override
	public void action() {
		try {
			
			// listen if a message arrives   
            ACLMessage msg = myAgent.receive();
			            
            if(msg != null) {
            	
            	ConversationID conversationId;
            	try {
            		conversationId = ConversationID.valueOf(msg.getConversationId());
				} catch (Exception e) {
					conversationId = ConversationID.NONE;
				}
            	
            	switch (msg.getPerformative()) {
					case ACLMessage.INFORM:						
						switch(conversationId) {
							case LIST_OF_READ_IN_PATH:
								@SuppressWarnings("unchecked") 
								List<ReadEntry> entries = (List<ReadEntry>)msg.getContentObject();								
								SwingUtilities.invokeLater( 
									() -> frame.loadSpecimens(entries)
								);
								break;
							case LOAD_WORKSPACES:
								@SuppressWarnings("unchecked") 
								List<Workspace> workspaces = (List<Workspace>)msg.getContentObject();								
								SwingUtilities.invokeLater( 
									() -> frame.firstLoadWorkspaces(workspaces)									
								);
								break;
							default:
								break;
						}
						break;
					case ACLMessage.REQUEST:
						final String path = msg.getContent();
						List<ReadEntry> entries = new ReadEntriesFromDirectory(path).items();
						ACLMessage remsg = msg.createReply();
						remsg.setContentObject(new ArrayList<>(entries)); 
						myAgent.send(remsg);
						break;
					case ACLMessage.ACCEPT_PROPOSAL:
						switch(conversationId) {
							case CREATE_WORKSPACES:						
								SwingUtilities.invokeLater( 
									() -> frame.acceptCreateWorkspaces()
								);
								break;
							default:
								break;
						}
						break;
					case ACLMessage.PROPAGATE:
						switch(conversationId) {
							case LOAD_WORKSPACES:
								@SuppressWarnings("unchecked") 
								List<Workspace> workspaces = (List<Workspace>)msg.getContentObject();
								SwingUtilities.invokeLater( 
									() -> frame.loadWorkspaces(workspaces)
								);
								break;
							case VISUALIZE:
								byte[] bytes = msg.getByteSequenceContent();																
								SwingUtilities.invokeLater( 
									() -> frame.openFastQCReport(bytes)
								);
								break;
							case NOTIFY_LOG:
								PipelineLogNotification notLog = (PipelineLogNotification)msg.getContentObject();													
								SwingUtilities.invokeLater( 
									() -> frame.notify(notLog)
								);
								break;
							case NOTIFY_PROGRESS:
								PipelineProgressNotification notProgress = (PipelineProgressNotification)msg.getContentObject();													
								SwingUtilities.invokeLater( 
									() -> frame.notify(notProgress)
								);
								break;
							case NOTIFY_WORKSPACE_STATUS:
								WorkspaceStatusNotification wpStatusNotification = (WorkspaceStatusNotification)msg.getContentObject();													
								SwingUtilities.invokeLater( 
									() -> frame.notify(wpStatusNotification)
								);
								break;								
							case REFRESH_WORKSPACE:
								Workspace refreshedWorkspace = (Workspace)msg.getContentObject();													
								SwingUtilities.invokeLater( 
									() -> frame.notifyRefreshWorkspace(refreshedWorkspace)
								);
								break;
							default:
								break;
						}
						break;
					case ACLMessage.FAILURE:
						final String errorMessage = msg.getContent();
						JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
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
