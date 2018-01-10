package com.liferay.blade.cli.commands.arguments;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.SamplesCommand;


@Parameters(commandNames = {"samples"},
commandDescription = SamplesCommand.DESCRIPTION)
public class SamplesArgs {
	
	public File getDir() {
		return dir;
	}
	
	public String getSampleName() {
		return sampleName;
	}

	@Parameter(
		names = {"-d", "--dir"},
		description ="The directory where to create the new project.")
	private File dir;

	@Parameter(description ="[name]")
	private String sampleName;

}
