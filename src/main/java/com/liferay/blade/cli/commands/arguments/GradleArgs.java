package com.liferay.blade.cli.commands.arguments;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.GradleCommand;

@Parameters(commandNames = {"gw"},
commandDescription = GradleCommand.DESCRIPTION)
public class GradleArgs {

	public List<String> getArgs() {
		return args;
	}

	@Parameter(description="[arguments]")
	private List<String> args = new ArrayList<>();
}
