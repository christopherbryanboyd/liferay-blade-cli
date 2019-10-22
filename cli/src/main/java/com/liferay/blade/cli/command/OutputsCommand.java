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

package com.liferay.blade.cli.command;

import aQute.bnd.annotation.spi.ServiceProvider;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.gradle.GradleTooling;
import com.liferay.blade.gradle.tooling.ProjectInfo;

import java.io.File;

import java.nio.file.Path;

import java.util.Map;
import java.util.Set;

/**
 * @author Gregory Amerson
 */
@ServiceProvider(BaseCommand.class)
public class OutputsCommand extends BaseCommand<OutputsArgs> {

	public OutputsCommand() {
	}

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		BaseArgs args = bladeCLI.getArgs();

		File base = new File(args.getBase());

		ProjectInfo projectInfo = GradleTooling.loadProjectInfo(base.toPath());

		Map<String, Set<File>> projectOutputFiles = projectInfo.getProjectOutputFiles();

		for (Map.Entry<String, Set<File>> entry : projectOutputFiles.entrySet()) {
			String projectPath = entry.getKey();

			bladeCLI.out(projectPath);

			Set<File> outputFiles = entry.getValue();

			for (File output : outputFiles) {
				Path outputPath = output.toPath();

				bladeCLI.out("\t" + outputPath);
			}

			bladeCLI.out("\n");
		}
	}

	@Override
	public Class<OutputsArgs> getArgsClass() {
		return OutputsArgs.class;
	}

}