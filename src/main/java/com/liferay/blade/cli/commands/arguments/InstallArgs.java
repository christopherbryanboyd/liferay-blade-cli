package com.liferay.blade.cli.commands.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.InstallCommand;

@Parameters(commandNames = {"install"},
commandDescription = InstallCommand.DESCRIPTION)
public class InstallArgs {
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getBundleFileName() {
		return bundleFileName;
	}
	
	@Parameter(
		names = {"-h", "--host"},
		description ="The host to use to connect to gogo shell")
	private String host;


	@Parameter(
		names = {"-p", "--port"},
		description ="The port to use to connect to gogo shell")
	private int port;
	
	@Parameter(description ="Bundle File Name")
	private String bundleFileName;
}
