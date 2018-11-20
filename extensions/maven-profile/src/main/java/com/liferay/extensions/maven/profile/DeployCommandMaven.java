/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.extensions.maven.profile;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.command.BladeProfile;
import com.liferay.blade.cli.command.DeployArgs;
import com.liferay.blade.cli.command.DeployCommand;
import com.liferay.blade.cli.gradle.ProcessResult;
import com.liferay.blade.cli.util.WorkspaceUtil;
import com.liferay.extensions.maven.profile.internal.MavenUtil;

/**
 * @author Gregory Amerson
 */
@BladeProfile("maven")
public class DeployCommandMaven extends DeployCommand {

	public DeployCommandMaven() {
	}

	@Override
	public void execute() throws Exception {

		DeployArgs deployArgs = getArgs();

		File baseDir = new File(deployArgs.getBase());

		if (WorkspaceUtil.isWorkspace(baseDir)) {
			_deploy();

		} else {


			//_deployStandalone(gradleExec, projectInfo);
		}
	}

	@Override
	public Class<DeployArgs> getArgsClass() {
		return DeployArgs.class;
	}

	private void _addError(String msg) {
		getBladeCLI().addErrors("deploy", Collections.singleton(msg));
	}

	private void _deploy() throws Exception {
		DeployArgs deployArgs = getArgs();

		File baseDir = new File(deployArgs.getBase());
		
		String[] goals = {"clean", "package", "bundle-support:deploy"};

		ProcessResult processResult = MavenUtil.executeGoals(baseDir.getAbsolutePath(), false, goals);

		int resultCode = processResult.getResultCode();

		BladeCLI bladeCLI = getBladeCLI();

		if (resultCode > 0) {
			String errorMessage = "Maven " + goals + " goals failed.";

			_addError(errorMessage);

			PrintStream err = bladeCLI.error();

			new IOException(errorMessage).printStackTrace(err);

			return;
		} else {

			String output = "Maven " + goals + " goals succeeded.";

			bladeCLI.out(output);
		}
	}

}