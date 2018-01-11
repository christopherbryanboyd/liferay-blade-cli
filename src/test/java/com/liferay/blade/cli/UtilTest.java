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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;

import com.liferay.blade.cli.util.FilesUtil;

import aQute.lib.io.IO;

/**
 * @author David Truong
 */
public class UtilTest {
	@After
	public void cleanUp() throws Exception {
		testdir = Paths.get("build/test");

		if (Files.exists(testdir)) {
			IO.delete(testdir);
			assertFalse(testdir.toFile().exists());
		}
	}

	@Test
	public void testAppServerProperties() throws Exception {
		Path dir = Paths.get("build", "test");

		Files.createDirectories(dir);

		Path appServerProperty1 = 
			dir.resolve(
			"app.server." + System.getProperty("user.name") + ".properties");

		Files.createFile(appServerProperty1);

		Path appServerProperty2 = dir.resolve("app.server.properties");

		Files.createFile(appServerProperty2);

		List<Properties> propertiesList = Util.getAppServerProperties(dir);

		assertTrue(propertiesList.size() == 2);
	}

	@Test
	public void testIsWorkspace1() throws Exception {
		Path workspace = Paths.get("build", "test", "workspace");

		Files.createDirectories(workspace);

		Path gradleFile = workspace.resolve("settings.gradle");

		String plugin = "apply plugin: \"com.liferay.workspace\"";

		Files.write(gradleFile, plugin.getBytes());

		assertTrue(Util.isWorkspace(workspace));
	}

	@Test
	public void testIsWorkspace2() throws Exception {
		Path workspace = Paths.get("build", "test", "workspace");

		Files.createDirectories(workspace);

		Path gradleFile = workspace.resolve("settings.gradle");

		String plugin = "apply plugin: 'com.liferay.workspace'";

		Files.write(gradleFile, plugin.getBytes());

		assertTrue(Util.isWorkspace(workspace));
	}

	@Test
	public void testIsWorkspace3() throws Exception {
		Path workspace = Paths.get("build", "test", "workspace");

		Files.createDirectories(workspace);

		Path buildFile = workspace.resolve("build.gradle");

		Path settingsFile = workspace.resolve("settings.gradle");

		Files.createFile(settingsFile);

		String plugin = "\napply   plugin:   \n\"com.liferay.workspace\"";

		Files.write(buildFile, plugin.getBytes());

		assertTrue(Util.isWorkspace(workspace));
	}

	private Path testdir;
}