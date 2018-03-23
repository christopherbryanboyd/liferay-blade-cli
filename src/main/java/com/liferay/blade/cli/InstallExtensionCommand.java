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

package com.liferay.blade.cli;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.gradle.GradleTooling;

/**
 * @author Christopher Bryan Boyd
 */
public class InstallExtensionCommand {
	
	public static final String DESCRIPTION = "Installs an extension into Blade.";

	public InstallExtensionCommand(BladeCLI blade, InstallExtensionCommandArgs options) throws Exception {
		_blade = blade;
		_args = options;
		
	}

	public void execute() throws Exception {


		GradleExec gradle = new GradleExec(_blade);

		Set<File> outputFiles = GradleTooling.getOutputFiles(_blade.getCacheDir(), _blade.getBase());

		gradle.executeGradleCommand("assemble -x check");
		
		Iterator<File> i = outputFiles.iterator();
		if (i.hasNext()) {
			Path outputFile = i.next().toPath();
			
			if (Files.exists(outputFile)) {
				Path bladeExtensionsPath = Util.getExtensionsDirectory();
				FileSystem fileSystem = FileSystems.getDefault();
				PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:**/*.project.templates.*");
				
			}
			
			
		}
	}

	private final InstallExtensionCommandArgs _args;
	private final BladeCLI _blade;
}
