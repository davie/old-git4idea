package com.assembla.git;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author mike
 */
public class GitVcsPanel
{
	private JButton testButton;
	private JComponent panel;
	private TextFieldWithBrowseButton pathField;
	private Project project;

	public GitVcsPanel( Project project )
	{
		this.project = project;
		testButton.addActionListener(
				new ActionListener()
				{
					public void actionPerformed( ActionEvent e )
					{
						testConnection();
					}
				} );

		pathField.addBrowseFolderListener(
				"Git Configuration",
				"Select path to hg executable",
				project,
				new FileChooserDescriptor( true, false, false, false, false, false ) );
	}

	private void testConnection()
	{
		final GitVcsSettings settings = new GitVcsSettings();
		settings.GIT_EXECUTABLE = pathField.getText();
        final VirtualFile baseDir = project.getBaseDir();
        assert baseDir != null;
        final com.assembla.git.commands.GitCommand command = new com.assembla.git.commands.GitCommand( project, settings, baseDir);
		final String s;

		try
		{
			s = command.version();
		}
		catch( VcsException e )
		{
			Messages.showErrorDialog( project, e.getMessage(), "Error Running hg" );
			return;
		}
		Messages.showInfoMessage( project, s, "Hg Executed Successfully" );
	}

	public JComponent getPanel()
	{
		return panel;
	}

	public void load( GitVcsSettings settings )
	{
		pathField.setText( settings.GIT_EXECUTABLE);
	}

	public boolean isModified( GitVcsSettings settings )
	{
		if( !settings.GIT_EXECUTABLE.equals( pathField.getText() ) )
			return true;
		return false;
	}

	public void save( GitVcsSettings settings )
	{
		settings.GIT_EXECUTABLE = pathField.getText();
	}
}
