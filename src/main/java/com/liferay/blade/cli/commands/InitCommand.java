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

import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.util.FilesUtil;
import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.InitArgs;
import com.liferay.project.templates.ProjectTemplates;
import com.liferay.project.templates.ProjectTemplatesArgs;

import aQute.lib.io.IO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Gregory Amerson
 * @author Terry Jia
 */
public class InitCommand {

	private final static String[] _SDK_6_GA5_FILES = {
		"app-servers.gradle", "build.gradle", "build-plugins.gradle",
		"build-themes.gradle", "sdk.gradle", "settings.gradle",
		"util.gradle", "versions.gradle" };

	public static final String DESCRIPTION =
		"Initializes a new Liferay workspace";

	public InitCommand(blade blade, InitArgs options) throws Exception {
		_blade = blade;
		_options = options;
	}

	public void execute() throws Exception {

		String name = _options.getName();

		File destDir = name != null ? Paths.get(
			_blade.getBase().toString(), name).toFile() : Paths.get(_blade.getBase().toString()).toFile();

		Path temp = null;

		boolean isPluginsSDK = isPluginsSDK(destDir.toPath());

		trace("Using destDir " + destDir);

		if (destDir.exists() && !destDir.isDirectory()) {
			addError(destDir.getAbsolutePath() + " is not a directory.");
			return;
		}

		if (destDir.exists()) {
			if (isPluginsSDK) {
				if (!isPluginsSDK70(destDir.toPath())) {
					if (_options.isUpgrade()) {
						trace(
							"Found plugins-sdk 6.2, upgraded to 7.0, moving contents to new subdirectory " +
								"and initing workspace.");

						for (String fileName : _SDK_6_GA5_FILES) {
							File file = new File(destDir, fileName);

							if (file.exists()) {
								file.delete();
							}
						}
					}
					else {
						addError("Unable to run blade init in plugins sdk 6.2, please add -u (--upgrade)"
							+ " if you want to upgrade to 7.0");
						return;
					}
				}

				trace("Found plugins-sdk, moving contents to new subdirectory " +
					"and initing workspace.");

				temp = Files.createTempDirectory("orignal-sdk");

				_moveContentsToDirectory(destDir.toPath(), temp);
			}
			else if (destDir.list().length > 0) {
				if (_options.isForce()) {
					trace("Files found, initing anyways.");
				}
				else {
					addError(
						destDir.getAbsolutePath() +
						" contains files, please move them before continuing " +
							"or use -f (--force) option to init workspace " +
								"anyways.");
					return;
				}
			}
		}

		ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

		if (name == null) {
			name = destDir.getName();
		}

		File destParentDir = destDir.getParentFile();

		projectTemplatesArgs.setDestinationDir(destParentDir);

		if (_options.isForce() || _options.isUpgrade()) {
			projectTemplatesArgs.setForce(true);
		}

		projectTemplatesArgs.setName(name);
		projectTemplatesArgs.setTemplate("workspace");

		new ProjectTemplates(projectTemplatesArgs);

		if (isPluginsSDK) {
			if (_options.isUpgrade()) {
				GradleExec gradleExec = new GradleExec(_blade);

				gradleExec.executeGradleCommand("upgradePluginsSDK");
			}

			File gitFile = new File(temp.toFile(), ".git");

			if (gitFile.exists()) {
				File destGitFile = new File(destDir, ".git");

				_moveContentsToDirectory(gitFile.toPath(), destGitFile.toPath());

				IO.deleteWithException(gitFile);
			}

			File pluginsSdkDir = new File(destDir, "plugins-sdk");

			_moveContentsToDirectory(temp, pluginsSdkDir.toPath());

			IO.deleteWithException(temp);
		}
	}
	
	private void addError(String msg) {
		_blade.addErrors("init", Collections.singleton(msg));
	}

	private boolean isPluginsSDK(Path dir) throws IOException {
		if ((dir == null) || Files.notExists(dir) || !Files.isDirectory(dir)) {
			return false;
		}

		List<String> names = Arrays.asList("portlets", 
				"hooks",
				"layouttpl",
				"themes",
				"build.properties",
				"build.xml",
				"build-common.xml",
				"build-common-plugin.xml");
		return Files.list(dir).map(x -> x.getFileName().toString()).anyMatch(names::contains);

	}

	private boolean isPluginsSDK70( Path dir) {
		if ((dir == null) || !Files.exists(dir) || !Files.isDirectory(dir)) {
			return false;
		}

		Path buildProperties = dir.resolve("build.properties");
		Properties properties = new Properties();

		
		try (InputStream in = Files.newInputStream(buildProperties)) {
		
			properties.load(in);

			String sdkVersionValue = (String) properties.get("lp.version");

			if (Objects.equals(sdkVersionValue, "7.0.0")) {
				return true;
			}
		} catch (IOException e) {
		}
		return false;
	}

	private void _moveContentsToDirectory(Path src, Path dest) throws IOException {
		Path source = src.toAbsolutePath();
		Path target = dest.toAbsolutePath();

		Files.walkFileTree(source,
			new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

					if (!dir.endsWith(src.getFileName())) {
						Files.delete(dir);
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Path targetDir = target.resolve(source.relativize(dir));

					if (!Files.exists(targetDir)) {
						Files.createDirectory(targetDir);
					}

					if (Util.isWindows() && !dir.toFile().canWrite()) {
						Files.setAttribute(dir, "dos:readonly", false);
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Path targetFile = target.resolve(source.relativize(file));

					if (!Files.exists(targetFile)) {
						Files.copy(file, targetFile);
					}

					if (Util.isWindows() && !file.toFile().canWrite()) {
						Files.setAttribute(file, "dos:readonly", false);
					}

					Files.delete(file);

					return FileVisitResult.CONTINUE;
				}
			});		
	} 

	private void trace(String msg) {
		_blade.trace("%s: %s", "init", msg);
	}

	private final blade _blade;
	private final InitArgs _options;

}
