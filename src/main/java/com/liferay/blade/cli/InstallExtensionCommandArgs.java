package com.liferay.blade.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Christopher Bryan Boyd
 */
@Parameters(commandDescription = InstallCommand.DESCRIPTION, commandNames = {"installExtension"})
public class InstallExtensionCommandArgs extends BaseArgs {

	@Parameter(description = "The path to the extension to install")
	private String _path;

	public String getPath() {
		return _path;
	}
}
