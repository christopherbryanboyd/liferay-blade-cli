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
import com.liferay.blade.cli.commands.arguments.ConvertArgs;
import com.liferay.blade.cli.util.Constants;
import com.liferay.project.templates.ProjectTemplatesArgs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Terry Jia
 */
public class ConvertServiceBuilderCommand {

	public static final String DESCRIPTION = "Convert a service builder project to new Liferay Workspace projects";

	public ConvertServiceBuilderCommand(blade blade, ConvertArgs options) throws Exception {
		_blade = blade;
		_options = options;

		Path projectDir = Util.getWorkspaceDir(_blade);

		Properties gradleProperties = Util.getGradleProperties(projectDir);

		String warsDirPath = null;

		if (gradleProperties != null) {
			warsDirPath = gradleProperties.getProperty(Workspace.DEFAULT_WARS_DIR_PROPERTY);
		}

		if (warsDirPath == null) {
			warsDirPath = Workspace.DEFAULT_WARS_DIR;
		}

		_warsDir = projectDir.resolve(warsDirPath);

		String moduleDirPath = null;

		if (gradleProperties != null) {
			moduleDirPath = gradleProperties.getProperty(Workspace.DEFAULT_MODULES_DIR_PROPERTY);
		}

		if (moduleDirPath == null) {
			moduleDirPath = Workspace.DEFAULT_MODULES_DIR;
		}

		_moduleDir = projectDir.resolve(moduleDirPath);
	}

