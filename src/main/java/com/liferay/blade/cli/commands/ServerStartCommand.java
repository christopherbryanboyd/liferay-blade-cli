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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.Workspace;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.ServerStartArgs;

/**
 * @author David Truong
 */
public class ServerStartCommand {

	public static final String DESCRIPTION =
			"Start server defined by your Liferay project";
	public ServerStartCommand(blade blade, ServerStartArgs options) {
		_blade = blade;
		_options = options;
	}

	private void commandServer(
			Path dir, String serverType)
		throws Exception {

		if (Files.notExists(dir) || !Files.list(dir).findAny().isPresent()) {
			_blade.error(
				" bundles folder does not exist in Liferay Workspace, execute 'gradlew initBundle' in order to create it.");

			return;
		}

		for (Path file : Files.list(dir).collect(Collectors.toList())) {
			Path fileName = file.getFileName();

			if (fileName.startsWith(serverType) && Files.isDirectory(file)) {
				if (serverType.equals("tomcat")) {
					commmandTomcat(file);

					return;
				}
				else if (serverType.equals("jboss") ||
						 serverType.equals("wildfly")) {

					commmandJBossWildfly(file);

					return;
				}
			}
		}

		_blade.error(serverType + " not supported");
	}

	private void commmandJBossWildfly(
			Path dir)
		throws Exception {

		Map<String, String> enviroment = new HashMap<>();

		String executable = "./standalone.sh";

		if (Util.isWindows()) {
			executable = "standalone.bat";
		}


		String debug = "";

		if (_options.isDebug()) {
			debug = " --debug";
		}

		Process process = Util.startProcess(
			_blade, executable + debug, dir.resolve("bin"), enviroment);

		process.waitFor();

	}

	private void commmandTomcat(Path dir)
		throws Exception {

		Map<String, String> enviroment = new HashMap<>();

		enviroment.put("CATALINA_PID", "catalina.pid");

		String executable = "./catalina.sh";

		if (Util.isWindows()) {
			executable = "catalina.bat";
		}

		String startCommand = " run";

		if (_options.isBackground()) {
			startCommand = " start";
		}
		else if (_options.isDebug()) {
			startCommand = " jpda " + startCommand;
		}

		Path logs = dir.resolve("logs");
		Files.createDirectory(logs);

		Path catalinaOut = logs.resolve("catalina.out");
		Files.createFile(catalinaOut);

		final Process process = Util.startProcess(
			_blade, executable + startCommand, dir.resolve("bin"),
			enviroment);

		Runtime runtime = Runtime.getRuntime();

		runtime.addShutdownHook(new Thread() {
			public void run() {
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					_blade.error("Could not wait for process to end " +
						"before shutting down");
				}
			}
		});

		if (_options.isBackground() && _options.isTail()) {
			Process tailProcess = Util.startProcess(
				_blade, "tail -f catalina.out", logs, enviroment);

			tailProcess.waitFor();
		}

	}

	public void execute()
		throws Exception {

		Path gradleWrapper = Util.getGradleWrapper(_blade.getBase());

		Path rootDir = gradleWrapper.getParent();

		String serverType = null;

		if (Util.isWorkspace(rootDir)) {
			Properties properties = Util.getGradleProperties(rootDir);

			String liferayHomePath = properties.getProperty(
				Workspace.DEFAULT_LIFERAY_HOME_DIR_PROPERTY);

			if ((liferayHomePath == null) || liferayHomePath.equals("")) {
				liferayHomePath = Workspace.DEFAULT_LIFERAY_HOME_DIR;
			}

			serverType = properties.getProperty(
				Workspace.DEFAULT_BUNDLE_ARTIFACT_NAME_PROPERTY);

			if (serverType == null) {
				serverType = Workspace.DEFAULT_BUNDLE_ARTIFACT_NAME;
			}

			if (serverType.contains("jboss")) {
				serverType = "jboss";
			}
			else if (serverType.contains("wildfly")) {
				serverType = "wildfly";
			}
			else if (serverType.contains("tomcat")) {
				serverType = "tomcat";
			}

			Path tempLiferayHome = Paths.get(liferayHomePath);
			Path liferayHomeDir = null;

			if (tempLiferayHome.isAbsolute()) {
				liferayHomeDir = tempLiferayHome.normalize();
			}
			else {
				Path tempFile = rootDir.resolve(liferayHomePath);
				liferayHomeDir = tempFile.normalize();
			}

			commandServer(liferayHomeDir, serverType);
		}
		else {
			try {
				List<Properties> propertiesList = Util.getAppServerProperties(
					rootDir);

				String appServerParentDir = "";

				for (Properties properties : propertiesList) {
					if (appServerParentDir.equals("")) {
						String appServerParentDirTemp = properties.getProperty(
							Util.APP_SERVER_PARENT_DIR_PROPERTY);

						if ((appServerParentDirTemp != null) &&
							!appServerParentDirTemp.equals("")) {

							appServerParentDirTemp =
								appServerParentDirTemp.replace(
									"${project.dir}",
									rootDir.toRealPath().toString());

							appServerParentDir = appServerParentDirTemp;
						}
					}

					if ((serverType == null) || serverType.equals("")) {
						String serverTypeTemp = properties.getProperty(
							Util.APP_SERVER_TYPE_PROPERTY);

						if ((serverTypeTemp != null) &&
							!serverTypeTemp.equals("")) {

							serverType = serverTypeTemp;
						}
					}
				}

				if (appServerParentDir.startsWith("/") ||
					appServerParentDir.contains(":")) {

					commandServer(
						Paths.get(appServerParentDir), serverType);
				}
				else {
					commandServer(
						rootDir.resolve(appServerParentDir), serverType);
				}
			}
			catch (Exception e) {
				_blade.error(
					"Please execute this command from a Liferay project");
			}
		}
	}

	private blade _blade;
	
	private ServerStartArgs _options;

}