package com.ipci.ngs.datacleaner.client.ui;

import javax.swing.JPanel;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

public class NoWorkspacePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; 

	/**
	 * Create the panel.
	 */
	public NoWorkspacePanel() {
		setLayout(new GridLayout(1, 0, 0, 0));
		
		JLabel lblNoWorkspace = new JLabel("No workspace selected.");
		lblNoWorkspace.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblNoWorkspace.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblNoWorkspace);

	}

}
