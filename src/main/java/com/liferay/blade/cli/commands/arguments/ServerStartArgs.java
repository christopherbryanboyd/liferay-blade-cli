package com.liferay.blade.cli.commands.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.ServerStartCommand;


@Parameters(commandNames = {"server start"},
commandDescription = ServerStartCommand.DESCRIPTION)
public class ServerStartArgs {
	
	@Parameter(
			names = {"-b", "--background"},
			description ="Start server in background")
	private boolean background;

	public boolean isBackground() {
		return background;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isTail() {
		return tail;
	}

	@Parameter(
			names = {"-d", "--debug"},
			description ="Start server in debug mode")
	private boolean debug;

	@Parameter(
			names = {"-t", "--tail"},
			description ="Tail a running server")
	private boolean tail;

	
}
