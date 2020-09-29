package com.ipci.ngs.datacleaner.client.ui;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.jgoodies.forms.layout.FormLayout;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommandImpl;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntryType;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadStats;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.reads.WorkspaceOutput;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.XChartPanel;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.format.DateTimeFormatter;
import java.awt.event.ActionEvent;

public class WorkspacePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTable table;
	private final OutputTableModel model;
	private final JTable tableStats;
	private final StatsTableModel statsModel;
	private final PieChart chart;

	/**
	 * Create the panel.
	 */
	public WorkspacePanel(final HomePage homePage, final Workspace workspace) {
		
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("min(70dlu;default):grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:200px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		JPanel panel = new JPanel();
		add(panel, "2, 2, 3, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblSpecimenProperties = new JLabel("Specimen properties");
		lblSpecimenProperties.setFont(new Font("Tahoma", Font.BOLD, 12));
		panel.add(lblSpecimenProperties, "2, 2");
		
		JLabel lblName = new JLabel("Name :");
		panel.add(lblName, "2, 4");
		
		JLabel lblNameOfFile = new JLabel("Name of file (s)");
		lblNameOfFile.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel.add(lblNameOfFile, "4, 4, 13, 1");
		
		JLabel lblWeight = new JLabel("Weight :");
		panel.add(lblWeight, "2, 6");
		
		JLabel lblWeightInGo = new JLabel("Weight in Go");
		lblWeightInGo.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel.add(lblWeightInGo, "4, 6, 13, 1");
		
		JLabel lblPath = new JLabel("Path :");
		panel.add(lblPath, "2, 8");
		
		JLabel lblAbsolutePath = new JLabel("Absolute path");
		lblAbsolutePath.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel.add(lblAbsolutePath, "4, 8, 13, 1");
		
		JLabel lblType = new JLabel("Type :");
		panel.add(lblType, "2, 10");
		
		JLabel lblSingleOrPaired = new JLabel("Single or paired reads");
		lblSingleOrPaired.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel.add(lblSingleOrPaired, "4, 10, 13, 1");
		
		JLabel lblWorkspace = new JLabel("Workspace path :");
		panel.add(lblWorkspace, "2, 12");
		
		JLabel lblWorkspacePath = new JLabel("Workspace path");
		lblWorkspacePath.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel.add(lblWorkspacePath, "4, 12, 13, 1");
		
		JLabel lblReferences = new JLabel("References :");
		panel.add(lblReferences, "2, 14");
		
		JLabel lblReferencesDetails = new JLabel("References details");
		lblReferencesDetails.setFont(new Font("Tahoma", Font.ITALIC, 11));
		panel.add(lblReferencesDetails, "4, 14, 13, 1");
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3, "2, 16, 15, 1, fill, fill");
		panel_3.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnVisualize = new JButton("Visualize");
		btnVisualize.addActionListener(
			e -> {				
				homePage.visualize(workspace, workspace.settingsFile().out().first().getAbsolutePath());
			}
		);
		
		JButton btnVisualize1 = new JButton("Visualize 1");
		btnVisualize1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				homePage.visualize(workspace, workspace.settingsFile().out().first().getAbsolutePath());
			}
		});
		panel_3.add(btnVisualize1);
		
		JButton btnVisualize2 = new JButton("Visualize 2");
		btnVisualize2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				homePage.visualize(workspace, workspace.settingsFile().out().second().getAbsolutePath());
			}
		});
		panel_3.add(btnVisualize2);
		panel_3.add(btnVisualize);
		
		JButton btnRunPipeline = new JButton("Run pipeline");
		btnRunPipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				PipelineDialog dialog = new PipelineDialog(homePage, Arrays.asList(workspace));
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				for (PipelineCommand command : dialog.getCommands()) {
					homePage.openPipeline(command);
				}			
			}
		});
		
		JButton btnIndexReferences = new JButton("Index references");
		btnIndexReferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final PipelineCommand commandIndex = new PipelineCommandImpl(workspace);
				commandIndex.put(PipelineStep.INDEX_REFERENCE);
				homePage.openPipeline(commandIndex);
				
			}
		});
		panel_3.add(btnIndexReferences);
		panel_3.add(btnRunPipeline);
		
		JPanel panel_1 = new JPanel();
		add(panel_1, "2, 4, 1, 3, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		JLabel lblOutput = new JLabel("Output");
		lblOutput.setFont(new Font("Tahoma", Font.BOLD, 13));
		panel_1.add(lblOutput, "2, 2");
		
		model = new OutputTableModel(this);
		table = new JTable(model) {
		      /**
			 * 
			 */
			private static final long serialVersionUID = 1L;			

			@Override
			public void tableChanged(TableModelEvent e) {
	        super.tableChanged(e);
	        repaint();
	      }
	    };
		
		table.getColumn("").setCellRenderer(new RadioButtonRenderer());
		table.getColumn("").setCellEditor(new RadioButtonEditor(new JCheckBox()));
		table.getColumnModel().getColumn(OutputTableModel.NO).setMaxWidth(30);
		table.getColumnModel().getColumn(OutputTableModel.SELECTION).setMaxWidth(30);
		table.getColumnModel().getColumn(OutputTableModel.FOLDER).setMaxWidth(100);
		table.getColumnModel().getColumn(OutputTableModel.FOLDER).setMinWidth(100);
		table.getColumnModel().getColumn(OutputTableModel.WEIGHT).setMaxWidth(50);
		table.getColumnModel().getColumn(OutputTableModel.WEIGHT).setMinWidth(50);
		table.getColumnModel().getColumn(OutputTableModel.LAST_MODIFICATION_DATE).setMinWidth(170);	
		table.getColumnModel().getColumn(OutputTableModel.LAST_MODIFICATION_DATE).setMaxWidth(170);
		
		table.setFillsViewportHeight(true);
		    
		panel_1.add(new JScrollPane(table), "2, 4, fill, fill");
		
		final TableColumn idClmn= table.getColumn("Path");
    	idClmn.setMaxWidth(0);
    	idClmn.setMinWidth(0);
    	idClmn.setPreferredWidth(0);
    	
    	final TableColumn outClmn= table.getColumn("Output");
    	outClmn.setMaxWidth(0);
    	outClmn.setMinWidth(0);
    	outClmn.setPreferredWidth(0);
		
		lblNameOfFile.setText(String.join(", ", workspace.settingsFile().origin().files().stream().map(c -> c.getName()).collect(Collectors.toList())));	
		
		lblWeightInGo.setText(weightStr(workspace.specimen().weight())); 
		
		lblAbsolutePath.setText(workspace.settingsFile().origin().first().getParentFile().getAbsolutePath());
		lblSingleOrPaired.setText(workspace.specimen().type().name());
		lblWorkspacePath.setText(workspace.path());
		
		lblReferencesDetails.setText(String.join(", ", workspace.references().stream().map(c -> String.format("%s (%s)", c.name(), weightStr(c.weightInGo()))).collect(Collectors.toList())));
		
		JPanel panel_4 = new JPanel();
		add(panel_4, "4, 4, fill, fill");
		panel_4.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		JLabel lblNewLabel = new JLabel("Stats");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
		panel_4.add(lblNewLabel, "2, 2");
		
		statsModel = new StatsTableModel(this);
		tableStats = new JTable(statsModel) {
		      /**
			 * 
			 */
			private static final long serialVersionUID = 1L;			

			@Override
			public void tableChanged(TableModelEvent e) {
	        super.tableChanged(e);
	        repaint();
	      }
	    };
		
	    tableStats.getColumnModel().getColumn(StatsTableModel.NO).setMaxWidth(30);
	    tableStats.getColumnModel().getColumn(StatsTableModel.STEP).setMaxWidth(120);
	    tableStats.getColumnModel().getColumn(StatsTableModel.READ).setMaxWidth(50);
	    tableStats.getColumnModel().getColumn(StatsTableModel.READ_NUMBER).setMinWidth(100);
	    tableStats.getColumnModel().getColumn(StatsTableModel.PERCENT).setMaxWidth(100);
		
		tableStats.setFillsViewportHeight(true);
		panel_4.add(new JScrollPane(tableStats), "2, 4, fill, fill");
		
		JPanel panel_2 = new JPanel();
		add(panel_2, "2, 8, 3, 1");
		panel_2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnVisualize_1 = new JButton("Visualize");
		btnVisualize_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<WorkspaceOutput> outputs = model.itemsSelected();
				
				if(outputs.isEmpty()) {
					JOptionPane.showMessageDialog(WorkspacePanel.this, "You must select at least one file !", "Visualize file", JOptionPane.INFORMATION_MESSAGE);
            		return;
				}
				
				final WorkspaceOutput selected = outputs.get(0);
				
				if(!(selected.name().endsWith("fastq") || selected.name().endsWith("fq") || selected.name().endsWith("fastq.gz") || selected.name().endsWith("fq.gz"))) {
					JOptionPane.showMessageDialog(WorkspacePanel.this, "Only fastq files can be visualized !", "Visualize file", JOptionPane.INFORMATION_MESSAGE);
            		return;
				}
				
				homePage.visualize(workspace, selected.path());
			}
		});
		panel_2.add(btnVisualize_1);
		
		JButton btnClean = new JButton("Clean");
		btnClean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				int reply = JOptionPane.showConfirmDialog(WorkspacePanel.this, "Do you want to clean workspace !", "Clean workspace", JOptionPane.YES_NO_OPTION);
            	if(reply == JOptionPane.NO_OPTION) {
            		return;
            	}
            	
            	homePage.cleanWorkspace(workspace.id());           	
            	
			}
		});
		panel_2.add(btnClean);
		
		JButton btnRefresh = new JButton("Refresh");
		panel_2.add(btnRefresh);
		btnRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				homePage.refreshWorkspaceOutput(workspace.id());
			}
		});
		
		if(workspace.specimen().type() == ReadEntryType.SE) {
			btnVisualize.setVisible(true);
			btnVisualize1.setVisible(false);
			btnVisualize2.setVisible(false);
		} else {
			btnVisualize.setVisible(false);
			btnVisualize1.setVisible(true);
			btnVisualize2.setVisible(true);
		}
		
		// 
		chart = new PieChartBuilder().width(800).height(600).title("Distribution of reads").build();
		
		// Customize Chart
	    Color[] sliceColors = new Color[] { new Color(224, 68, 14), new Color(230, 105, 62), new Color(246, 199, 182) };
	    chart.getStyler().setSeriesColors(sliceColors);
	    
	    // Series
	    chart.addSeries("3D7", 0);
	    chart.addSeries("GH", 0);
	    chart.addSeries("QC", 0);
	    // chart.addSeries("Autres", 0);
	    
	    // chart
	    final JPanel chartPanel = new XChartPanel<PieChart>(chart);
	    add(chartPanel, "4, 6, fill, fill");
	    chartPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		model.refresh(workspace.outputs());
		statsModel.refresh(workspace.stats());
		refreshCharts(workspace.stats());
	}
	
	private void refreshCharts(List<ReadStats> stats) {

		final long readsTotalNumber = stats.stream().filter(c -> c.step().equals("Begin")).findFirst().get().numberOfReads();
		
		final long troisD7ReadsNumber = stats.stream().filter(c -> c.step().equals("Map 3D7")).findFirst().get().numberOfReads();		
	    chart.updatePieSeries("3D7", troisD7ReadsNumber);
	    
	    final long ghReadsNumber = stats.stream().filter(c -> c.step().equals("Map GH")).findFirst().get().numberOfReads();
	    chart.updatePieSeries("GH", ghReadsNumber);
	    
	    long lastQcStepReadNumber = 0L;
	    for (ReadStats readStats : stats) {
			if(readStats.step().equals("Clip") || readStats.step().equals("Filter quality") || readStats.step().equals("Remove Ns") || readStats.step().equals("Min length") || readStats.step().equals("Pair read")) {
				lastQcStepReadNumber = readStats.numberOfReads();
			}
		}
	    	    
	    chart.updatePieSeries("QC", readsTotalNumber - lastQcStepReadNumber);
	    
	    /* final long otherReadsNumber = stats.stream().filter(c -> c.step().equals("Unmap 3D7")).findFirst().get().numberOfReads();
	    chart.updatePieSeries("Autres", otherReadsNumber);*/
	}
	
	public static String weightStr(double weightInGo) {
		
		final String weightStr;
		
		if(weightInGo >= 1) {
			weightStr = String.format("%.0f Go", weightInGo); 
		} else if(weightInGo * 1024 >= 1) {
			weightStr = String.format("%.0f Mo", weightInGo * 1024); 
		} else if(weightInGo * 1024 * 1024 >= 1) {
			weightStr = String.format("%.0f Ko", weightInGo * 1024 * 1024); 
		} else {
			weightStr = String.format("%.0f o", weightInGo * 1024 * 1024 * 1024); 
		}
		
		return weightStr;
	}
}

