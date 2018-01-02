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

import aQute.lib.io.IO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Test;

import com.liferay.project.templates.internal.util.FileUtil;

/**
 * @author Gregory Amerson
 */
public class ConvertCommandTest {

	@After
	public void cleanUp() throws Exception {
		Files.deleteIfExists(workspaceDir.getParentFile().toPath());
	}

	@Test
	public void testAll() throws Exception {
		File testdir = IO.getFile("build/testUpgradePluginsSDKTo70");

		if (testdir.exists()) {
			IO.deleteWithException(testdir);
			assertFalse(testdir.exists());
		}

		testdir.mkdirs();

		Util.unzip(new File("test-resources/projects/plugins-sdk-with-git.zip").toPath(), testdir.toPath());

		assertTrue(testdir.exists());

		File projectDir = new File(testdir, "plugins-sdk-with-git");

		String[] args = {"-b", projectDir.getPath(), "init", "-u"};

		new bladenofail().run(args);

		args = new String[] {"-b", projectDir.getPath(), "convert", "-a"};

		new bladenofail().run(args);

		assertTrue(new File(testdir, "plugins-sdk-with-git/modules/sample-service-builder/sample-service-builder-api").exists());

		assertTrue(new File(testdir, "plugins-sdk-with-git/modules/sample-service-builder/sample-service-builder-service").exists());

		assertTrue(new File(testdir, "plugins-sdk-with-git/wars/sample-service-builder-portlet").exists());
		/*Path testdir = Paths.get("build","testUpgradePluginsSDKTo70");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Files.createDirectories(testdir);

		Util.unzip(Paths.get("test-resources","projects","plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve( "plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		args = new String[] {"-b", projectDir.toString(), "convert", "-a"};

		new bladenofail().run(args);

		assertTrue(Files.exists(testdir.resolve(Paths.get("plugins-sdk-with-git", "modules", "sample-service-builder", "sample-service-builder-api"))));

		assertTrue(Files.exists(testdir.resolve(Paths.get("plugins-sdk-with-git", "modules", "sample-service-builder", "sample-service-builder-service"))));;

		assertTrue(Files.exists(testdir.resolve(Paths.get("plugins-sdk-with-git", "wars", "sample-service-builder-portlet"))));*/
	}

	@Test
	public void testMoveLayouttplToWars() throws Exception {
		Path testdir = Paths.get("build", "testMoveLayouttplToWars");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Util.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		args = new String[] {"-b", projectDir.toString(), "convert", "1-2-1-columns-layouttpl"};

		new bladenofail().run(args);

		Path layoutWar = projectDir.resolve(Paths.get("wars", "1-2-1-columns-layouttpl"));

		assertTrue(Files.exists(layoutWar));

		assertTrue(Files.notExists(layoutWar.resolve("build.xml")));

		assertTrue(Files.notExists(layoutWar.resolve("build.gradle")));

		assertTrue(Files.notExists(layoutWar.resolve("docroot")));
	}

