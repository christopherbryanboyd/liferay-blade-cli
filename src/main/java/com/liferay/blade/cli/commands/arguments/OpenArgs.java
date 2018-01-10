package com.liferay.blade.cli.commands.arguments;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.OpenCommand;

@Parameters(commandNames = {"open"},
commandDescription = OpenCommand.DESCRIPTION)
public class OpenArgs {

	public String getWorkspace() {
		return workspace;
	}

	public File getFile() {
		return file;
	}

	@Parameter(
			names = {"-w", "--workspace"},
			description ="The workspace to open or import this file or project")
	private String workspace;
	
	@Parameter(description ="<file or directory to open/import>")
	private File file;
}
