package com.ipci.ngs.datacleaner.client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SelectSpecimensDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private final JCheckBox chckbxSelectionnerTout;
	private List<ReadEntry> entries;
	private final DefaultTableModel model;
	private JTextField textFieldPath;
	private final MainFrame mainFrame;
	/**
	 * Create the dialog.
	 * @throws IOException
	 */
	public SelectSpecimensDialog(final MainFrame mainFrame) {
		super(mainFrame);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.mainFrame = mainFrame;
		
		setTitle("Select specimens"); 
		setBounds(100, 100, 450, 439);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		{
			JLabel lblPath = new JLabel("Path");
			contentPanel.add(lblPath, "2, 2, right, default");
		}
		{
			textFieldPath = new JTextField();
			contentPanel.add(textFieldPath, "4, 2, fill, default");
			textFieldPath.setColumns(10);
		}
		{
			JButton btnRechercher = new JButton("Rechercher");
			btnRechercher.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
					mainFrame.searchSpecimens(textFieldPath.getText().trim());					
				}
			});
			contentPanel.add(btnRechercher, "6, 2");
		}
		{
			chckbxSelectionnerTout = new JCheckBox("S\u00E9lectionner tout");
			contentPanel.add(chckbxSelectionnerTout, "2, 4, 5, 1");
			chckbxSelectionnerTout.addItemListener(new ItemListener() {
		      public void itemStateChanged(ItemEvent e) {
		    	
		        for(int i = 0; i < table.getRowCount(); i++) {
		        	table.setValueAt(chckbxSelectionnerTout.isSelected(), i, 0);
		        }
		      }
		    });
		}
		{
			table = new JTable();
			
			//THE MODEL OF OUR TABLE
		    model = new DefaultTableModel()
		    {
		      /**
			   * 
			  */
				private static final long serialVersionUID = 1L;

			public Class<?> getColumnClass(int column)
		      {
		        switch(column)
		        {
			        case 0:
			          return Boolean.class;
			        case 1:
			          return String.class;
			        case 2:
			          return String.class;
			        default:
			            return String.class;
		        }
		      }
		    };
		    
			table.setModel(model);
			model.addColumn("Coché");
		    model.addColumn("Libellé");
		    model.addColumn("Nature");
		    
		    final JScrollPane scrollpane = new JScrollPane(table);
			contentPanel.add(scrollpane, "2, 6, 5, 1, fill, fill");			
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				okButton.addActionListener((e) -> {
					final List<ReadEntry> selectedReads = new ArrayList<>();
	            	for (int i = 0; i < model.getRowCount(); i++) {
						final boolean checked = (boolean)model.getValueAt(i, 0);
	            		if(checked) {
	            			selectedReads.add(entries.get(i));
						}
					}
	            	
	            	if(selectedReads.isEmpty()) {
	            		JOptionPane.showMessageDialog(SelectSpecimensDialog.this, "No specimen selected !", "Select specimens", JOptionPane.INFORMATION_MESSAGE);
	            		return;
	            	}
	            	
	            	try {
						mainFrame.requestCreateWorkspaces(selectedReads);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(SelectSpecimensDialog.this, e1.getLocalizedMessage(), "Select specimens", JOptionPane.ERROR_MESSAGE);
					}	                
				});
				
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent arg0) {
		                setVisible(false);
		                dispose();
		            }
		        });
				buttonPane.add(cancelButton);
			}
		}
	}

	public List<ReadEntry> reads(){
		final List<ReadEntry> selectedItems = new ArrayList<>();
		for (int i = 0; i < model.getRowCount(); i++) {
			if((boolean)model.getValueAt(i, 0)) {
				selectedItems.add(entries.get(i));
			}		
		}
		
		return selectedItems;
	}
	
	public void loadSpecimens(List<ReadEntry> specimens) {
		
		model.setRowCount(0);
		chckbxSelectionnerTout.setSelected(false);
		
		entries = specimens;	
		for (int j = 0; j < entries.size(); j++) {
			final ReadEntry entry = entries.get(j);
			
			model.addRow(new Object[0]);
		    model.setValueAt(false, j, 0);
		    model.setValueAt(entry.name(), j, 1);
		    model.setValueAt(entry.type().name(), j, 2);
		}
	}
	
	public void accept() {		
		mainFrame.requestLoadWorkspaces();
		setVisible(false);
        dispose();
	}
}
