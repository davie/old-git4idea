package com.assembla.git;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class GitRevisionGraphEditorProvider implements FileEditorProvider
{
	public boolean accept( @NotNull Project project, @NotNull VirtualFile file )
	{
		return file instanceof GitVirtualFile &&
				((GitVirtualFile) file).getPrefix().equals( GitFileSystem.PREFIX_REV_GRAPH );
	}

	@NotNull
	public FileEditor createEditor( @NotNull Project project, @NotNull VirtualFile file )
	{
		throw new UnsupportedOperationException( "Method createEditor not implemented in " + getClass() );
	}

	public void disposeEditor( @NotNull FileEditor editor )
	{
		throw new UnsupportedOperationException( "Method disposeEditor not implemented in " + getClass() );
	}

	@NotNull
	public FileEditorState readState( @NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file )
	{
		throw new UnsupportedOperationException( "Method readState not implemented in " + getClass() );
	}

	public void writeState( @NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement )
	{
		throw new UnsupportedOperationException( "Method writeState not implemented in " + getClass() );
	}

	@NotNull
	@NonNls
	public String getEditorTypeId()
	{
		return "RevisionGraph";
	}

	@NotNull
	public FileEditorPolicy getPolicy()
	{
		throw new UnsupportedOperationException( "Method getPolicy not implemented in " + getClass() );
	}
}
