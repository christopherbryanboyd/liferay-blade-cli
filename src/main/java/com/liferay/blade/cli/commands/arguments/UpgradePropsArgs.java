package com.liferay.blade.cli.commands.arguments;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.UpgradePropsCommand;

@Parameters(commandNames = {"upgradeProps"},
commandDescription = UpgradePropsCommand.DESCRIPTION)
public class UpgradePropsArgs {

	public File getBundleDir() {
		return bundleDir;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public File getPropertiesFile() {
		return propertiesFile;
	}

	@Parameter(
		names = {"-d", "--bundleDir"},
		description ="Liferay server bundle directory.")
	private File bundleDir;

	@Parameter(
		names = {"-o", "--outputFile"},
		description ="If specified, write out report to this file, otherwise uses stdout.")
	private File outputFile;

	@Parameter(
		names = {"-p", "--propertiesFile"},
		description ="Specify existing Liferay 6.x portal-ext.properties file.")
	private File propertiesFile;
	

}
