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

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.Workspace;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.ConvertArgs;

/**
 * @author David Truong
 */
public class ConvertThemeCommand {

	public static final String DESCRIPTION =
		"Migrate a plugins sdk theme to new workspace theme project";

	public ConvertThemeCommand(blade blade, ConvertArgs options)
		throws Exception {

		_blade = blade;
		_options = options;

		Path projectDir = Util.getWorkspaceDir(_blade);

		Properties gradleProperties = Util.getGradleProperties(projectDir);

		String pluginsSDKDirPath = null;

		if (gradleProperties != null) {
			pluginsSDKDirPath = gradleProperties.getProperty(
				Workspace.DEFAULT_PLUGINS_SDK_DIR_PROPERTY);
		}

		if (pluginsSDKDirPath == null) {
			pluginsSDKDirPath = Workspace.DEFAULT_PLUGINS_SDK_DIR;
		}

		_pluginsSDKThemesDir = 
			projectDir.resolve(Paths.get(pluginsSDKDirPath, "themes"));

		String themesDirPath = null;

		if (gradleProperties != null) {
			themesDirPath = gradleProperties.getProperty(
				Workspace.DEFAULT_THEMES_DIR_PROPERTY);
		}

		if (themesDirPath == null) {
			themesDirPath = Workspace.DEFAULT_THEMES_DIR;
		}

		_themesDir = projectDir.resolve(themesDirPath);
	}

	public void execute() throws Exception {

		final String themeName = _options.getName().isEmpty() ? null : _options.getName().iterator().next();
		
		if (!Util.isWorkspace(_blade)) {
			_blade.error("Please execute this in a Liferay Workspace Project");

			return;
		}

		if (themeName == null) {
			List<String> themes = new ArrayList<>();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(_pluginsSDKThemesDir, f -> Files.isDirectory(f))) {
				if (_options.isAll()) {
					for (Path file : (Iterable<Path>)stream::iterator) {
						importTheme(file.toRealPath());
					}
				} else {
					stream.forEach(file -> themes.add(file.getFileName().toString()));
				}
			}
			if (!_options.isAll()) {
				if (themes.size() > 0) {
					String exampleTheme = themes.get(0);

					_blade.out().println(
						"Please provide the theme project name to migrate, " +
							"e.g. \"blade migrateTheme " + exampleTheme +
								"\"\n");
					_blade.out().println("Currently available themes:");
					_blade.out().println(
						WordUtils.wrap(StringUtils.join(themes, ", "), 80));
				}
				else {
					_blade.out().println(
						"Good news! All your themes have already been " +
							"migrated to " + _themesDir);
				}
			}
		}
		else {
			Path themeDir = _pluginsSDKThemesDir.resolve(themeName);

			if (Files.exists(themeDir)) {
				importTheme(themeDir.toRealPath());
			}
			else {
				_blade.error("Theme does not exist");
			}
		}
	}

	public void importTheme(Path themePath) throws Exception {
		Process process = Util.startProcess(
			_blade,
			"yo liferay-theme:import -p \"" + themePath + "\" -c " +
				compassSupport(themePath) + " --skip-install",
			_themesDir, false);

		int errCode = process.waitFor();

		if (errCode == 0) {
			_blade.out().println(
				"Theme " + themePath + " migrated successfully");

			FileUtils.deleteDirectory(themePath.toFile());
		}
		else {
			_blade.error("blade exited with code: " + errCode);
		}
	}

	private boolean compassSupport(Path themePath) throws Exception {

		Path customCss = themePath.resolve(Paths.get("docroot", "_diffs", "css", "custom.css"));

		if (Files.notExists(customCss)) {
			customCss = themePath.resolve(Paths.get("docroot", "_diffs", "css", "_custom.scss"));
		}

		if (Files.notExists(customCss)) {
			return false;
		}

		String css = new String(Files.readAllBytes(customCss));

		Matcher matcher = _compassImport.matcher(css);

		return matcher.find();
	}

	private blade _blade;
	private final Pattern
		_compassImport = Pattern.compile("@import\\s*['\"]compass['\"];");
	private ConvertArgs _options;
	private Path _pluginsSDKThemesDir;
	private Path _themesDir;

}