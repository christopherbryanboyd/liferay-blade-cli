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

package com.liferay.blade.extensions.maven.profile.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.liferay.blade.cli.gradle.ProcessResult;
import com.liferay.blade.cli.util.BladeUtil;

import net.jmatrix.jproperties.JProperties;

/**
 * @author Christopher Bryan Boyd
 */
public class MavenUtil {

	public static ProcessResult executeGoals(String projectPath, boolean throwErrors, String... goals) {
		Objects.requireNonNull(goals, "Goals must be specified");

		if (!(goals.length > 0)) {
			throw new RuntimeException("Goals must be specified");
		}

		String os = System.getProperty("os.name");

		boolean windows = false;

		os = os.toLowerCase();

		if (os.startsWith("win")) {
			windows = true;
		}

		boolean buildSuccess = false;

		int exitValue = 1;

		StringBuilder stringBuilder = new StringBuilder();

		for (String goal : goals) {
			stringBuilder.append(goal + " ");
		}

		StringBuilder output = new StringBuilder();
		StringBuilder error = new StringBuilder();

		String command = null;

		try {
			Runtime runtime = Runtime.getRuntime();

			command = (windows ? "cmd.exe /c .\\mvnw.cmd" : "./mvnw") + " " + stringBuilder.toString();

			Process process = runtime.exec(command, null, new File(projectPath));

			BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader processError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			String line = null;

			while ((line = processOutput.readLine()) != null) {
				output.append(line);
				output.append(System.lineSeparator());

				if (line.contains("BUILD SUCCESS")) {
					buildSuccess = true;
				}
			}

			while ((line = processError.readLine()) != null) {
				error.append(line);
				error.append(System.lineSeparator());
			}

			exitValue = process.waitFor();
		}
		catch (Exception e) {
			StringBuilder sb = new StringBuilder();

			sb.append("Project path: " + projectPath);
			sb.append(System.lineSeparator());
			sb.append("maven command failed: " + command);
			sb.append(System.lineSeparator());
			sb.append(e.getMessage());

			if (throwErrors) {
				throw new RuntimeException(sb.toString(), e);
			}
		}

		boolean exitValueCorrect = false;

		if (exitValue == 0) {
			exitValueCorrect = true;
		}

		if (throwErrors && (!exitValueCorrect || !buildSuccess)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Maven goals " + goals + " failed in project path " + projectPath);
			sb.append(System.lineSeparator());
			sb.append(output);
			sb.append(System.lineSeparator());
			sb.append(error);

			throw new RuntimeException(sb.toString());
		}

		return new ProcessResult(exitValue, output.toString(), error.toString());
	}

	public static ProcessResult executeGoals(String projectPath, String... goals) {
		return executeGoals(projectPath, true, goals);
	}

	public static Properties getMavenProperties(File baseDir) {
		try {
			File absoluteBaseDir = baseDir.getAbsoluteFile();

			Path absoluteBasePath = absoluteBaseDir.toPath();

			absoluteBasePath = absoluteBasePath.normalize();

			JProperties jProperties = new JProperties();

			jProperties.put("project.basedir", absoluteBasePath.toString());

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			File pomXmlFile;
			
			if (isWorkspace(absoluteBaseDir)) {
				pomXmlFile = getPomXMLFile(absoluteBaseDir);
			}
			else
			{
				pomXmlFile = new File(absoluteBaseDir, _POM_XML_FILE_NAME);
			}

			Document document = documentBuilder.parse(pomXmlFile);

			Element documentElement = document.getDocumentElement();

			documentElement.normalize();

			NodeList propertiesNodeList = document.getElementsByTagName("properties");

			Node propertiesNode = propertiesNodeList.item(0);

			if (propertiesNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList nodeList = propertiesNode.getChildNodes();

				for (int nodeInt = 0; nodeInt < nodeList.getLength(); nodeInt++) {
					Node sNode = nodeList.item(nodeInt);

					if (sNode.getNodeType() == Node.ELEMENT_NODE) {
						jProperties.put(sNode.getNodeName(), sNode.getTextContent());
					}
				}
			}

			Set<Entry<String, Object>> entrySet = jProperties.entrySet();

			Iterator<Entry<String, Object>> iterator = entrySet.iterator();

			Properties properties = new Properties();

			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();

				String key = entry.getKey();

				Object value = entry.getValue();

				properties.put(key, value);
			}

			return properties;
		}
		catch (Throwable th) {
			throw new RuntimeException("Unable to get maven properties", th);
		}
	}
	public static File getWorkspaceDir(File dir) {

		File mavenParent = BladeUtil.findParentFile(dir, new String[] {"pom.xml"}, true, MavenUtil::_isWorkspacePomFile);

		if (_isWorkspacePomFile(new File(mavenParent, "pom.xml"))) {
			return mavenParent;
		}
			File mavenPom = new File(dir, "pom.xml");

			if (mavenPom.exists() && _isWorkspacePomFile(mavenPom)) {
				return dir;
			}
		

		return null;
	}

    private static boolean _isWorkspacePomFile(File pomFile) {
		boolean pom = false;

		if ((pomFile != null) && "pom.xml".equals(pomFile.getName()) && pomFile.exists()) {
			pom = true;
		}

		if (pom) {
			try {
				String content = BladeUtil.read(pomFile);

				if (content.contains("portal.tools.bundle.support")) {
					return true;
				}
			}
			catch (Exception e) {
			}
		}

		return false;
	}
    
	public static boolean isWorkspace(File dir) {

		File workspaceDir = getWorkspaceDir(dir);

		if (Objects.isNull(dir) || Objects.isNull(workspaceDir)) {
			return false;
		}

			File pomFile = new File(workspaceDir, "pom.xml");

			if (_isWorkspacePomFile(pomFile)) {
				return true;
			}

			return false;
		

		
	}
	public static File getPomXMLFile(File dir) {
		return new File(getWorkspaceDir(dir), _POM_XML_FILE_NAME);
	}

	private static final String _POM_XML_FILE_NAME = "pom.xml";

}