class OutputTableModel extends AbstractTableModel {
	
	public static int NO = 0;
	public static int SELECTION = 1;
	public static int FOLDER = 2;
	public static int FILE = 3;
	public static int WEIGHT = 4;
	public static int LAST_MODIFICATION_DATE = 5;
	public static int PATH = 6;
	public static int OUTPUT = 7;
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] columnNames = {"No", "", "Folder", "File", "Weight", "Last modified", "Path", "Output"};
    private Object[][] data = {};
    private final WorkspacePanel ui;
    
    public OutputTableModel(final WorkspacePanel ui) {  	
    	this.ui = ui;  	
    }
    
    public void refresh(List<WorkspaceOutput> outputs) {
    	
    	data = new Object[][] {};
    	
    	int i = 0;    	
    	try{  
    		DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    		
    		ButtonGroup group1 = new ButtonGroup();
    		for (WorkspaceOutput output : outputs) {
    			final JRadioButton rb = new JRadioButton();
    			if(i == 0) {
    				rb.setSelected(true);    				
    			}
    			group1.add(rb);
        		Object[] newRow = {i + 1, rb, output.folder(), output.name(), WorkspacePanel.weightStr(output.weightInGo()), formatter.format(output.lastModificationDate()), output.path(), output};						
    		    data = Arrays.copyOf(data, data.length + 1);
    			data[data.length - 1] = newRow;	
    			i++;
    		}
		}catch(Exception e){ 
			JOptionPane.showMessageDialog(ui, String.format("Error while loading workspaces : %s", e.getLocalizedMessage()), "Loading workspaces", JOptionPane.ERROR_MESSAGE);
		}
    	
    	fireTableDataChanged();
    }
    
    public List<WorkspaceOutput> itemsSelected() {
    	
    	final List<WorkspaceOutput> itemsSelected = new ArrayList<>();
    	
    	for (int i = 0; i < data.length; i++) {
    		Object[] row = data[i];
    		if(((JRadioButton)row[SELECTION]).isSelected()) {
    			itemsSelected.add((WorkspaceOutput)row[OUTPUT]);
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

class StatsTableModel extends AbstractTableModel {
	
	public static int NO = 0;
	public static int STEP = 1;
	public static int READ = 2;
	public static int READ_NUMBER = 3;
	public static int PERCENT = 4;
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] columnNames = {"No", "Step", "Read", "Number of reads", "Percent"};
    private Object[][] data = {};
    private final WorkspacePanel ui;
    
    public StatsTableModel(final WorkspacePanel ui) {  	
    	this.ui = ui;  	
    }
    
    public void refresh(List<ReadStats> items) {
    	
    	data = new Object[][] {};
    	
    	int i = 0;    	
    	try{  
   		
    		for (ReadStats item : items) {
        		Object[] newRow = { i + 1, item.step(), item.name(), item.numberOfReads(), item.percent() };						
    		    data = Arrays.copyOf(data, data.length + 1);
    			data[data.length - 1] = newRow;	
    			i++;
    		}
		}catch(Exception e){ 
			JOptionPane.showMessageDialog(ui, String.format("Error while loading stats : %s", e.getLocalizedMessage()), "Loading stats", JOptionPane.ERROR_MESSAGE);
		}
    	
    	fireTableDataChanged();
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
}

class RadioButtonRenderer implements TableCellRenderer {
	  public Component getTableCellRendererComponent(JTable table, Object value,
	      boolean isSelected, boolean hasFocus, int row, int column) {
	    if (value == null)
	      return null;
	    return (Component) value;
	  }
	}

	class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
	  /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	private JRadioButton button;

	  public RadioButtonEditor(JCheckBox checkBox) {
	    super(checkBox);
	  }

	  public Component getTableCellEditorComponent(JTable table, Object value,
	      boolean isSelected, int row, int column) {
	    if (value == null)
	      return null;
	    button = (JRadioButton) value;
	    button.addItemListener(this);
	    return (Component) value;
	  }

	  public Object getCellEditorValue() {
	    button.removeItemListener(this);
	    return button;
	  }

	  public void itemStateChanged(ItemEvent e) {
	    super.fireEditingStopped();
	  }
	}
