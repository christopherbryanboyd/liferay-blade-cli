package com.liferay.extensions.maven.command;

import com.liferay.blade.cli.BladeProfile;
import com.liferay.blade.cli.command.InitCommand;

/**
 * @author Christopher Boyd
 */
@BladeProfile("maven")
public class MavenInitCommand extends InitCommand {

	@Override
	public void execute() throws Exception {
		getArgs().setBuild("maven");
		super.execute();
	}


}
