package com.ipci.ngs.datacleaner.client.ui;

import javax.swing.JInternalFrame;
import com.jgoodies.forms.layout.FormLayout;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommandImpl;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.WorkspaceStatusNotificationImpl;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.utilities.JobState;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.swing.JButton;

import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTabbedPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class HomePage extends JInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTable table;
	private final SpecimenTableModel model;
	private final JTabbedPane tabbedPane;
	private List<Workspace> workspaces;
	private final MainFrame mainFrame;
	private final Map<String, Collection<JPanel>> tabbedPanesByWid;
	
	private final transient Logger log = Logger.getLogger("log");
	
	/**
	 * Create the frame.
	 */
	public HomePage(final MainFrame mainFrame) {
		
		this.mainFrame = mainFrame;
		this.tabbedPanesByWid = new ConcurrentHashMap<>();
		this.workspaces = new ArrayList<>();
		
		setBounds(100, 100, 891, 502);
		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("left:375px"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(112dlu;default):grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:260px:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(88dlu;default):grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, "2, 2, 1, 3, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:max(127dlu;default):grow"),}));
		
		JLabel lblSpecimens = new JLabel("Specimens");
		panel.add(lblSpecimens, "2, 2");
		
		model = new SpecimenTableModel(this);
		table = new JTable(model);		
		
		table.getColumnModel().getColumn(SpecimenTableModel.NO).setMaxWidth(30);
		table.getColumnModel().getColumn(SpecimenTableModel.SELECTION).setMaxWidth(30);
		table.getColumnModel().getColumn(SpecimenTableModel.STATE).setMaxWidth(100);		
		table.getColumnModel().getColumn(SpecimenTableModel.STATE).setMinWidth(100);
		table.getColumnModel().getColumn(SpecimenTableModel.STATE).setWidth(100);
		
		JCheckBox chckbxSelectedAll = new JCheckBox("Selected all");
		chckbxSelectedAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0; i < table.getRowCount(); i++) {
		        	table.setValueAt(chckbxSelectedAll.isSelected(), i, 1);
		        }
			}
		});
		panel.add(chckbxSelectedAll, "4, 2, right, default");
		table.setFillsViewportHeight(true);
		
		panel.add(new JScrollPane(table), "2, 4, 3, 1, fill, fill");
		
		final TableColumn idClmn= table.getColumn("Workspace");
    	idClmn.setMaxWidth(0);
    	idClmn.setMinWidth(0);
    	idClmn.setPreferredWidth(0);
    	
    	table.getSelectionModel().addListSelectionListener(
    		e -> {
	    		if(table.getSelectedRow() == -1) {
	    			setNoWorkspace();
	    			return;
	    		}
	    		
	    		final Workspace workspace = workspaces.get(table.getSelectedRow());
	    		setCurrentWorkspace(workspace); 		
    		}
    	);
    	
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, "4, 2, 1, 3, fill, fill");
		tabbedPane.addTab("Workspace", new NoWorkspacePanel());
		
		JPanel panel_4 = new JPanel();
		getContentPane().add(panel_4, "2, 6, fill, fill");
		panel_4.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnNew = new JButton("New");
		btnNew.addActionListener(
			e -> mainFrame.openSelectSpecimens()
		);
		panel_4.add(btnNew);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
								
				final List<Workspace> selectedSpecimens = selectedWorkspaces();
            	
            	if(selectedSpecimens.isEmpty()) {
            		JOptionPane.showMessageDialog(HomePage.this, "You must select at least one specimen !", "Remove workspaces", JOptionPane.INFORMATION_MESSAGE);
            		return;
            	}
            	
            	for (Workspace workspace : selectedSpecimens) {
					if(workspace.state() == JobState.RUNNING) {
						JOptionPane.showMessageDialog(HomePage.this, String.format("You can't delete a workspace while it is running (specimen %s) !", workspace.specimen().name()), "Remove workspaces", JOptionPane.INFORMATION_MESSAGE);
	            		return;
					}
				}
            	
            	int reply = JOptionPane.showConfirmDialog(HomePage.this, "Do you want to delete workspaces selected !", "Remove workspaces", JOptionPane.YES_NO_OPTION);
            	if(reply == JOptionPane.NO_OPTION) {
            		return;
            	}
            	
            	try {
					removeWorkspace(selectedSpecimens); 										
				} catch (IOException e1) {
					log.trace(e1.getLocalizedMessage(), e1); 
					JOptionPane.showMessageDialog(HomePage.this, e1.getLocalizedMessage(), "Remove workspaces", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		panel_4.add(btnRemove);
		
		JButton btnAllRunPipeline = new JButton("Run pipeline");
		btnAllRunPipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final List<Workspace> selectedSpecimens = selectedWorkspaces();
            	
            	if(selectedSpecimens.isEmpty()) {
            		JOptionPane.showMessageDialog(HomePage.this, "You must select at least one specimen !", "Run pipeline", JOptionPane.INFORMATION_MESSAGE);
            		return;
            	}
            	
            	PipelineDialog dialog = new PipelineDialog(HomePage.this, selectedSpecimens);
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				for (PipelineCommand command : dialog.getCommands()) {
					setCurrentWorkspace(command.workspace());
					openPipeline(command);
				}
			}
		});
		panel_4.add(btnAllRunPipeline);
	}
	
	private void addWorkspace(Workspace workspace) {
		
		if(workspaces.stream().anyMatch(w -> w.id().equals(workspace.id())))
			return;
		
		workspaces.add(workspace);		
		final WorkspacePanel workspacePanel = new WorkspacePanel(this, workspace);
		final Collection<JPanel> panels = Collections.synchronizedCollection(new LinkedHashSet<>());
		panels.add(workspacePanel);			
		
		try {
			if(StringUtils.isNotBlank(workspace.log())) {
				final SpecimenBuildingPanel buildingPanel = new SpecimenBuildingPanel(this, workspace);
				panels.add(buildingPanel);					
			}
		} catch (IOException e) {
			log.trace(e.getLocalizedMessage(), e);
			throw new RuntimeException(e);
		}
		
		tabbedPanesByWid.put(workspace.id(), panels);
		model.add(workspace);
		
		if(workspace.state() == JobState.RUNNING) {
			requestNotification(workspace.id());
		}		
	}

	public void loadWorkspaces(List<Workspace> workspaces) {
		
		tabbedPanesByWid.clear();
		this.workspaces.clear();
		model.refresh(new ArrayList<>());
		
		for (Workspace workspace : workspaces) {
			addWorkspace(workspace);
		}

		if(workspaces.isEmpty()) {
			setNoWorkspace();			
		} else {
			// select the first
			table.setRowSelectionInterval(0, 0);
		}
	}
	
	private void setNoWorkspace() {
		tabbedPane.setComponentAt(0, new NoWorkspacePanel());
	}
	
	private void setCurrentWorkspace(Workspace workspace) {
				
		final Collection<JPanel> panels;
		if(tabbedPanesByWid.containsKey(workspace.id())) {			
			panels = tabbedPanesByWid.get(workspace.id());	
		} else
			return;
		
		tabbedPane.removeAll();
		
		Iterator<JPanel> iterator = panels.iterator();
		final JPanel workspacePanel = iterator.next();
		tabbedPane.addTab("Workspace", workspacePanel);
		if(iterator.hasNext()) {
			final JPanel builderPanel = iterator.next();
			tabbedPane.addTab("Building", builderPanel);
			
			// select building by default
			if(workspace.state() == JobState.RUNNING) {
				tabbedPane.setSelectedComponent(builderPanel);		
			}						
		}				
	}
	
	private void refreshWorkspace(Workspace workspace) throws IOException {
				
		if(!tabbedPanesByWid.containsKey(workspace.id())) 
			return;
		
		int selectedIndex = tabbedPane.getSelectedIndex();
		
		final WorkspacePanel workspacePanel = new WorkspacePanel(this, workspace);
		final Collection<JPanel> panels = Collections.synchronizedCollection(new LinkedHashSet<>());
		panels.add(workspacePanel);			
		
		SpecimenBuildingPanel buildingPanel = null;
		if(StringUtils.isNotBlank(workspace.log())) {
			buildingPanel = new SpecimenBuildingPanel(this, workspace);
			panels.add(buildingPanel);
		}
		
		tabbedPanesByWid.put(workspace.id(), panels);
		
		tabbedPane.removeAll();
		
		tabbedPane.addTab("Workspace", workspacePanel);
		if(buildingPanel != null) {
			tabbedPane.addTab("Building", buildingPanel);		
		}	
		
		if(tabbedPane.getComponentCount() >= selectedIndex + 1) {
			tabbedPane.setSelectedIndex(selectedIndex);
		}				
		
		if(workspace.state() == JobState.RUNNING) {
			requestNotification(workspace.id());
		}
		
	}
	
	public void visualize(Workspace workspace, String filePath) {
		final PipelineCommand command = new PipelineCommandImpl(workspace);
		command.put(PipelineStep.VISUALIZE, filePath);
		openPipeline(command);
	}

	public void openPipeline(final PipelineCommand command) {	
		
		final SpecimenBuildingPanel panel;
		final Workspace workspace = command.workspace();
		
		if(tabbedPane.getTabCount() < 2) {
			panel = new SpecimenBuildingPanel(this, workspace);
			tabbedPane.add("Building", panel);
			Collection<JPanel> panels = tabbedPanesByWid.get(workspace.id());
			panels.add(panel);
			tabbedPanesByWid.put(workspace.id(), panels);
		} else {			
			panel = (SpecimenBuildingPanel)tabbedPane.getComponentAt(1);
		}
		
		tabbedPane.setSelectedComponent(panel);
		
		try {
			panel.start(command);
		} catch (IOException e) {
			log.trace(e.getLocalizedMessage(), e);	
			JOptionPane.showMessageDialog(HomePage.this, e.getLocalizedMessage(), "Run pipeline", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void requestRunPipeline(final PipelineCommand command) throws IOException {	
		mainFrame.requestRunPipeline(command);
	}
	
	public void notify(PipelineLogNotification notification) {
		
		if(!tabbedPanesByWid.containsKey(notification.workspaceId()))
			return;
		
		final Collection<JPanel> jpanels = tabbedPanesByWid.get(notification.workspaceId());
		Iterator<JPanel> iterator = jpanels.iterator();
		iterator.next();
		final SpecimenBuildingPanel speBuildingPanel = (SpecimenBuildingPanel)iterator.next();
		
		speBuildingPanel.notify(notification); 
	}
	
	public void notify(PipelineProgressNotification notification) {
		
		if(!tabbedPanesByWid.containsKey(notification.workspaceId()))
			return;
		
		final Collection<JPanel> jpanels = tabbedPanesByWid.get(notification.workspaceId());
		Iterator<JPanel> iterator = jpanels.iterator();
		iterator.next();
		final SpecimenBuildingPanel speBuildingPanel = (SpecimenBuildingPanel)iterator.next();
		
		speBuildingPanel.notify(notification); 
	}
	
	public void notify(WorkspaceStatusNotification notification) {
		model.changeStatus(notification);
	}
	
	public void removeWorkspace(final List<Workspace> workspaces) throws IOException {
		mainFrame.removeWorkspace(workspaces);		
	}
	
	private List<Workspace> selectedWorkspaces(){
		final List<Workspace> selectedSpecimens = new ArrayList<>();
    	for (int i = 0; i < model.getRowCount(); i++) {
			final boolean checked = (boolean)model.getValueAt(i, SpecimenTableModel.SELECTION);
    		if(checked) {
    			selectedSpecimens.add(workspaces.get(i));
			}
		}
    	
    	return selectedSpecimens;
	}
	
	public void cleanWorkspace(String wid) {
		mainFrame.cleanWorkspace(wid);
	}
	
	public void notifyRefreshWorkspace(Workspace workspace) throws IOException {
		model.changeStatus(new WorkspaceStatusNotificationImpl(workspace.id(), workspace.state().name(), workspace.state()));
		refreshWorkspace(workspace);
	}
	
	public void refreshWorkspaceOutput(String wid) {
		mainFrame.refreshWorkspaceOutput(wid);
	}
	
	public void requestNotification(String wid) {
		mainFrame.requestNotification(wid);
	}
}

class SpecimenTableModel extends AbstractTableModel {
	
	public static int NO = 0;
	public static int SELECTION = 1;
	public static int NAME = 2;
	public static int STATE = 3;
	public static int WORKSPACE = 4;
	
	private final transient Logger log = Logger.getLogger("log");
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] columnNames = {"No", "", "Name", "State", "Workspace"};
    private Object[][] data = {};
    private final HomePage ui;
    
    public SpecimenTableModel(final HomePage ui) {  	
    	this.ui = ui;  	
    }
    
    public void refresh(List<Workspace> workspaces) {
    	
    	data = new Object[][] {};
    	
    	int i = 1;    	
    	try{  
    		for (Workspace wp : workspaces) {
        		Object[] newRow = {i++, false, wp.specimen().name(), wp.state().name(), wp};						
    		    data = Arrays.copyOf(data, data.length + 1);
    			data[data.length - 1] = newRow;	
    		}
		}catch(Exception e){ 
			JOptionPane.showMessageDialog(ui, String.format("Error while loading workspaces : %s", e.getLocalizedMessage()), "Loading workspaces", JOptionPane.ERROR_MESSAGE);
		}
    	
    	fireTableDataChanged();
    }
    
    public void add(Workspace wp) {
    	  	
    	try{  
    		Object[] newRow = {data.length + 1, false, wp.specimen().name(), wp.state().name(), wp};						
		    data = Arrays.copyOf(data, data.length + 1);
			data[data.length - 1] = newRow;	
			fireTableRowsInserted(0, newRow.length - 1); 
		}catch(Exception e){ 
			JOptionPane.showMessageDialog(ui, String.format("Error while adding a workspace : %s", e.getLocalizedMessage()), "Loading workspaces", JOptionPane.ERROR_MESSAGE);
		}
    }
    
    public void changeStatus(WorkspaceStatusNotification notification) {
    	
    	synchronized (this) {
    		for (int i = 0; i < data.length; i++) {
        		Object[] row = data[i];
        		Workspace workspace = (Workspace)row[WORKSPACE];
        		if(workspace.id().equals(notification.workspaceId())) {
        			row[STATE] = notification.status();
        			try {
						workspace.setState(notification.state());
					} catch (IOException e) {
						log.trace(e.getLocalizedMessage(), e);
						throw new RuntimeException(e);
					}
        			row[WORKSPACE] = workspace;
        			fireTableCellUpdated(i, STATE);
        			break;
        		}  		
    		}    		
		}
    }
    
    public List<Workspace> itemsSelected() {
    	
    	final List<Workspace> itemsSelected = new ArrayList<>();
    	
    	for (int i = 0; i < data.length; i++) {
    		Object[] row = data[i];
    		if((boolean)row[SELECTION]) {
    			itemsSelected.add((Workspace)row[WORKSPACE]);
    		}  		
		}
    	
    	return itemsSelected;
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        return col == SELECTION;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}
