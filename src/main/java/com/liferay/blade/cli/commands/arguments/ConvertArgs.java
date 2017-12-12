package com.liferay.blade.cli.commands.arguments;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.ConvertCommand;

@Parameters(commandNames = {"convert"},
commandDescription = ConvertCommand.DESCRIPTION)
public class ConvertArgs {


	@Parameter(
		names = {"-a", "--all"},
		description ="Migrate all plugin projects")
	private boolean all;


	public boolean isAll() {
		return all;
	}

	public boolean isList() {
		return list;
	}

	public boolean isThemeBuilder() {
		return themeBuilder;
	}


	@Parameter(
		names = {"-l", "--list"},
		description ="List the projects available to be converted")
	private boolean list;


	@Parameter(
		names = {"-t", "--themeBuilder"},
		description ="Use ThemeBuilder gradle plugin instead of NodeJS to convert theme project")
	private boolean themeBuilder;
	
	

	public String getName() {
		return name;
	}

	@Parameter(
		description ="[name]")
	private String name;
	
}
