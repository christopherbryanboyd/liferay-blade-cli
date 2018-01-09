package com.liferay.blade.cli.commands.arguments;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.ShellCommand;

@Parameters(commandNames = {"sh"},
commandDescription = ShellCommand.DESCRIPTION)
public class ShellArgs {

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public List<String> getArgs() {
		return args;
	}

	@Parameter(
		names = {"-p", "--port"},
		description ="The port to use to connect to gogo shell")
	private int port;

	@Parameter(
		names = {"-h", "--host"},
		description ="The host to use to connect to gogo shell")
	private String host;
	
	@Parameter
	private List<String> args = new ArrayList<>();
}
