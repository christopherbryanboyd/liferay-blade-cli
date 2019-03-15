package com.liferay.blade.extensions.deploy.remote.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.liferay.blade.cli.command.BaseArgs;

@Parameters(commandDescription = "Builds and deploys bundles to the Liferay module framework with gogo shell.",
commandNames = "deploy")
public class DeployRemoteArgs extends BaseArgs {

	public boolean isWatch() {
		return _watch;
	}

	@Parameter(
		description = "Watches the deployed file for changes and will automatically redeploy", names = {"-w", "--watch"}
	)
	private boolean _watch;

}
