package com.ipci.ngs.datacleaner.server.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;

import com.ipci.ngs.datacleaner.commonlib.agent.ConversationID;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotification;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.server.business.CachedWorkspaces;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;

public final class HostAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final transient Logger log;
	private Collection<AID> clientAgents;
	private Collection<AID> workerAgents;
	
	private DFAgentDescription dfd;
	
	static {
		
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
            sd.setType("server-agent");
            sd.setName("host-agent");
            
            dfd = new DFAgentDescription();
            dfd.setName(getAID());
            dfd.addServices(sd);
			DFService.register(this, dfd);
			
			clientAgents = Collections.synchronizedCollection(new HashSet<>());
			workerAgents = Collections.synchronizedCollection(new HashSet<>());
			
			// add a Behaviour to process incoming messages
			addBehaviour(new HostBehaviour(this));			
			
		} catch (FIPAException e) {
			log.trace(e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	protected void takeDown() {		
		try {
			DFService.deregister(this, dfd);
		} catch (FIPAException e) {
			log.trace(e.getLocalizedMessage(), e); 
		}
	}

	protected void addClient(AID aid) {
		clientAgents.add(aid);
		log.info(String.format("Client %s connected", aid));
	}
	
	protected void removeClient(AID aid) {
		clientAgents.remove(aid);
		log.info(String.format("Client %s left", aid));
	}
	
	protected void addWorker(AID aid) {
		workerAgents.add(aid);
		log.info(String.format("Worker %s started", aid));
	}
	
	protected void removeWorker(AID aid) {
		workerAgents.remove(aid);
		log.info(String.format("Worker %s left", aid));
	}
	
	protected void notifyLog(PipelineLogNotification notification) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);	
		
		for (AID aid : clientAgents) {
			msg.addReceiver(aid);
		}
		
		msg.setContentObject(notification);		
		msg.setConversationId(ConversationID.NOTIFY_LOG.name());
		send(msg);
	}
	
	protected void notifyProgress(PipelineProgressNotification notification) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);	
		
		for (AID aid : clientAgents) {
			msg.addReceiver(aid);
		}
		
		msg.setContentObject(notification);		
		msg.setConversationId(ConversationID.NOTIFY_PROGRESS.name());
		send(msg);
	}
	
	protected void notifyWorkspaceStatus(WorkspaceStatusNotification notification) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);	
		
		for (AID aid : clientAgents) {
			msg.addReceiver(aid);
		}
		
		msg.setContentObject(notification);		
		msg.setConversationId(ConversationID.NOTIFY_WORKSPACE_STATUS.name());
		send(msg);
	}
	
	protected void requestNotification(AID client) throws IOException {		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		
		for (AID aid : workerAgents) {
			msg.addReceiver(aid);
		}
		
		msg.setContentObject(client);

		msg.setConversationId(ConversationID.REQUEST_NOTIFICATION.name());
		send(msg);
	}
	
	protected void requestNotification(AID client, String workspaceId) throws IOException {		
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);	
		
		for (AID aid : workerAgents) {
			if(aid.getLocalName().equals(workspaceId)) {
				msg.addReceiver(aid);
				break;
			}			
		}
		
		msg.setContentObject(client);

		msg.setConversationId(ConversationID.REQUEST_NOTIFICATION_SPECIFIC.name());
		send(msg);
	}
	
	protected void removeWorkspaces(List<Workspace> workspaces) throws IOException {
		for (Workspace workspace : workspaces) {
			FileUtils.forceDelete(new File(workspace.path()));
		}		
	}
	
	protected void notifyError(Throwable throwable) {
		ACLMessage msg = new ACLMessage(ACLMessage.FAILURE);	
		
		for (AID aid : clientAgents) {
			msg.addReceiver(aid);
		}
		
		msg.setContent(throwable.getLocalizedMessage());	
		msg.setConversationId(ConversationID.NOTIFY_ERROR.name());
		send(msg);
	}
	
	protected void sendWorkspaces(AID aid) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);
		msg.addReceiver(aid);
		msg.setContentObject(new ArrayList<>(new CachedWorkspaces().items())); 
		msg.setConversationId(ConversationID.LOAD_WORKSPACES.name()); 
		send(msg);
	}
	
	protected void firstSendWorkspaces(AID aid) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(aid);
		msg.setContentObject(new ArrayList<>(new CachedWorkspaces().items())); 
		msg.setConversationId(ConversationID.LOAD_WORKSPACES.name()); 
		send(msg);
	}
	
	protected void notifyRefreshWorkspace(AID aid, Workspace workspace) throws IOException {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);	
		msg.addReceiver(aid);
		msg.setContentObject(workspace);		
		msg.setConversationId(ConversationID.REFRESH_WORKSPACE.name());
		send(msg);
	}
	
	protected void runPipeline(PipelineCommand pipelineCommand) throws ControllerException {
		PlatformController container = getContainerController();
		AgentController worker = container.createNewAgent(pipelineCommand.workspace().id(), "com.ipci.ngs.datacleaner.server.agent.WorkerAgent", new Object[] {pipelineCommand});
		worker.start();
		
		addWorker(new AID(pipelineCommand.workspace().id(), AID.ISLOCALNAME));
	}
	
	protected void sendVisualization(final byte[] data) {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);	
		
		for (AID aid : clientAgents) {
			msg.addReceiver(aid);
		}
		
		msg.setByteSequenceContent(data);
		msg.setConversationId(ConversationID.VISUALIZE.name());
		send(msg);
	}
}
