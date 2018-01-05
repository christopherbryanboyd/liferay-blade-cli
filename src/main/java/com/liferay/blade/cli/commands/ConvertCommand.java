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

import com.liferay.blade.cli.CopyDirVisitor;
import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.Workspace;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.ConvertArgs;
import com.liferay.project.templates.ProjectTemplatesArgs;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aQute.lib.io.IO;

/**
 * @author Gregory Amerson
 */
public class ConvertCommand {

	public static final String DESCRIPTION =
		"Converts a plugins-sdk plugin project to a gradle WAR project in Liferay workspace";

	public ConvertCommand(blade blade, ConvertArgs options)
		throws Exception {
		
		_blade = blade;
		_options = options;

		Path projectDir = Util.getWorkspaceDir(_blade);

		Properties gradleProperties = Util.getGradleProperties(projectDir);

		String pluginsSdkDirPath = null;

		if (gradleProperties != null) {
			pluginsSdkDirPath = gradleProperties.getProperty(
				Workspace.DEFAULT_PLUGINS_SDK_DIR_PROPERTY);
		}

		if (pluginsSdkDirPath == null) {
			pluginsSdkDirPath = Workspace.DEFAULT_PLUGINS_SDK_DIR;
		}

		_pluginsSdkDir = projectDir.resolve(pluginsSdkDirPath);
		_hooksDir = _pluginsSdkDir.resolve("hooks");
		_layouttplDir = _pluginsSdkDir.resolve("layouttpl");
		_portletsDir = _pluginsSdkDir.resolve( "portlets");
		_websDir = _pluginsSdkDir.resolve("webs");
		_themesDir = _pluginsSdkDir.resolve("themes");

		String warsDirPath = null;

		if (gradleProperties != null) {
			warsDirPath = gradleProperties.getProperty(
				Workspace.DEFAULT_WARS_DIR_PROPERTY);
		}

		if (warsDirPath == null) {
			warsDirPath = Workspace.DEFAULT_WARS_DIR;
		}

		_warsDir = projectDir.resolve(warsDirPath);

		if (Files.notExists(_pluginsSdkDir)) {
			_blade.error("Plugins SDK folder " + pluginsSdkDirPath + " doesn't exist.\n" +
					"Please edit gradle.properties and set " + Workspace.DEFAULT_PLUGINS_SDK_DIR_PROPERTY);

			return;
		}
	}

