package com.ipci.ngs.datacleaner.client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommandImpl;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntryType;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.ipci.ngs.datacleaner.commonlib.utilities.PipelineStep;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class PipelineDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private final JCheckBox chckbxClip;
	private final JSpinner spinnerClipBegin;
	private final JSpinner spinnerClipEnd;
	private final JCheckBox chckbxFilterQuality;
	private final JSpinner spinnerLevel;
	private final JCheckBox chckbxRemoveNs;
	private final JSpinner spinnerNmax;
	private final JCheckBox chckbxPairedReads;
	private final JCheckBox chckbxUnmappedGh;
	private final JCheckBox chckbxMappedd;
	private final JCheckBox chckbxMarkDuplicated;
	private final List<PipelineCommand> commands;
	private final JSpinner spinnerMinLength;
	private final JCheckBox chckbxApplyMinLength;
	private final JCheckBox chckbxDeleteIntermediateFiles;
	
	/**
	 * Create the dialog.
	 */
	public PipelineDialog(final HomePage homePage, final List<Workspace> workspaces) {
		setResizable(false);
		
		this.commands = new ArrayList<>();
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Pipeline settings");
		setBounds(100, 100, 520, 369);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("50px"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("50px"),},
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
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		{
			JLabel lblStep = new JLabel("Step 1 :");
			contentPanel.add(lblStep, "2, 2");
		}
		{
			chckbxClip = new JCheckBox("Clip");
			chckbxClip.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					spinnerClipBegin.setEnabled(chckbxClip.isSelected());
					spinnerClipEnd.setEnabled(chckbxClip.isSelected());
				}
			});
			chckbxClip.setSelected(true);
			contentPanel.add(chckbxClip, "4, 2");
		}
		{
			JLabel lblBegin = new JLabel("Begin :");
			contentPanel.add(lblBegin, "8, 2");
		}
		{
			spinnerClipBegin = new JSpinner();
			spinnerClipBegin.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
			contentPanel.add(spinnerClipBegin, "10, 2");
		}
		{
			JLabel lblEnd = new JLabel("End :");
			contentPanel.add(lblEnd, "14, 2");
		}
		{
			spinnerClipEnd = new JSpinner();
			spinnerClipEnd.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
			contentPanel.add(spinnerClipEnd, "16, 2");
		}
		{
			JLabel lblStep_1 = new JLabel("Step 2 :");
			contentPanel.add(lblStep_1, "2, 4");
		}
		{
			chckbxFilterQuality = new JCheckBox("Filter quality");
			chckbxFilterQuality.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					spinnerLevel.setEnabled(chckbxFilterQuality.isSelected());
				}
			});
			chckbxFilterQuality.setSelected(true);
			contentPanel.add(chckbxFilterQuality, "4, 4");
		}
		{
			JLabel lblLevel = new JLabel("Level :");
			contentPanel.add(lblLevel, "8, 4");
		}
		{
			spinnerLevel = new JSpinner();
			spinnerLevel.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
			contentPanel.add(spinnerLevel, "10, 4");
		}
		{
			JLabel lblStep_2 = new JLabel("Step 3 :");
			contentPanel.add(lblStep_2, "2, 6");
		}
		{
			chckbxRemoveNs = new JCheckBox("Remove Ns");
			chckbxRemoveNs.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					spinnerNmax.setEnabled(chckbxRemoveNs.isSelected());
				}
			});
			chckbxRemoveNs.setSelected(true);
			contentPanel.add(chckbxRemoveNs, "4, 6");
		}
		{
			JLabel lblNmax = new JLabel("Nmax :");
			contentPanel.add(lblNmax, "8, 6");
		}
		{
			spinnerNmax = new JSpinner();
			spinnerNmax.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
			contentPanel.add(spinnerNmax, "10, 6");
		}
		{
			JLabel lblNewLabel = new JLabel("Step 4 :");
			contentPanel.add(lblNewLabel, "2, 8");
		}
		{
			chckbxApplyMinLength = new JCheckBox("Apply min length :");
			chckbxApplyMinLength.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					spinnerMinLength.setEnabled(chckbxApplyMinLength.isSelected());
				}
			});
			chckbxApplyMinLength.setSelected(true);
			contentPanel.add(chckbxApplyMinLength, "4, 8");
		}
		{
			JLabel lblNewLabel_1 = new JLabel("Min length : ");
			contentPanel.add(lblNewLabel_1, "8, 8");
		}
		{
			spinnerMinLength = new JSpinner();
			spinnerMinLength.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
			contentPanel.add(spinnerMinLength, "10, 8");
		}
		{
			JLabel lblStep_3 = new JLabel("Step 5 :");
			contentPanel.add(lblStep_3, "2, 10");
		}
		{
			chckbxPairedReads = new JCheckBox("Paired reads");
			
			boolean doPairRead = false;
			for (Workspace workspace : workspaces) {
				if(workspace.settingsFile().origin().type() == ReadEntryType.PE) {
					doPairRead = true;
					break;
				}
			}
			
			chckbxPairedReads.setSelected(doPairRead);
			chckbxPairedReads.setEnabled(doPairRead);
			
			contentPanel.add(chckbxPairedReads, "4, 10");
		}
		{
			JLabel lblStep_4 = new JLabel("Step 6 :");
			contentPanel.add(lblStep_4, "2, 12");
		}
		{
			chckbxUnmappedGh = new JCheckBox("Unmapped GH");
			chckbxUnmappedGh.setSelected(true);
			contentPanel.add(chckbxUnmappedGh, "4, 12");
		}
		{
			JLabel lblStep_5 = new JLabel("Step 7 :");
			contentPanel.add(lblStep_5, "2, 14");
		}
		{
			chckbxMappedd = new JCheckBox("Mapped 3D7");
			chckbxMappedd.setSelected(true);
			contentPanel.add(chckbxMappedd, "4, 14");
		}
		{
			JLabel lblStep_6 = new JLabel("Step 8 :");
			lblStep_6.setVisible(false);			
			contentPanel.add(lblStep_6, "2, 16");
		}
		{
			chckbxMarkDuplicated = new JCheckBox("Mark duplicated");
			chckbxMarkDuplicated.setSelected(false);
			chckbxMarkDuplicated.setVisible(false);
			contentPanel.add(chckbxMarkDuplicated, "4, 16");
		}
		{
			chckbxDeleteIntermediateFiles = new JCheckBox("Delete intermediate files");
			chckbxDeleteIntermediateFiles.setSelected(true);
			chckbxDeleteIntermediateFiles.setHorizontalAlignment(SwingConstants.RIGHT);
			contentPanel.add(chckbxDeleteIntermediateFiles, "8, 18, 9, 1");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Start");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {

						for (Workspace workspace : workspaces) {
							
							final PipelineCommand command = new PipelineCommandImpl(workspace);
							command.deleteIntermediateFiles(chckbxDeleteIntermediateFiles.isSelected());
							
							if(chckbxClip.isSelected()) {
								command.put(PipelineStep.CLIP, spinnerClipBegin.getValue(), spinnerClipEnd.getValue());
							}
							
							if(chckbxFilterQuality.isSelected()) {
								command.put(PipelineStep.FILTER_QUALITY, spinnerLevel.getValue());
							}
							
							if(chckbxRemoveNs.isSelected()) {
								command.put(PipelineStep.REMOVE_NS, spinnerNmax.getValue());
							}
							
							if(chckbxApplyMinLength.isSelected()) {
								command.put(PipelineStep.MIN_LENGTH, spinnerMinLength.getValue());
							}
							
							if(chckbxPairedReads.isSelected()) {
								command.put(PipelineStep.PAIRED_READS);
							}
							
							if(chckbxUnmappedGh.isSelected()) {
								command.put(PipelineStep.UNMAPPED_GH);
							}
							
							if(chckbxMappedd.isSelected()) {
								command.put(PipelineStep.MAPPED_3D7);
							}
							
							if(chckbxMarkDuplicated.isSelected()) {
								command.put(PipelineStep.MARK_DUPLICATED);
							}
							
							commands.add(command);
						}
												
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public List<PipelineCommand> getCommands() {
		return commands;
	}

}
