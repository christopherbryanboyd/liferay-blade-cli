package com.liferay.blade.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "List of available Blade Extensions.", commandNames = "extension list")
public class ListExtensionsCommandArgs extends BaseArgs {
	@Parameter(description = "The path to the extensions.xml file")
	private String path;

	public String getPath() {
		return path;
	}
	
}
