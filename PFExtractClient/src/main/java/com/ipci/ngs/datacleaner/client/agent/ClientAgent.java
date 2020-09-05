package com.ipci.ngs.datacleaner.client.agent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;

import com.ipci.ngs.datacleaner.client.ui.MainFrame;
import com.ipci.ngs.datacleaner.commonlib.agent.ConversationID;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public final class ClientAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MainFrame mainFrame;
	
	private static final transient Logger log;
	private static final Properties config;
	
	private DFAgentDescription dfd;
	
	static {
		
		config = new Properties();
    	try(InputStream inputStreamConfig = new FileInputStream("config/settings.properties")) {
			config.load(inputStreamConfig);			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	
		log = Logger.getLogger("log");
		
		try {			
			SimpleLayout layout = new SimpleLayout(); 			
			FileAppender appender = new FileAppender(layout, "log.log", false);
			ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN));
			appender.activateOptions();
			log.addAppender(appender);
			log.addAppender(consoleAppender);
			log.setLevel(Level.ALL);			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected  void setup() {
		try {
        	// create the agent description of itself
        	final ServiceDescription sd = new ServiceDescription();
            sd.setType("client-agent");
            sd.setName("client-agent");
            
            dfd = new DFAgentDescription();
            dfd.setName(getAID());
            dfd.addServices(sd);
			DFService.register(this, dfd);			
			
			// add the GUI
			mainFrame = new MainFrame( this );
			
			// add a Behaviour to process incoming messages
			addBehaviour(new ClientBehaviour(this, mainFrame));
			
			addBehaviour
			( 
				new OneShotBehaviour() {
                      /**
					 * 
					 */
					private static final long serialVersionUID = 1L; 

					public void action() {
						mainFrame.setLocationRelativeTo(null);
						mainFrame.setVisible(true);	
						mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);						
                    }
                } 
			);
			
		} catch (FIPAException e) {
			log.trace(e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	protected void takeDown() {		
		informImGone();
		try {
			DFService.deregister(this, dfd);			
		} catch (FIPAException e) {
			log.trace(e.getLocalizedMessage(), e); 
		}
	}
	
	public void searchSpecimens(String path) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);		
		msg.setContent(path); 		
		msg.setConversationId(ConversationID.LIST_OF_READ_IN_PATH.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void createWorkspaces(List<ReadEntry> entries) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);		
		msg.setContentObject(new ArrayList<>(entries)); 		
		msg.setConversationId(ConversationID.LIST_OF_READ_IN_PATH.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void requestLoadWorkspaces() {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);				
		msg.setConversationId(ConversationID.LOAD_WORKSPACES.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void requestCreateWorkspaces(List<ReadEntry> entries) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);		
		msg.setContentObject(new ArrayList<>(entries));
		msg.setConversationId(ConversationID.CREATE_WORKSPACES.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	private AID remoteAID() {
		AID remoteAMSf = new AID(String.format("host-agent@%s:%s/JADE", config.getProperty("host"), config.getProperty("port")), AID.ISGUID);
		remoteAMSf.addAddresses(String.format("http://%s:%s/acc", config.getProperty("host"), config.getProperty("mtp-port")));
		return remoteAMSf;
	}
	
	public void requestVisualize(String filePath) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);		
		msg.setContent(filePath); 
		msg.setConversationId(ConversationID.VISUALIZE.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void requestRunPipeline(final PipelineCommand command) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		msg.setContentObject(command);
		msg.setConversationId(ConversationID.RUN_PIPELINE.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void removeWorkspace(final List<Workspace> workspaces) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		msg.setContentObject(new ArrayList<>(workspaces));
		msg.setConversationId(ConversationID.REMOVE_WORKSPACE.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void informImNew() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);		
		msg.setConversationId(ConversationID.NOTIFY_NEW_CLIENT.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void informImGone() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);	
		msg.setConversationId(ConversationID.NOTIFY_CLIENT_LEFT.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void cleanWorkspace(String wid) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		msg.setContent(wid); 
		msg.setConversationId(ConversationID.CLEAN_WORKSPACE.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void refreshWorkspaceOutput(String wid) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		msg.setContent(wid); 
		msg.setConversationId(ConversationID.REFRESH_WORKSPACE_OUTPUT.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
	
	public void requestNotification(String wid) {
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		msg.setContent(wid); 
		msg.setConversationId(ConversationID.REQUEST_NOTIFICATION_SPECIFIC.name());
		msg.addReceiver(remoteAID()); 		
		send(msg);
	}
}
