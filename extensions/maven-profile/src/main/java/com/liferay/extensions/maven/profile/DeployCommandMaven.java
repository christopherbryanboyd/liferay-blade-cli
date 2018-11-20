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

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.command.BladeProfile;
import com.liferay.blade.cli.command.DeployArgs;
import com.liferay.blade.cli.command.DeployCommand;
import com.liferay.blade.cli.gradle.GradleTooling;
import com.liferay.blade.cli.util.WorkspaceUtil;
import com.liferay.blade.gradle.tooling.ProjectInfo;
import com.liferay.extensions.maven.profile.internal.MavenUtil;

import java.io.File;

import java.util.Collections;

/**
 * @author Gregory Amerson
 */
@BladeProfile("maven")
public class DeployCommandMaven extends DeployCommand {

	public DeployCommandMaven() {
	}

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		DeployArgs deployArgs = getArgs();

		File baseDir = new File(deployArgs.getBase());

		if (WorkspaceUtil.isWorkspace(baseDir)) {
			_deploy("deploy");

		} else {

			ProjectInfo projectInfo = GradleTooling.loadProjectInfo(baseDir.toPath());

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

	private void _deploy(String command) throws Exception {
		DeployArgs deployArgs = getArgs();

		File baseDir = new File(deployArgs.getBase());

		MavenUtil.executeGoals(baseDir.getAbsolutePath(), new String[] {"clean", "package", "bundle-support:deploy"});

		/*ProcessResult processResult = gradle.executeTask(command, baseDir, false);

		int resultCode = processResult.getResultCode();

		BladeCLI bladeCLI = getBladeCLI();

		if (resultCode > 0) {
			String errorMessage = "Maven " + command + " task failed.";

			_addError(errorMessage);

			PrintStream err = bladeCLI.error();

			new ConnectException(errorMessage).printStackTrace(err);

			return;
		} else {

			String output = "Gradle " + command + " task succeeded.";

			bladeCLI.out(output);
		}*/
	}

}