	public void execute() throws Exception {

		final String pluginName = _options.getName().isEmpty() ? null : _options.getName().iterator().next();

		if (!Util.isWorkspace(_blade)) {
			_blade.error("Please execute this in a Liferay Workspace project");

			return;
		}

		if (pluginName == null && (!_options.isAll() && !_options.isList())) {
			_blade.error("Please specify a plugin name, list the projects with [-l] or specify all using option [-a]");

			return;
		}

		final Predicate<Path> containsDocrootFilter = ((path) -> Files.isDirectory(path) && Files.exists(path.resolve("docroot")));
			
		
		final Predicate<Path> serviceBuilderPluginsFilter = ((path) -> containsDocrootFilter.test(path) && hasServiceXmlFile(path));

		List<Path> serviceBuilderPlugins = Files.exists(_portletsDir) ?
				Files.find(_portletsDir, 999, (p, bfa) -> serviceBuilderPluginsFilter.test(p)).collect(Collectors.toList()): Collections.emptyList();
		List<Path> portlets = Files.exists(_portletsDir) ? Files.find(_portletsDir, 999, (p, bfa) -> containsDocrootFilter.test(p)).collect(Collectors.toList()) : Collections.emptyList();
		List<Path> portletPlugins = portlets.stream().filter(portletPlugin -> !serviceBuilderPlugins.contains(portletPlugin)).collect(Collectors.toList());

		List<Path> hookPlugins = Files.exists(_hooksDir) ? Files.find(_hooksDir, 999, (p, bfa) -> containsDocrootFilter.test(p)).collect(Collectors.toList()) : Collections.emptyList();
		List<Path> layoutPlugins = Files.exists(_layouttplDir) ? Files.find(_layouttplDir, 999, (p, bfa) -> containsDocrootFilter.test(p)).collect(Collectors.toList()) : Collections.emptyList();
		List<Path> webPlugins = Files.exists(_websDir) ? Files.find(_websDir, 999, (p, bfa) -> containsDocrootFilter.test(p)).collect(Collectors.toList()) : Collections.emptyList();
		List<Path> themePlugins = Files.exists(_themesDir) ? Files.find(_themesDir, 999, (p, bfa) -> containsDocrootFilter.test(p)).collect(Collectors.toList()) : Collections.emptyList();

		if (_options.isAll()) {
			serviceBuilderPlugins.stream().forEach(this::convertToServiceBuilderWarProject);
			portletPlugins.stream().forEach(this::convertToWarProject);
			hookPlugins.stream().forEach(this::convertToWarProject);
			webPlugins.stream().forEach(this::convertToWarProject);
			layoutPlugins.stream().forEach(this::convertToLayoutWarProject);

			if (_options.isThemeBuilder()) {
				themePlugins.stream().forEach(this::convertToThemeBuilderWarProject);
			}
			else {
				themePlugins.stream().forEach(this::convertToThemeProject);
			}
		}
		else if (_options.isList()) {
			_blade.out().println("The following is a list of projects available to convert:\n");

			Stream<Path> plugins = concat(serviceBuilderPlugins.stream(), concat(portletPlugins.stream(), concat(hookPlugins.stream(), concat(webPlugins.stream(), concat(layoutPlugins.stream(), themePlugins.stream())))));
			
			plugins.forEach(plugin -> _blade.out().println("\t" + plugin.getFileName().toString()));
		}
		else {
			Path pluginDir = findPluginDir(pluginName);

			if (pluginDir == null) {
				_blade.error("Plugin does not exist.");

				return;
			}

			if (pluginDir.startsWith(_portletsDir)) {
				if (isServiceBuilderPlugin(pluginDir)) {
					convertToServiceBuilderWarProject(pluginDir);
				}
				else {
					convertToWarProject(pluginDir);
				}
			}
			if (pluginDir.startsWith(_hooksDir) ||
					pluginDir.startsWith(_websDir)) {

				convertToWarProject(pluginDir);
			}
			else if(pluginDir.startsWith(_layouttplDir)) {
				convertToLayoutWarProject(pluginDir);
			}
			else if(pluginDir.startsWith(_themesDir)) {
				if (_options.isThemeBuilder()) {
					convertToThemeBuilderWarProject(pluginDir);
				}
				else {
					convertToThemeProject(pluginDir);
				}
			}
		}
	}

	public static <T> Stream<T> concat(Stream<? extends T> lhs, Stream<? extends T> rhs) {
	    return Stream.concat(lhs, rhs);
	}

	private void convertToThemeProject(Path themePlugin)  {
		try {
			new ConvertThemeCommand(_blade, _options).execute();
		}
		catch (Exception e) {
			_blade.error("Error upgrading project %s\n%s", themePlugin.getFileName().toString(), e.getMessage());
		}
	}

	private void convertToServiceBuilderWarProject(Path pluginDir) {
		try {
			convertToWarProject(pluginDir);

			final List<String> arguments; {
				if (_options.isAll()) {
					arguments = new ArrayList<>();

					String pluginName = pluginDir.getFileName().toString();

					arguments.add(pluginName);

					if (pluginName.endsWith("-portlet")) {
						arguments.add(pluginName.replaceAll("-portlet$", ""));
					}
				}
				else {
					arguments = _options.getName();
				}
			}
			List<String> list = new ArrayList<>(_options.getName());
			list.addAll(arguments);
			
			ConvertArgs convertArgs = new ConvertArgs(_options.isAll(), _options.isList(), _options.isThemeBuilder(), list);
			new ConvertServiceBuilderCommand(_blade, convertArgs).execute();
		}
		catch (Exception e) {
			_blade.error("Error upgrading project %s\n%s", pluginDir.getFileName().toString(), e.getMessage());
		}
	}

