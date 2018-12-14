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

package com.liferay.blade.extensions.maven.profile;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.command.BaseArgs;
import com.liferay.blade.cli.command.BladeProfile;
import com.liferay.blade.cli.command.DeployArgs;
import com.liferay.blade.cli.command.DeployCommand;
import com.liferay.blade.cli.gradle.ProcessResult;
import com.liferay.blade.extensions.maven.profile.internal.MavenUtil;

/**
 * @author Gregory Amerson
 * @author Christopher Bryan Boyd
 */
@BladeProfile("maven")
public class DeployCommandMaven extends DeployCommand {

	public DeployCommandMaven() {
	}

	@Override
	public void execute() throws Exception {
		DeployArgs deployArgs = getArgs();

		File baseDir = new File(deployArgs.getBase());

		if (MavenUtil.isWorkspace(baseDir)) {
			_deploy();
		}
		else {
			_deployStandalone(baseDir);
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
		}
		else {
			String output = "Maven " + goals + " goals succeeded.";

			bladeCLI.out(output);
		}
	}

	private void _deployStandalone(File baseDir) {
		Properties properties = MavenUtil.getMavenProperties(baseDir);

		if (!properties.containsKey("liferay.home")) {
			throw new NoSuchElementException("\"liferay.home\" not defined in pom.xml, cannot deploy");
		}

		String liferayHome = properties.getProperty("liferay.home");

		Path liferayHomePath = Paths.get(liferayHome);

		if (!Files.exists(liferayHomePath)) {
			IOException ioException = new IOException(liferayHome + " does not exist, cannot deploy");

			throw new RuntimeException(ioException);
		}

		Path deployPath = liferayHomePath.resolve("deploy");

		if (!Files.exists(deployPath)) {
			IOException ioException = new IOException(deployPath + " does not exist, cannot deploy.");

			throw new RuntimeException(ioException);
		}

		String[] goals = {"clean", "package"};

		ProcessResult processResult = MavenUtil.executeGoals(baseDir.getAbsolutePath(), false, goals);

		int resultCode = processResult.getResultCode();

		BladeCLI bladeCLI = getBladeCLI();

		BaseArgs args = getArgs();

		if (resultCode > 0) {
			String errorMessage = "Maven \"clean\" and \"package\" goals failed.";

			_addError(errorMessage);

			PrintStream err = bladeCLI.error();

			new IOException(errorMessage).printStackTrace(err);

			return;
		}
		else {
			String output = "Maven \"clean\" and \"package\" goals succeeded.";

			bladeCLI.out(output);

			File targetDir = new File(baseDir, "target");

			FileSystem defaultFileSystem = FileSystems.getDefault();

			PathMatcher matcher = defaultFileSystem.getPathMatcher("glob:**/*.{jar,war}");

			for (File outputFile : targetDir.listFiles()) {
				Path outputFilePath = outputFile.toPath();

				if (matcher.matches(outputFilePath)) {
					Path outputFileName = outputFilePath.getFileName();

					Path outputFileDeployPath = deployPath.resolve(outputFileName);

					try {
						Files.copy(outputFilePath, outputFileDeployPath);

						output = "";

						bladeCLI.out(output);
					}
					catch (IOException ioe) {
						bladeCLI.error("Unable to copy file " + outputFilePath);

						if (args.isTrace()) {
							bladeCLI.error(ioe);
						}
						else {
							bladeCLI.error(ioe.getMessage());
						}
					}
				}
			}
		}
	}

}