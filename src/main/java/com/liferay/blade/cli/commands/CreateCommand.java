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

import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.Workspace;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.CreateArgs;
import com.liferay.project.templates.ProjectTemplates;
import com.liferay.project.templates.ProjectTemplatesArgs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Gregory Amerson
 * @author David Truong
 */
public class CreateCommand {

	public static final String DESCRIPTION =
		"Creates a new Liferay module project from several available " +
			"templates.";


	public CreateCommand(blade blade, CreateArgs options) {
		_blade = blade;
		_options = options;
	}

	CreateCommand(blade blade) {
		_blade = blade;
		_options = null;
	}

	public void execute() throws Exception {
		if (_options.isListtemplates()) {
			printTemplates();
			return;
		}


		String name = _options.getName();

		if (name == null||name.isEmpty()) {
			addError("Create", "SYNOPSIS\n\t create [options] <[name]>");
			return;
		}

		String template = _options.getTemplate();

		if (template == null) {
			template = "mvc-portlet";
		}
		else if (!isExistingTemplate(template)) {
				addError(
					"Create", "the template "+template+" is not in the list");
				return;
		}

		Path dir;

		if(_options.getDir() != null) {
			dir = _options.getDir().toPath().toAbsolutePath();
		}
		else if (template.equals("theme") || template.equals("layout-template")
				|| template.equals("spring-mvc-portlet")) {
			dir = getDefaultWarsDir();
		}
		else {
			dir = getDefaultModulesDir();
		}

		final Path checkDir = dir.resolve(name);

		if(!checkDir(checkDir)) {
			addError(
				"Create", name + " is not empty or it is a file." +
				" Please clean or delete it then run again");
			return;
		}

		ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

		projectTemplatesArgs.setClassName(_options.getClassname());
		projectTemplatesArgs.setContributorType(_options.getContributorType());
		projectTemplatesArgs.setDestinationDir(dir.toFile());
		projectTemplatesArgs.setHostBundleSymbolicName(_options.getHostbundlebsn());
		projectTemplatesArgs.setHostBundleVersion(_options.getHostbundleversion());
		projectTemplatesArgs.setName(name);
		projectTemplatesArgs.setPackageName(_options.getPackagename());
		projectTemplatesArgs.setService(_options.getService());
		projectTemplatesArgs.setTemplate(template);

		boolean mavenBuild = "maven".equals(_options.getBuild());

		projectTemplatesArgs.setGradle(!mavenBuild);
		projectTemplatesArgs.setMaven(mavenBuild);

		execute(projectTemplatesArgs);

		_blade.out().println(
			"Successfully created project " + projectTemplatesArgs.getName() + 
				" in " + dir.toAbsolutePath());
	}

	void execute(ProjectTemplatesArgs projectTemplatesArgs) throws Exception {
		Path dir = projectTemplatesArgs.getDestinationDir().toPath();
		String name = projectTemplatesArgs.getName();

		new ProjectTemplates(projectTemplatesArgs);

		Path gradlew = dir.resolve(Paths.get(name, "gradlew"));

		if(Files.exists(gradlew)) {
			gradlew.toFile().setExecutable(true);
		}
	}

	private void addError(String prefix, String msg) {
		_blade.addErrors(prefix, Collections.singleton(msg));
	}

	private boolean containsDir(Path currentDir, Path parentDir)
		throws Exception {
		return currentDir.toRealPath().startsWith(parentDir.toRealPath());
	}

	private boolean checkDir(Path file) {
		if(Files.exists(file)) {
			if(!Files.isDirectory(file)) {
				return false;
			}
			else {
				try {
					return !Files.list(file).findAny().isPresent();
				} catch (IOException e) {
					_blade.error(e.getMessage());
					return false;
				}
			}
		}

		return true;
	}

	private Path getDefaultModulesDir() throws Exception {
		Path baseDir = _blade.getBase();

		if (!Util.isWorkspace(baseDir)) {
			return baseDir;
		}

		Properties properties = Util.getGradleProperties(baseDir);

		String modulesDirValue = (String)properties.get(
			Workspace.DEFAULT_MODULES_DIR_PROPERTY);

		if (modulesDirValue == null) {
			modulesDirValue = Workspace.DEFAULT_MODULES_DIR;
		}

		Path projectDir = Util.getWorkspaceDir(_blade);

		Path modulesDir = projectDir.resolve( modulesDirValue);

		return containsDir(baseDir, modulesDir) ? baseDir : modulesDir;
	}

	private Path getDefaultWarsDir() throws Exception {
		Path baseDir = _blade.getBase();

		if (!Util.isWorkspace(baseDir)) {
			return baseDir;
		}

		Properties properties = Util.getGradleProperties(baseDir);

		String warsDirValue = (String)properties.get(
			Workspace.DEFAULT_WARS_DIR_PROPERTY);

		if (warsDirValue == null) {
			warsDirValue = Workspace.DEFAULT_WARS_DIR;
		}

		if(warsDirValue.contains(",")) {
			warsDirValue = warsDirValue.split(",")[0];
		}

		Path projectDir = Util.getWorkspaceDir(_blade);

		Path warsDir = projectDir.resolve(warsDirValue);

		return containsDir(baseDir, warsDir) ? baseDir : warsDir;
	}

	public static String[] getTemplateNames() throws Exception {
		Map<String, String> templates = ProjectTemplates.getTemplates();

		return templates.keySet().toArray(new String[0]);
	}

	public static boolean isExistingTemplate(String templateName) throws Exception {
		String[] templates = getTemplateNames();

		for (String template : templates) {
			if (templateName.equals(template)) {
				return true;
			}
		}

		return false;
	}

	private void printTemplates() throws Exception {
		Map<String,String> templates = ProjectTemplates.getTemplates();

		List<String> templateNames = new ArrayList<>(templates.keySet());

		Collections.sort(templateNames);

		Comparator<String> compareLength =
			Comparator.comparingInt(String::length);

		String longestString = templateNames.stream().max(compareLength).get();

		int padLength = longestString.length() + 2;

		for (String name : templateNames) {
			_blade.out().print(StringUtils.rightPad(name, padLength));

			_blade.out().println(templates.get(name));
		}
	}

	private final blade _blade;
	private final CreateArgs _options;

}