	private boolean isServiceBuilderPlugin(Path pluginDir) {
		return hasServiceXmlFile(pluginDir);
	}

	private Path findPluginDir(final String pluginName) throws Exception {
		return Files.find(_pluginsSdkDir, 999, (dir, attrs) ->
			Files.isDirectory(dir) && 
			dir.getName(dir.getNameCount() - 1).toString().equals(pluginName)
		).findAny()
		.orElse(null);
	}

	private void convertToThemeBuilderWarProject(Path themePlugin) {
		try {
			Files.createDirectories(_warsDir);

			CreateCommand createCommand = new CreateCommand(_blade);

			ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

			projectTemplatesArgs.setDestinationDir(_warsDir.toFile());
			projectTemplatesArgs.setName(themePlugin.getFileName().toString());
			projectTemplatesArgs.setTemplate("theme");

			createCommand.execute(projectTemplatesArgs);

			Path docroot = themePlugin.resolve(
				"docroot");

			Path diffsDir = docroot.resolve("_diffs");

			if (Files.notExists(diffsDir)) {
				throw new IllegalStateException(
					"theme " + themePlugin.getFileName() + " does not contain a docroot/_diffs folder.  "
							+ "Please correct it and try again.");
			}

			// only copy _diffs and WEB-INF

			Path newThemeDir = _warsDir.resolve(themePlugin.getFileName());

			Path webapp = newThemeDir.resolve(Paths.get("src", "main","webapp"));

			Files.walkFileTree(diffsDir, new CopyDirVisitor(diffsDir, webapp, StandardCopyOption.REPLACE_EXISTING));

			Path webinfDir = docroot.resolve("WEB-INF");

			Path newWebinfDir = webapp.resolve("WEB-INF");

			Files.walkFileTree(webinfDir, new CopyDirVisitor(webinfDir, newWebinfDir, StandardCopyOption.REPLACE_EXISTING));

			File[] others = docroot.toFile().listFiles( new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return !"_diffs".equals(name) && !"WEB-INF".equals(name);
				}
			});

			if (others != null && others.length > 0) {
				Path backup = newThemeDir.resolve("docroot_backup");

				Files.createDirectories(backup);

				for (File other : others) {
					Files.move(other.toPath(), backup.resolve(other.getName()));
				}
			}

