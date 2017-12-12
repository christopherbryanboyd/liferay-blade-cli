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

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
			File dir, String serverType)
		throws Exception {

		if (!dir.exists() || dir.listFiles() == null) {
			_blade.error(
				" bundles folder does not exist in Liferay Workspace, execute 'gradlew initBundle' in order to create it.");

			return;
		}

		for (File file : dir.listFiles()) {
			String fileName = file.getName();

			if (fileName.startsWith(serverType) && file.isDirectory()) {
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
			File dir)
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
			_blade, executable + debug, new File(dir, "bin"), enviroment);

		process.waitFor();

	}

	private void commmandTomcat(File dir)
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

		File logs = new File(dir, "logs");
		logs.mkdirs();

		File catalinaOut = new File(logs, "catalina.out");
		catalinaOut.createNewFile();

		final Process process = Util.startProcess(
			_blade, executable + startCommand, new File(dir, "bin"),
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

		File gradleWrapper = Util.getGradleWrapper(_blade.getBase());

		File rootDir = gradleWrapper.getParentFile();

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

			File tempLiferayHome = new File(liferayHomePath);
			File liferayHomeDir = null;

			if (tempLiferayHome.isAbsolute()) {
				liferayHomeDir = tempLiferayHome.getCanonicalFile();
			}
			else {
				File tempFile = new File(rootDir, liferayHomePath);
				liferayHomeDir = tempFile.getCanonicalFile();
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
									rootDir.getCanonicalPath());

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
						new File(appServerParentDir), serverType);
				}
				else {
					commandServer(
						new File(rootDir, appServerParentDir), serverType);
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