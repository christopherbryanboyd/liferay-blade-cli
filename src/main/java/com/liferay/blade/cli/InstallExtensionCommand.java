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
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;
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

		String arg = _args.getPath();
		
		if (Objects.nonNull(arg) && arg.trim().length() > 0) {
			Path pathArg = Paths.get(arg);
			
			if (Files.exists(pathArg)) {
				
			} 
			else {
				throw new InstallExtensionCommandException("Path to extension does not exist: " + pathArg);
			}
			
		}
		else {
			
		}

	}
	
	private static void _gradleDeploy(BladeCLI blade, Path pathToProject) throws Exception {
		GradleExec gradle = new GradleExec(blade);

		Set<File> outputFiles = GradleTooling.getOutputFiles(blade.getCacheDir(), blade.getBase());

		gradle.executeGradleCommand("assemble -x check");
		
		Iterator<File> i = outputFiles.iterator();
		if (i.hasNext()) {
			Path outputFile = i.next().toPath();
			
			if (Files.exists(outputFile)) {

				if (_isTemplateMatch(outputFile)) {
					_installTemplate(outputFile);
				}
			}
		}
	}
	
	private static boolean _isTemplateMatch(Path path) {
		return _pathMatcher.matches(path);
	}
	
	private static void _installTemplate(Path pathToTemplate) throws IOException {
		Path extensionsHome = Util.getExtensionsDirectory();
		
		Path pathToTemplateName = pathToTemplate.getFileName();
		
		Path pathToTemplateNew = extensionsHome.resolve(pathToTemplateName);
		
		Files.copy(pathToTemplate, pathToTemplateNew);
	}
	
	private final InstallExtensionCommandArgs _args;
	private final BladeCLI _blade;
	private static final FileSystem _fileSystem = FileSystems.getDefault();
	private static final PathMatcher _pathMatcher = _fileSystem.getPathMatcher("glob:**/*.project.templates.*");
	
}
