package com.liferay.blade.cli.commands.arguments;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.commands.CreateCommand;


@Parameters(commandNames = {"create"},
commandDescription = CreateCommand.DESCRIPTION)
public class CreateArgs {

	public String getBuild() {
		return build;
	}

	public String getClassname() {
		return classname;
	}

	public String getContributorType() {
		return contributorType;
	}

	public File getDir() {
		return dir;
	}

	public String getHostbundlebsn() {
		return hostbundlebsn;
	}

	public String getHostbundleversion() {
		return hostbundleversion;
	}

	public boolean isListtemplates() {
		return listtemplates;
	}

	public String getPackagename() {
		return packagename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getService() {
		return service;
	}

	public String getTemplate() {
		return template;
	}

	@Parameter(
		names = {"-b", "--build"},
		description =
		"Specify the build type of the project. " +
		"Available options are gradle, maven. (gradle is default)")
	private String build;
	

	@Parameter(
		names = {"-c", "--classname"},
		description =
		"If a class is generated in the project, provide the name of the " +
			"class to be generated. If not provided defaults to Project " +
				"name."
	)
	private String classname;

	@Parameter(
		names = {"C", "-C", "--contributorType"},
		description =
		"Used to identify your module as a Theme Contributor. Also, used " +
		"to add the Liferay-Theme-Contributor-Type and Web-ContextPath " +
		"bundle headers.")
	private String contributorType;

	@Parameter(
		names = {"-d", "--dir"},
		description ="The directory where to create the new project.")
	private File dir;

	@Parameter(
		names = {"-h", "--hostbundlebsn"},
		description =
		"If a new jsp hook fragment needs to be created, provide the name" +
			" of the host bundle symbolic name."
	)
	private String hostbundlebsn;

	@Parameter(
		names = {"-H", "--hostbundleversion"},
		description =
		"If a new jsp hook fragment needs to be created, provide the name" +
			" of the host bundle version."
	)
	private String hostbundleversion;


	@Parameter(
		names = {"-l", "--listtemplates"},
		description ="Prints a list of available project templates")
	private boolean listtemplates;

	@Parameter(
		names = {"-p", "--packagename"},
		description = "")
	private String packagename;

	@Parameter(
		names = {"-s", "--service"},
		description =
		"If a new DS component needs to be created, provide the name of " +
			"the service to be implemented."
	)
	private String service;

	@Parameter(
		names = {"-t", "--template"},
		description =
		"The project template to use when creating the project. To " +
			"see the list of templates available use blade create <-l | " +
				"--listtemplates>"
	)
	private String template;
	
	@Parameter(description="<[name]>")
	private String name;
}
