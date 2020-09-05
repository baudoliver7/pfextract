package com.ipci.ngs.datacleaner.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.ipci.ngs.datacleaner.client.agent.ClientAgent;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotification;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final transient Logger log = Logger.getLogger("log");
	
	private final JPanel contentPane;
	private HomePage homePage = null;
	private final JDesktopPane desktopPane;
	private SelectSpecimensDialog selectSpecimensDg = null;
	private final ClientAgent clientAgent;
	private final JLabel lblStatus;
	
	/**
	 * Create the frame.
	 */
	public MainFrame(final ClientAgent clientAgent) {
		
		this.clientAgent = clientAgent;
		
		setTitle("IPCI NGS Data Cleaner Client");
		setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 582, 354);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		mnFile.add(mntmExit);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.SOUTH);
		
		lblStatus = new JLabel("New label");
		toolBar.add(lblStatus);
		
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.LIGHT_GRAY);
		contentPane.add(desktopPane, BorderLayout.CENTER);
		
		addWindowListener( new WindowAdapter()
		{
			@Override
		    public void windowClosing(WindowEvent e)
		    {
		    	close();
		    }
		});
		
		showHomePage();
		
		requestLoadWorkspaces();
	}
	
	private void close() {
		
		int result = JOptionPane.showConfirmDialog(
	            this,
	            "Do you want to exit ?",
	            "Exit",
	            JOptionPane.YES_NO_OPTION);
	 
	        if (result == JOptionPane.YES_OPTION) {
	        	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        }
	}
	
	public void showHomePage() {
		
		if(homePage == null) {
			homePage = new HomePage(this);
			javax.swing.plaf.InternalFrameUI ui = homePage.getUI();
			((javax.swing.plaf.basic.BasicInternalFrameUI)ui).setNorthPane(null);
			homePage.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			desktopPane.add(homePage);
		}
		
		try {
			homePage.setMaximum(true);
			homePage.setSelected(true);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
		
		homePage.setVisible(true);
		
		notify("Ready");
	}
	
	protected void openSelectSpecimens() {
		selectSpecimensDg = new SelectSpecimensDialog(this);
		selectSpecimensDg.setLocationRelativeTo(this);
		selectSpecimensDg.setVisible(true);				
		selectSpecimensDg = null;
	}
	
	protected void searchSpecimens(String path) {
		clientAgent.searchSpecimens(path); 
	}
	
	public void loadSpecimens(List<ReadEntry> specimens) {
		if(selectSpecimensDg == null)
			return;
		
		selectSpecimensDg.loadSpecimens(specimens);
	}
	
	public void requestLoadWorkspaces() {
		clientAgent.requestLoadWorkspaces();
	}
	
	public void acceptCreateWorkspaces() {
		if(selectSpecimensDg == null)
			return;
		
		selectSpecimensDg.accept();
	}
	
	public void requestCreateWorkspaces(List<ReadEntry> specimens) throws IOException {
		clientAgent.requestCreateWorkspaces(specimens);
	}

	public void loadWorkspaces(List<Workspace> workspaces) {
		notifiyReady();
		if(homePage == null)
			return;
		
		homePage.loadWorkspaces(workspaces);	
	}
	
	public void firstLoadWorkspaces(List<Workspace> workspaces) {
		loadWorkspaces(workspaces);
		
		clientAgent.informImNew();
	}
	
	public void notify(String message) {
		lblStatus.setText(message);
	}
	
	public void notifiyReady() {
		lblStatus.setText("Ready");
	}
	
	public void requestVisualize(String filePath) {
		notify("Requesting visualization...");
		clientAgent.requestVisualize(filePath);
	}
	
	public void requestRunPipeline(final PipelineCommand command) throws IOException {
		clientAgent.requestRunPipeline(command);
	}	
	
	public void notify(PipelineLogNotification notification) {
		homePage.notify(notification); 
	}
	
	public void notify(PipelineProgressNotification notification) {
		homePage.notify(notification); 
	}
	
	public void notify(WorkspaceStatusNotification notification) {
		homePage.notify(notification); 
	}
	
	public void openFastQCReport(final byte[] bytes) {
		
		notify("Opening report in browser...");
		
		try {
			File tempFile = File.createTempFile("file_", ".html", null);
			try(FileOutputStream fos = new FileOutputStream(tempFile)){
				fos.write(bytes);
			}
			
        	Desktop desktop = Desktop.getDesktop();
        	desktop.browse(tempFile.toURI());
        	        	
		} catch (IOException e) {
			log.trace(e.getLocalizedMessage(), e);
			JOptionPane.showMessageDialog(MainFrame.this, e.getLocalizedMessage(), "Remove workspaces", JOptionPane.ERROR_MESSAGE);
		} finally {
			notifiyReady();
		}
	}
	
	public void removeWorkspace(final List<Workspace> workspaces) throws IOException {
		notify("Removing workpspace...");
		clientAgent.removeWorkspace(workspaces); 
	}
	
	public void cleanWorkspace(String wid) {
		notify("Cleaning workpspace...");
		clientAgent.cleanWorkspace(wid);
	}
	
	public void refreshWorkspaceOutput(String wid) {
		notify("Refreshing output...");
		clientAgent.refreshWorkspaceOutput(wid);
	}
	
	public void notifyRefreshWorkspace(Workspace workspace) {
		try {
			homePage.notifyRefreshWorkspace(workspace);
		} catch (IOException e) {
			log.trace(e.getLocalizedMessage(), e);
			JOptionPane.showMessageDialog(MainFrame.this, e.getLocalizedMessage(), "Refresh workspace", JOptionPane.ERROR_MESSAGE);
		} finally {
			notifiyReady();
		}
	}	
	
	public void requestNotification(String wid) {
		clientAgent.requestNotification(wid);
	}
}
