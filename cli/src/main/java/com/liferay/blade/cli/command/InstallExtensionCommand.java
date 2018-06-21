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

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.Extensions;
import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.gradle.GradleTooling;
import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.blade.cli.util.FileUtil;
import com.liferay.blade.cli.util.StringUtil;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class InstallExtensionCommand extends BaseCommand<InstallExtensionArgs> {

	public InstallExtensionCommand() {
	}

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();
		InstallExtensionArgs args = getArgs();

		String pathArg = args.getPath();

		if (StringUtil.isNullOrEmpty(pathArg)) {
			pathArg = ".";
		}

		String pathArgLower = pathArg.toLowerCase();

		if (pathArgLower.startsWith("http") && _isValidURL(pathArg)) {
			if (pathArgLower.contains("//github.com/")) {
				Path path = Files.createTempDirectory(null);

				try {
					Path zip = path.resolve("master.zip");

					File dir = path.toFile();

					bladeCLI.out("Downloading github repository " + pathArg);

					BladeUtil.downloadGithubProject(pathArg, zip);

					bladeCLI.out("Unzipping github repository to " + path);

					BladeUtil.unzip(zip.toFile(), dir, null);

					File[] directories = dir.listFiles(File::isDirectory);

					if ((directories != null) && (directories.length > 0)) {
						Path directory = directories[0].toPath();

						if (_isGradleBuild(directory)) {
							bladeCLI.out("Building extension...");

							Set<Path> extensionPaths = _gradleAssemble(directory);

							if (!extensionPaths.isEmpty()) {
								for (Path extensionPath : extensionPaths) {
									_installExtension(extensionPath);
								}
							}
							else {
								bladeCLI.err("Unable to get output of gradle build " + directory);
							}
						}
						else {
							bladeCLI.err("Path not a gradle build " + directory);
						}
					}
				}
				catch (Exception e) {
					throw e;
				}
				finally {
					FileUtil.deleteDir(path);
				}
			}
			else {
				throw new Exception("Only github http(s) links are supported");
			}
		}
		else {
			Path path = Paths.get(pathArg);

			if (Files.exists(path)) {
				Path gradleBuildPath = Optional.of(
					path
				).filter(
					Files::exists
				).filter(
					Files::isDirectory
				).filter(
					InstallExtensionCommand::_isGradleBuild
				).orElse(
					null
				);

				if (gradleBuildPath != null) {
					Set<Path> paths = _gradleAssemble(path);

					if (!paths.isEmpty()) {
						Iterator<Path> pathsIterator = paths.iterator();

						path = pathsIterator.next();
					}
				}
			}

			if (path == null) {
				throw new Exception("Path to extension does not exist: " + pathArg);
			}
			else {
				_installExtension(path);
			}
		}
	}

	@Override
	public Class<InstallExtensionArgs> getArgsClass() {
		return InstallExtensionArgs.class;
	}

	private static boolean _isArchetype(Path path) {
		return BladeUtil.searchZip(path, name -> name.endsWith("archetype-metadata.xml"));
	}

	private static boolean _isCustomTemplate(Path path) {
		if (_isTemplateMatch(path) && _isArchetype(path)) {
			return true;
		}

		return false;
	}

	private static boolean _isExtension(Path path) {
		if (_isCustomTemplate(path)) {
			return true;
		}

		return BladeUtil.searchZip(
			path, name -> name.startsWith("META-INF/services/com.liferay.blade.cli.command.BaseCommand"));
	}

	private static boolean _isGradleBuild(Path path) {
		if ((path != null) && Files.exists(path.resolve("build.gradle"))) {
			return true;
		}

		return false;
	}

	private static boolean _isTemplateMatch(Path path) {
		if (Files.exists(path) && (_customTemplatePathMatcher.matches(path) || _isArchetype(path))) {
			return true;
		}

		return false;
	}

	private static boolean _isValidURL(String urlString) {
		try {
			new URL(urlString).toURI();

			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private Set<Path> _gradleAssemble(Path projectPath) throws Exception {
		BladeCLI bladeCLI = getBladeCLI();

		GradleExec gradleExec = new GradleExec(bladeCLI);

		Set<File> outputFiles = GradleTooling.getOutputFiles(bladeCLI.getCacheDir(), projectPath.toFile());

		gradleExec.executeCommand("assemble -x check", projectPath.toFile());

		Set<Path> extensionPaths = new HashSet<>();

		Iterator<File> i = outputFiles.iterator();

		if (i.hasNext()) {
			File next = i.next();

			Path outputPath = next.toPath();

			if (Files.exists(outputPath)) {
				extensionPaths.add(outputPath);
			}
		}

		return extensionPaths;
	}

	private void _installExtension(Path extensionPath) throws IOException {
		if (_isExtension(extensionPath)) {
			Path extensionsHome = Extensions.getDirectory();

			Path extensionName = extensionPath.getFileName();

			Path newExtensionPath = extensionsHome.resolve(extensionName);

			Files.copy(extensionPath, newExtensionPath);

			getBladeCLI().out("The extension " + extensionName + " has been installed successfully.");
		}
		else {
			throw new IOException(
				"Unable to install. " + extensionPath.getFileName() +
					" is not a valid blade extension, e.g. custom template or command");
		}
	}

	private static final PathMatcher _customTemplatePathMatcher = FileSystems.getDefault().getPathMatcher(
		"glob:**/*.project.templates.*");

}