	@Test
	public void testMoveThemesToWars() throws Exception {
		Path testdir = Paths.get("build", "testMoveThemesToWar");

		if (Files.exists(testdir)) { 
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Util.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve( "plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		Path theme = projectDir.resolve(Paths.get("wars", "sample-styled-minimal-theme"));

		args = new String[] {"-b", projectDir.toString(), "convert", "-t", "sample-styled-minimal-theme"};

		new bladenofail().run(args);

		assertTrue(Files.exists(theme));

		assertTrue(Files.notExists(theme.resolve("build.xml")));

		assertTrue(Files.exists(theme.resolve("build.gradle")));

		assertTrue(Files.notExists(theme.resolve("docroot")));

		assertTrue(Files.exists(theme.resolve(Paths.get("src", "main", "webapp"))));

		assertTrue(Files.notExists(theme.resolve(Paths.get("src", "main", "webapp", "_diffs"))));

		assertTrue(Files.notExists(projectDir.resolve(Paths.get("plugins-sdk", "themes", "sample-styled-minimal-theme"))));

		args = new String[] {"-b", projectDir.toString(), "convert", "-t", "sample-styled-advanced-theme"};

		new bladenofail().run(args);

		Path advancedTheme = projectDir.resolve(Paths.get("wars", "sample-styled-advanced-theme"));

		assertTrue(Files.exists(advancedTheme));

		assertTrue(Files.notExists(advancedTheme.resolve("build.xml")));

		assertTrue(Files.exists(advancedTheme.resolve("build.gradle")));

		assertTrue(Files.notExists(advancedTheme.resolve("docroot")));

		assertTrue(Files.exists(advancedTheme.resolve(Paths.get("src", "main", "webapp"))));

		assertTrue(Files.notExists(advancedTheme.resolve(Paths.get("src", "main", "webapp","_diffs"))));

		assertTrue(Files.notExists(projectDir.resolve(Paths.get("plugins-sdk","themes","sample-styled-advanced-theme"))));
	}

	@Test
	public void testMovePluginsToWars() throws Exception {
		Path testdir = Paths.get("build","testMovePluginsToWars");

		if (Files.exists(testdir)) { 
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Util.unzip(Paths.get("test-resources","projects","plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		args = new String[] {"-b", projectDir.toString(), "convert", "sample-application-adapter-hook"};

		new bladenofail().run(args);

		Path sampleExpandoHook = projectDir.resolve(Paths.get("wars", "sample-application-adapter-hook"));

		assertTrue(Files.exists(sampleExpandoHook));

		assertTrue(Files.notExists(projectDir.resolve(Paths.get("plugins-sdk","hooks","sample-application-adapter-hook"))));

		args = new String[] {"-b", projectDir.toString(), "convert", "sample-servlet-filter-hook"};

		new bladenofail().run(args);

		Path sampleServletFilterHook = projectDir.resolve(Paths.get("wars","sample-servlet-filter-hook"));

		assertTrue(Files.exists(sampleServletFilterHook));

		assertTrue(Files.notExists(projectDir.resolve(Paths.get("plugins-sdk","hooks","sample-servlet-filter-hook"))));
	}

	private Path setupWorkspace(String name) throws Exception {
		Path testdir = Paths.get("build", name);

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Util.unzip(Paths.get("test-resources", "projects", "plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		assertTrue(Files.exists(projectDir.resolve("plugins-sdk")));

		return projectDir;
	}

	@Test
	public void testThemeDocrootBackup() throws Exception {
		Path projectDir = setupWorkspace("testThemeDocrootBackup");

		String[] args = {"-b", projectDir.toString(), "convert", "-t", "sample-html4-theme"};

		new bladenofail().run(args);

		assertTrue(Files.exists(projectDir.resolve(Paths.get("wars","sample-html4-theme","docroot_backup","other","afile"))));
	}

	@Test
	public void testReadIvyXml() throws Exception {
		Path projectDir = setupWorkspace("readIvyXml");

		String[] args = {"-b", projectDir.toString(), "convert", "sample-dao-portlet"};

		new bladenofail().run(args);

		contains(
			projectDir.resolve(Paths.get("wars","sample-dao-portlet","build.gradle")),
			".*compile group: 'c3p0', name: 'c3p0', version: '0.9.0.4'.*",
			".*compile group: 'mysql', name: 'mysql-connector-java', version: '5.0.7'.*");

		args = new String[] {"-b", projectDir.toString(), "convert", "sample-tapestry-portlet"};

		new bladenofail().run(args);

		contains(
			projectDir.resolve(Paths.get("wars","sample-tapestry-portlet","build.gradle")),
			".*compile group: 'hivemind', name: 'hivemind', version: '1.1'.*",
			".*compile group: 'hivemind', name: 'hivemind-lib', version: '1.1'.*",
			".*compile group: 'org.apache.tapestry', name: 'tapestry-annotations', version: '4.1'.*",
			".*compile group: 'org.apache.tapestry', name: 'tapestry-framework', version: '4.1'.*",
			".*compile group: 'org.apache.tapestry', name: 'tapestry-portlet', version: '4.1'.*");

		assertTrue(Files.notExists(projectDir.resolve(Paths.get("wars","sample-tapestry-portlet","ivy.xml"))));
	}

	private void contains(Path file, String... patterns) throws Exception {
		String content = new String(Files.readAllBytes(file));

		for (String pattern : patterns) {
			contains(content, pattern);
		}
	}

	private void contains(String content, String pattern) throws Exception {
		assertTrue(
			Pattern.compile(
				pattern,
				Pattern.MULTILINE | Pattern.DOTALL).matcher(content).matches());
	}
	private final File workspaceDir = IO.getFile("build/test/workspace");

}