	public void execute() throws Exception {

		final String projectName = _options.getName().isEmpty() ? null : _options.getName().iterator().next();

		if (!Util.isWorkspace(_blade)) {
			_blade.error("Please execute command in a Liferay Workspace project");

			return;
		}

		if (projectName == null) {
			_blade.error("Please specify a plugin name");

			return;
		}

		Path project = _warsDir.resolve(projectName);

		if (Files.notExists(project)) {
			_blade.error("The project " + projectName + " doesn't exist in " + _warsDir.toAbsolutePath());

			return;
		}

		Path serviceFile = project.resolve(Paths.get("src", "main", "webapp", "WEB-INF", "service.xml"));

		if (Files.notExists(serviceFile)) {
			_blade.error("There is no service.xml file in " + projectName);

			return;
		}
		List<String> args = _options.getName();
		String sbProjectName = !args.isEmpty() && args.size() >= 2 ? args.get(1) : null;

		if (sbProjectName == null) {
			if (projectName.endsWith("-portlet")) {
				sbProjectName = projectName.replaceAll("-portlet$", "");
			}
			else {
				sbProjectName = projectName;
			}
		}

		Path sbProject = _moduleDir.resolve(sbProjectName);

		if (Files.exists(sbProject)) {
			_blade.error(
				"The service builder module project " + sbProjectName + " exist now, please choose another name");

			return;
		}

		ServiceBuilder oldServiceBuilderXml = new ServiceBuilder(serviceFile);

		CreateCommand createCommand = new CreateCommand(_blade);

		ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

		projectTemplatesArgs.setDestinationDir(_moduleDir.toFile());
		projectTemplatesArgs.setName(sbProject.getFileName().toString());
		projectTemplatesArgs.setPackageName(oldServiceBuilderXml.getPackagePath());
		projectTemplatesArgs.setTemplate("service-builder");

		createCommand.execute(projectTemplatesArgs);

		Path sbServiceProject = sbProject.resolve(sbProjectName + "-service");

		Path newServiceXml = sbServiceProject.resolve(ServiceBuilder.SERVICE_XML);

		Files.move(serviceFile, newServiceXml, StandardCopyOption.REPLACE_EXISTING);

		ServiceBuilder serviceBuilderXml = new ServiceBuilder(newServiceXml);

		String sbPackageName = serviceBuilderXml.getPackagePath();

		String packageName = sbPackageName.replaceAll("\\.", "/");

		Path oldSBFolder = project.resolve(Paths.get(Constants.DEFAULT_JAVA_SRC, packageName));

		Path newSBFolder = sbServiceProject.resolve(Paths.get(Constants.DEFAULT_JAVA_SRC, packageName));

		Path oldServiceImplFolder = oldSBFolder.resolve("service");
		Path newServiceImplFolder = newSBFolder.resolve("service");

		if (Files.exists(oldServiceImplFolder)) {
			Files.createDirectories(newServiceImplFolder);

			Files.move(oldServiceImplFolder, newServiceImplFolder, StandardCopyOption.REPLACE_EXISTING);
		}

		Path oldModelImplFolder = oldSBFolder.resolve("model");
		Path newModelImplFolder = newSBFolder.resolve("model");

		if (Files.exists(oldModelImplFolder)) {
			Files.createDirectories(newModelImplFolder);

			Files.move(oldModelImplFolder, newModelImplFolder, StandardCopyOption.REPLACE_EXISTING);
		}

		Path oldMetaInfFolder = project.resolve(Paths.get(Constants.DEFAULT_JAVA_SRC, ServiceBuilder.META_INF));
		Path newMetaInfFolder = sbServiceProject.resolve(Paths.get(Constants.DEFAULT_RESOURCES_SRC, ServiceBuilder.META_INF));

		if (Files.exists(oldMetaInfFolder)) {
			Files.createDirectories(newMetaInfFolder);

			Files.move(oldMetaInfFolder.resolve(ServiceBuilder.PORTLET_MODEL_HINTS_XML),
				newMetaInfFolder.resolve(ServiceBuilder.PORTLET_MODEL_HINTS_XML));
		}

		Path oldSrcFolder = project.resolve(Constants.DEFAULT_JAVA_SRC);
		Path newResourcesSrcFolder = sbServiceProject.resolve(Constants.DEFAULT_RESOURCES_SRC);

		if (Files.exists(oldSrcFolder)) {
			Files.createDirectories(newResourcesSrcFolder);

			Files.move(oldSrcFolder.resolve(ServiceBuilder.SERVICE_PROPERTIES),
				newResourcesSrcFolder.resolve(ServiceBuilder.SERVICE_PROPERTIES));
		}

		Path sbApiProject = sbProject.resolve(sbProjectName + "-api");
		Path oldApiFolder = project.resolve(Paths.get(Constants.DEFAULT_WEBAPP_SRC, ServiceBuilder.API_62));

		if (Files.exists(oldApiFolder)) {
			Path newApiFolder = sbApiProject.resolve(Constants.DEFAULT_JAVA_SRC);

			Files.createDirectories(newApiFolder);

			for (Path oldApiFile : Files.find(oldApiFolder, 999, (p, a) -> true).collect(Collectors.toList())) {
				Files.move(oldApiFile, newApiFolder.resolve(oldApiFile.getFileName()));
			}
		}

		Files.deleteIfExists(oldApiFolder);

		// go through all api folders and make sure to add a packageinfo file

		Stream<Path> srcPaths = Files.walk(sbApiProject.resolve(Constants.DEFAULT_JAVA_SRC));

		srcPaths.map(
			path -> path.toFile()
		).filter(
			file -> file.isFile() && file.getName().endsWith(".java") && isInExportedApiFolder(file)
		).map(
			file -> file.toPath().resolveSibling("packageinfo").toFile()
		).filter(
			file -> !file.exists()
		).forEach(
			file -> {
				try {
					Files.write(file.toPath(), new String("version 1.0.0").getBytes());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		);

		srcPaths.close();

		// add dependency on -api to portlet project
		Path gradleFile = project.resolve( "build.gradle");

		String gradleContent = new String(Files.readAllBytes(gradleFile));

		StringBuilder sb = new StringBuilder();

		sb.append("dependencies {\n");
		sb.append("\tcompileOnly project(\":modules:");
		sb.append(sbProject.getFileName());
		sb.append(":");
		sb.append(sbApiProject.getFileName());
		sb.append("\")\n");

		String updatedContent = gradleContent.replaceAll("dependencies \\{", sb.toString());

		Files.write(gradleFile, updatedContent.getBytes());

		System.out.println("Migrating files done, then you should fix breaking changes and re-run build-service task.");
	}

	private boolean isInExportedApiFolder(File file) {
		File dir = file.getParentFile();

		String dirName = dir.getName();

		return dirName.equals("exception") || dirName.equals("model") || dirName.equals("service") || dirName.equals("persistence");
	}

	private static class ServiceBuilder {
		public static final String META_INF = "META-INF/";
		public static final String API_62 = "WEB-INF/service/";
		public static final String PORTLET_MODEL_HINTS_XML = "portlet-model-hints.xml";
		public static final String SERVICE_XML = "service.xml";
		public static final String SERVICE_PROPERTIES = "service.properties";

		Path _serviceXml;
		Element _rootElement;

		public ServiceBuilder(Path serviceXml) throws Exception {
			_serviceXml = serviceXml;
			parse();
		}

		private void parse() throws Exception {
			if ((_rootElement == null) && (_serviceXml != null) && (Files.exists(_serviceXml))) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(_serviceXml.toFile());

				_rootElement = doc.getDocumentElement();
			}
		}

		public String getPackagePath() {
			return _rootElement.getAttribute("package-path");
		}
	}

	private blade _blade;
	private final Path _warsDir;
	private final Path _moduleDir;
	private ConvertArgs _options;

}