package com.ipci.ngs.datacleaner.client.ui;

import javax.swing.JPanel;
import com.jgoodies.forms.layout.FormLayout;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineCommand;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotification;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineLogNotificationImpl;
import com.ipci.ngs.datacleaner.commonlib.pipeline.PipelineProgressNotification;
import com.ipci.ngs.datacleaner.commonlib.reads.Workspace;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SpecimenBuildingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final transient Logger log = Logger.getLogger("log");
	private final String NEWLINE = System.getProperty("line.separator");
	
	private final HomePage homePage;
	@SuppressWarnings("unused")
	private final Workspace workspace;
	private final JLabel lblProgressStatus;
	private final JProgressBar progressBar;
	private final JTextArea textArea;
	
	/**
	 * Create the panel.
	 */
	public SpecimenBuildingPanel(final HomePage homePage, final Workspace workspace) {
		
		this.homePage = homePage;
		this.workspace = workspace;
		
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(302dlu;default):grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		
		lblProgressStatus = new JLabel("Progress status");
		add(lblProgressStatus, "2, 2");
		
		progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		add(progressBar, "2, 4");
		
		JLabel lblOutput = new JLabel("Output");
		add(lblOutput, "2, 6");
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		add(new JScrollPane(textArea), "2, 8, fill, fill");
	
		try {
			notify(new PipelineLogNotificationImpl(workspace.id(), workspace.log()));
		} catch (IOException e) {
			log.trace(e.getLocalizedMessage(), e);
			JOptionPane.showMessageDialog(SpecimenBuildingPanel.this, e.getLocalizedMessage(), "Loading building specimen", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void start(PipelineCommand command) throws IOException {
		lblProgressStatus.setText("Initializing..."); 		
		setProgressBarVisible(true);	
		textArea.setText("");
		homePage.requestRunPipeline(command);
	}
	
	public void setProgressBarVisible(boolean visible) {
		lblProgressStatus.setVisible(visible);
		progressBar.setVisible(visible);
	}
	
	public void notify(PipelineLogNotification notification) {
		
		if(StringUtils.isBlank(notification.log().trim())) {
			textArea.append(NEWLINE);
		} else {
			textArea.append(notification.log());
		}	
        
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
        
        setProgressBarVisible(progressBar.getValue() < 100);
	}
	
	public void notify(PipelineProgressNotification notification) {	
		lblProgressStatus.setText(String.format("Executing task -> %s (%d ", notification.action(), notification.progressPercent()) + "%)");
		progressBar.setValue(notification.progressPercent()); 
		
		setProgressBarVisible(notification.progressPercent() < 100);
	}
}
