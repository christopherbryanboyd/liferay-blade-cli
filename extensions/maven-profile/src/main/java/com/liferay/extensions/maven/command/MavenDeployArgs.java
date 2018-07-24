package com.liferay.extensions.maven.command;

import com.beust.jcommander.Parameters;
import com.liferay.blade.cli.command.DeployArgs;

/**
 * @author Christopher Bryan Boyd
 */
@Parameters(
	commandDescription = "Builds and deploys maven bundles to the Liferay module framework.", 
	commandNames = "deploy"
)
public class MavenDeployArgs extends DeployArgs {

}
