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

import com.liferay.project.templates.ProjectTemplates;
import com.liferay.project.templates.ProjectTemplatesArgs;
import com.liferay.project.templates.internal.util.FileUtil;

import java.io.File;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Gregory Amerson
 * @author David Truong
 * @author Christopher Boyd
 */
public class CreateCommand extends BaseCommand<CreateCommandArgs> {

	public CreateCommand(BladeCLI blade, CreateCommandArgs args) {
		super(blade, args);
	}

	public void execute() throws Exception {
		if (_args.isListTemplates()) {
			_printTemplates();

			return;
		}

		String template = _args.getTemplate();

		if (template == null) {
			_blade.err("The following option is required: [-t | --template]\n\n");
			_blade.err("Availble project templates:\n\n");

			_printTemplates();

			return;
		}

		if (Objects.equals(_args.getTemplate(), "fragment")) {
			boolean hasHostBundleBSN = false;

			if (_args.getHostBundleBSN() != null) {
				hasHostBundleBSN = true;
			}

			boolean hasHostBundleVersion = false;

			if (_args.getHostBundleVersion() != null) {
				hasHostBundleVersion = true;
			}

			if (!hasHostBundleBSN || !hasHostBundleVersion) {
				StringBuilder sb = new StringBuilder("\"-t fragment\" options missing:" + System.lineSeparator());

				if (!hasHostBundleBSN) {
					sb.append("Host Bundle BSN (\"-h\", \"--host-bundle-bsn\") is required.");
				}

				if (!hasHostBundleVersion) {
					sb.append("Host Bundle Version (\"-H\", \"--host-bundle-version\") is required.");
				}

				_blade.printUsage("create", sb.toString());

				return;
			}
		}

		String name = _args.getName();

		if (Util.isEmpty(name)) {
			_addError("Create", "SYNOPSIS\n\t create [options] <[name]>");

			return;
		}

		if (!_isExistingTemplate(template)) {
			_addError("Create", "The template " + template + " is not in the list");

			return;
		}

		File dir;

		File argsDir = _args.getDir();

		if (argsDir != null) {
			dir = new File(argsDir.getAbsolutePath());
		}
		else if (template.startsWith("war") || template.equals("theme") || template.equals("layout-template") ||
				 template.equals("spring-mvc-portlet")) {

			dir = _getDefaultWarsDir();
		}
		else {
			dir = _getDefaultModulesDir();
		}

		final File checkDir = new File(dir, name);

		if (!_checkDir(checkDir)) {
			_addError("Create", name + " is not empty or it is a file. Please clean or delete it then run again");

			return;
		}

		ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

		projectTemplatesArgs.setClassName(_args.getClassname());
		projectTemplatesArgs.setContributorType(_args.getContributorType());
		projectTemplatesArgs.setDestinationDir(dir.getAbsoluteFile());
		projectTemplatesArgs.setHostBundleSymbolicName(_args.getHostBundleBSN());
		projectTemplatesArgs.setHostBundleVersion(_args.getHostBundleVersion());
		projectTemplatesArgs.setLiferayVersion(_args.getLiferayVersion());
		projectTemplatesArgs.setName(name);
		projectTemplatesArgs.setPackageName(_args.getPackageName());
		projectTemplatesArgs.setService(_args.getService());
		projectTemplatesArgs.setTemplate(template);
		projectTemplatesArgs.getArchetypesDirs().add(FileUtil.getJarFile(ProjectTemplates.class));
		projectTemplatesArgs.getArchetypesDirs().add(Util.getTemplatesDirectory().toFile());
		
		boolean mavenBuild = "maven".equals(_args.getBuild());

		projectTemplatesArgs.setGradle(!mavenBuild);
		projectTemplatesArgs.setMaven(mavenBuild);

		execute(projectTemplatesArgs);

		_blade.out("Successfully created project " + projectTemplatesArgs.getName() + " in " + dir.getAbsolutePath());
	}

	@Override
	public Class<CreateCommandArgs> getArgsClass() {

		return CreateCommandArgs.class;
	}

	protected CreateCommand(BladeCLI blade) {
		super(blade, null);

	}

	protected void execute(ProjectTemplatesArgs projectTemplatesArgs) throws Exception {
		File dir = projectTemplatesArgs.getDestinationDir();
		String name = projectTemplatesArgs.getName();

		new ProjectTemplates(projectTemplatesArgs);

		File gradlew = new File(dir, name + "/gradlew");

		if (gradlew.exists()) {
			gradlew.setExecutable(true);
		}
	}

	private static boolean _checkDir(File file) {
		if (file.exists()) {
			if (!file.isDirectory()) {
				return false;
			}
			else {
				File[] children = file.listFiles();

				if (Util.isNotEmpty(children)) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean _containsDir(File currentDir, File parentDir) throws Exception {
		String currentPath = currentDir.getCanonicalPath();

		String parentPath = parentDir.getCanonicalPath();

		return currentPath.startsWith(parentPath);
	}
	
	private void _addError(String prefix, String msg) {
		_blade.addErrors(prefix, Collections.singleton(msg));
	}

	private File _getDefaultModulesDir() throws Exception {
		File base = _blade.getBase();

		File baseDir = base.getAbsoluteFile();

		if (!Util.isWorkspace(baseDir)) {
			return baseDir;
		}

		Properties properties = Util.getGradleProperties(baseDir);

		String modulesDirValue = (String)properties.get(Workspace.DEFAULT_MODULES_DIR_PROPERTY);

		if (modulesDirValue == null) {
			modulesDirValue = Workspace.DEFAULT_MODULES_DIR;
		}

		File projectDir = Util.getWorkspaceDir(_blade);

		File modulesDir = new File(projectDir, modulesDirValue);

		if (_containsDir(baseDir, modulesDir)) {
			return baseDir;
		}

		return modulesDir;
	}

	private File _getDefaultWarsDir() throws Exception {
		File base = _blade.getBase();

		File baseDir = base.getAbsoluteFile();

		if (!Util.isWorkspace(baseDir)) {
			return baseDir;
		}

		Properties properties = Util.getGradleProperties(baseDir);

		String warsDirValue = (String)properties.get(Workspace.DEFAULT_WARS_DIR_PROPERTY);

		if (warsDirValue == null) {
			warsDirValue = Workspace.DEFAULT_WARS_DIR;
		}

		if (warsDirValue.contains(",")) {
			warsDirValue = warsDirValue.split(",")[0];
		}

		File projectDir = Util.getWorkspaceDir(_blade);

		File warsDir = new File(projectDir, warsDirValue);

		if (_containsDir(baseDir, warsDir)) {
			return baseDir;
		}

		return warsDir;
	}

	private boolean _isExistingTemplate(String templateName) throws Exception {
		return Util.getTemplateNames().contains(templateName);
	}

	private void _printTemplates() throws Exception {

		Map<String, String> templates = Util.getTemplates();
		
		List<String> templateNames = new ArrayList<>(Util.getTemplateNames());

		Collections.sort(templateNames);

		Comparator<String> compareLength = Comparator.comparingInt(String::length);

		String longestString = templateNames.stream().max(
			compareLength
		).get();

		int padLength = longestString.length() + 2;

		PrintStream out = _blade.out();

		for (String name : templateNames) {

			out.print(StringUtils.rightPad(name, padLength));

			_blade.out(templates.get(name));
		}
	}

}