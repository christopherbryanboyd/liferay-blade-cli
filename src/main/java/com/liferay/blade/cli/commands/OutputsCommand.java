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

package com.liferay.blade.cli.commands;

import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.OutputsArgs;
import com.liferay.blade.cli.gradle.GradleTooling;

import java.nio.file.Path;
import java.util.Set;

/**
 * @author Gregory Amerson
 */
public class OutputsCommand {

	public OutputsCommand(blade blade, OutputsArgs options)
		throws Exception {

		_blade = blade;
	}

	public void execute() throws Exception {
		final Path basePath = _blade.getBase();
		final Path basePathRoot = basePath.getRoot();

		final Set<Path> outputs = GradleTooling.getOutputFiles(
			_blade.getCacheDir(), basePath);

		outputs.forEach(outputPath -> 
		{
			Path outputPathRoot = outputPath.getRoot();

			Object print = null;

			if (basePathRoot != null && outputPathRoot != null) {
				print = basePath.relativize(outputPath);
			}
			else {
				_blade.out().println(outputPath);
			}

			_blade.out().println(print);
		});
	}

	private blade _blade;

}