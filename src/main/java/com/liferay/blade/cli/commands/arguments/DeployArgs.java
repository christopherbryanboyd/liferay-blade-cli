package com.liferay.blade.cli.commands.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.DeployCommand;

@Parameters(commandNames = {"deploy"},
commandDescription = DeployCommand.DESCRIPTION)
public class DeployArgs {

	@Parameter(
		names = {"-w", "--watch"},
		description =
			"Watches the deployed file for changes and will automatically " +
			"redeploy"
		)
	private boolean watch;

	public boolean isWatch() {
		return watch;
	}
}
