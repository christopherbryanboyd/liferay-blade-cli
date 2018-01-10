package com.liferay.blade.cli.commands.arguments;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.ConvertCommand;

@Parameters(commandNames = {"convert"},
commandDescription = ConvertCommand.DESCRIPTION)
public class ConvertArgs {

	public ConvertArgs() {
		super();
	}
	
	public ConvertArgs(boolean all, boolean list, boolean themeBuilder, List<String> name) {
		super();
		this.all = all;
		this.list = list;
		this.themeBuilder = themeBuilder;
		this.name = name;
	}
	
	public boolean isAll() {
		return all;
	}

	public boolean isList() {
		return list;
	}

	public boolean isThemeBuilder() {
		return themeBuilder;
	}
	
	public List<String> getName() {
		return name;
	}

	@Parameter(
		names = {"-a", "--all"},
		description ="Migrate all plugin projects")
	private boolean all;

	@Parameter(
		names = {"-l", "--list"},
		description ="List the projects available to be converted")
	private boolean list;

	@Parameter(
		names = {"-t", "--themeBuilder"},
		description ="Use ThemeBuilder gradle plugin instead of NodeJS to convert theme project")
	private boolean themeBuilder;
	
	@Parameter(
		description ="[name]")
	private List<String> name = new ArrayList<>();
	
}