			IO.delete(themePlugin);
		}
		catch (Exception e) {
			_blade.error("Error upgrading project %s\n%s", themePlugin.getFileName().toString(), e.getMessage());
		}
	}

	private void convertToLayoutWarProject(Path layoutPluginDir) {
		try {
			Files.createDirectories(_warsDir);

			Files.move(layoutPluginDir, _warsDir.resolve(layoutPluginDir.getFileName()));

			Path warDir = _warsDir.resolve(layoutPluginDir.getFileName());

			Path docrootSrc = warDir.resolve(Paths.get("docroot", "WEB-INF", "src"));

			if (Files.exists(docrootSrc)) {
				throw new IllegalStateException(
					"layouttpl project " + layoutPluginDir.getFileName() + " contains java src at " +
							docrootSrc.toAbsolutePath().toString() + ". Please remove it before continuing.");
			}

			Path webapp = warDir.resolve(Paths.get("src", "main", "webapp"));

			Files.createDirectories(webapp);

			Path docroot = warDir.resolve("docroot");

			for(File docrootFile : docroot.toFile().listFiles()) {
				Files.move(docrootFile.toPath(), webapp.resolve(docrootFile.getName()));
			}

			Files.deleteIfExists(docroot);
			Files.deleteIfExists(warDir.resolve("build.xml"));
			Files.deleteIfExists(warDir.resolve(".classpath"));
			Files.deleteIfExists(warDir.resolve(".project"));
			Files.deleteIfExists(warDir.resolve(".settings"));
		}
		catch (Exception e) {
			_blade.error("Error upgrading project %s\n%s", layoutPluginDir.getFileName().toString(), e.getMessage());
		}
	}

	private void convertToWarProject(Path pluginDir) {
		try {
			Files.createDirectories(_warsDir);
			
			Path warDir = _warsDir.resolve(pluginDir.getFileName());

			Files.move(pluginDir, warDir);

			Path src = warDir.resolve(Paths.get("src", "main", "java"));

			Files.createDirectories(src);

			Path docrootSrc = warDir.resolve(Paths.get("docroot", "WEB-INF", "src"));

			if (Files.exists(docrootSrc)) {
				for (Path docrootSrcFile : Files.list(docrootSrc).collect(Collectors.toSet())) {
					Files.move(docrootSrcFile, src.resolve(docrootSrcFile.getFileName()));
				}

				Files.delete(docrootSrc);
			}

			Path webapp = warDir.resolve(Paths.get("src", "main", "webapp"));

			Files.createDirectories(webapp);

			Path docroot = warDir.resolve("docroot");

			for (Path docrootFile : Files.list(docroot).collect(Collectors.toSet())) {
				Files.move(docrootFile, webapp.resolve(docrootFile.getFileName()));
			}
			
			Files.delete(docroot);
			
			for (Path path : (Iterable<Path>)Arrays.asList("build.xml", ".classpath", ".project", ".settings", "ivy.xml.MD5").stream().map(x -> warDir.resolve(x))::iterator)
			{
				Files.deleteIfExists(path);
			}

			List<String> dependencies = new ArrayList<>();
			dependencies.add("compileOnly group: \"com.liferay.portal\", name: \"com.liferay.portal.kernel\", version: \"2.0.0\"");
			dependencies.add("compileOnly group: \"javax.portlet\", name: \"portlet-api\", version: \"2.0\"");
			dependencies.add("compileOnly group: \"javax.servlet\", name: \"javax.servlet-api\", version: \"3.0.1\"");

			Path ivyFile = warDir.resolve("ivy.xml");

			if (Files.exists(ivyFile)) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(ivyFile.toFile());
				Element documentElement = doc.getDocumentElement();
				documentElement.normalize();

				NodeList depElements = documentElement.getElementsByTagName("dependency");

				if (depElements != null && depElements.getLength() > 0) {
					for (int i = 0; i < depElements.getLength(); i++) {
						Node depElement = depElements.item(i);

						String name = getAttr(depElement, "name");
						String org = getAttr(depElement, "org");
						String rev = getAttr(depElement, "rev");

						if (name != null && org != null && rev != null) {
							dependencies.add(MessageFormat.format("compile group: ''{0}'', name: ''{1}'', version: ''{2}''", org, name, rev));
						}
					}
				}

				Files.delete(ivyFile);
			}

			StringBuilder depsContent = new StringBuilder();

			depsContent.append("dependencies {\n");

			for (String dep : dependencies) {
				depsContent.append("\t" + dep + "\n");
			}

			depsContent.append("}");

			Path gradleFile = warDir.resolve("build.gradle");

			Files.write(gradleFile, depsContent.toString().getBytes());
		}
		catch (Exception e) {
			_blade.error("Error upgrading project %s\n%s", pluginDir.getFileName().toString(), e.getMessage());
		}
	}

	private String getAttr(Node item, String attrName) {
		if (item != null) {
			NamedNodeMap attrs = item.getAttributes();

			if (attrs != null) {
				Node attr = attrs.getNamedItem(attrName);

				if (attr != null) {
					return attr.getNodeValue();
				}
			}
		}

		return null;
	}

	private boolean hasServiceXmlFile(Path pathname) {
		return Files.exists(pathname.resolve(Paths.get("docroot", "WEB-INF", "service.xml")));
	}

	private final blade _blade;
	private final ConvertArgs _options;
	private final Path _hooksDir;
	private final Path _layouttplDir;
	private final Path _pluginsSdkDir;
	private final Path _portletsDir;
	private final Path _themesDir;
	private final Path _warsDir;
	private final Path _websDir;

}