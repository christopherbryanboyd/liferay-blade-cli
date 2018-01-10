package com.liferay.blade.cli.commands.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.InitCommand;

@Parameters(commandNames = {"init"},
commandDescription = InitCommand.DESCRIPTION)
public class InitArgs {

	@Parameter(
			description =
			"[name]")
	private String name;
	public String getName() {
		return name;
	}


	@Parameter(
			names = {"-f", "--force"},
			description =
			"create anyway if there are files located at target folder")
	private boolean force;


	@Parameter(
		names = {"-r", "--refresh"},
		description ="force to refresh workspace template")
	private boolean refresh;


	public boolean isForce() {
		return force;
	}


	public boolean isRefresh() {
		return refresh;
	}


	public boolean isUpgrade() {
		return upgrade;
	}


	@Parameter(
		names = {"-u", "--upgrade"},
		description ="upgrade plugins-sdk from 6.2 to 7.0")
	private boolean upgrade;
}
