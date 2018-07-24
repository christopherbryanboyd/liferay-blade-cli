package com.liferay.extensions.maven.command;

import com.liferay.blade.cli.BladeProfile;
import com.liferay.blade.cli.command.CreateCommand;

/**
 * @author Christopher Boyd
 */
@BladeProfile("maven")
public class MavenCreateCommand extends CreateCommand {

	@Override
	public void execute() throws Exception {
		getArgs().setBuild("maven");
		super.execute();
	}

}
