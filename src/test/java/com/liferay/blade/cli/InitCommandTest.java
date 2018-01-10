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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.gradle.testkit.runner.BuildTask;
import org.junit.After;
import org.junit.Test;

import com.liferay.project.templates.internal.util.FileUtil;

/**
 * @author Gregory Amerson
 */
public class InitCommandTest {

	@After
	public void cleanUp() throws Exception {
		if (Files.exists(workspaceDir))
			FileUtil.deleteDir(workspaceDir);
	}

	@Test
	public void testBladeInitUpgradePluginsSDKTo70() throws Exception {
		Path testdir = Paths.get("build","testUpgradePluginsSDKTo70");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Files.createDirectories(testdir);

		Util.unzip(Paths.get("test-resources","projects","plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		Path buildProperties = projectDir.resolve(Paths.get("plugins-sdk","build.properties"));

		Properties props = new Properties();
		
		try (InputStream inputStream = Files.newInputStream(buildProperties)) {
			props.load(inputStream);
		
		}

		String version = props.getProperty("lp.version");

		assertEquals("7.0.0", version);
	}

	@Test
	public void testBladeInitDontLoseGitDirectory() throws Exception {
		Path testdir = Paths.get("build","testBladeInitDontLoseGitDirectory");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Files.createDirectories(testdir);

		Util.unzip(Paths.get("test-resources","projects","plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		Path gitdir = projectDir.resolve(".git");

		assertTrue(Files.exists(gitdir));

		Path oldGitIgnore = projectDir.resolve(Paths.get("plugins-sdk",".gitignore"));

		assertTrue(Files.exists(oldGitIgnore));
	}

	@Test
	public void testDefaultInitWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"-b", workspaceDir.toString(), "init"};

		new bladenofail().run(args);

		assertTrue(Files.exists(workspaceDir));

		assertTrue(Files.exists(workspaceDir.resolve("build.gradle")));

		assertTrue(Files.exists(workspaceDir.resolve("modules")));

		assertFalse(Files.exists(workspaceDir.resolve("com")));

		verifyGradleBuild();
	}

	@Test
	public void testDefaultInitWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"-b", workspaceDir.toString(), "init"};

		try {
			Files.createDirectories(workspaceDir);
		}
		catch (Exception e) {
			fail("Unable to create workspace dir: " + e.getMessage());
		}

		Files.createFile(workspaceDir.resolve("foo"));

		new bladenofail().run(args);

		assertTrue(Files.notExists(workspaceDir.resolve("build.gradle")));
	}

	@Test
	public void testDefaultInitWorkspaceDirectoryHasFilesForce() throws Exception {
		String[] args = {"-b", workspaceDir.toString(), "init", "-f"};

		try {
			Files.createDirectories(workspaceDir);
		}
		catch (Exception e) {
			fail("Unable to create workspace dir: " + e.getMessage());
		}

		Files.createFile(workspaceDir.resolve("foo"));

		new bladenofail().run(args);

		assertTrue(Files.exists(workspaceDir));

		assertTrue(Files.exists(workspaceDir.resolve("build.gradle")));

		assertTrue(Files.exists(workspaceDir.resolve("modules")));

		verifyGradleBuild();
	}

	@Test
	public void testInitInPluginsSDKDirectory() throws Exception {
		String[] args = {"-b", workspaceDir.toString(), "init", "-u"};

		makeSDK(workspaceDir);

		new bladenofail().run(args);

		assertTrue(Files.exists(workspaceDir.resolve("build.gradle")));

		assertTrue(Files.exists(workspaceDir.resolve("modules")));

		assertTrue(Files.exists(workspaceDir.resolve("themes")));

		assertTrue(Files.notExists(workspaceDir.resolve("portlets")));

		assertTrue(Files.notExists(workspaceDir.resolve("hooks")));

		assertTrue(Files.notExists(workspaceDir.resolve("build.properties")));

		assertTrue(Files.notExists(workspaceDir.resolve("build.xml")));

		assertTrue(Files.exists(workspaceDir.resolve(Paths.get("plugins-sdk","build.properties"))));

		assertTrue(Files.exists(workspaceDir.resolve(Paths.get("plugins-sdk","build.xml"))));
	}

	@Test
	public void testInitWithNameWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {
			"-b", workspaceDir.toString(), "init", "-f", "newproject"
		};

		Path newproject = workspaceDir.resolve("newproject");

		Files.createDirectories(newproject);

		new bladenofail().run(args);

		assertTrue(Files.exists(newproject.resolve("build.gradle")));

		assertTrue(Files.exists(newproject.resolve("modules")));

		String contents = new String(Files.readAllBytes(newproject.resolve("settings.gradle")));

		assertTrue(contents, contents.contains("1.7.1"));
	}

	@Test
	public void testInitWithNameWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {
			"-b", workspaceDir.toString(), "init", "newproject"
		};

		Files.createDirectories(workspaceDir.resolve("newproject"));

		Files.createFile(workspaceDir.resolve(Paths.get("newproject","foo")));

		new bladenofail().run(args);

		assertTrue(Files.notExists(workspaceDir.resolve(Paths.get("newproject","build.gradle"))));
	}

	@Test
	public void testInitWithNameWorkspaceNotExists() throws Exception {
		String[] args = {
			"-b", workspaceDir.toString(), "init", "newproject"
		};

		try {
			Files.createDirectories(workspaceDir);
			} catch(Exception e) {
			fail("Unable to create workspace dir");
		}

		new bladenofail().run(args);

		assertTrue(Files.exists(workspaceDir.resolve(Paths.get("newproject", "build.gradle"))));

		assertTrue(Files.exists(workspaceDir.resolve(Paths.get("newproject","modules"))));
	}

	private void createBundle(Path workspaceDir) throws Exception {
		Path projectPath = Paths.get("build","test","workspace","modules");

		String[] args = {"create", "-d", projectPath.toString(), "foo"};

		new bladenofail().run(args);

		Path file = projectPath.resolve("foo");
		
		Path bndFile = file.resolve("bnd.bnd");

		assertTrue(Files.exists(file));

		assertTrue(Files.exists(bndFile));
	}
	

	private void verifyGradleBuild() throws Exception{
		createBundle(workspaceDir);

		Path projectPath = workspaceDir.resolve("modules");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspaceDir, "jar");

		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);

		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("foo"), "foo-1.0.0.jar");
	}

	private static void makeSDK(Path dir) throws IOException {
		Files.createDirectories(dir.resolve("portlets"));
		Files.createDirectories(dir.resolve("hooks"));
		Files.createDirectories(dir.resolve("layouttpl"));
		Files.createDirectories(dir.resolve("themes"));
		Files.createFile(dir.resolve("build.properties"));
		Files.createFile(dir.resolve("build.xml"));
		Files.createFile(dir.resolve("build-common.xml"));
		Files.createFile(dir.resolve("build-common-plugin.xml"));
	}

	private final Path workspaceDir = Paths.get("build","test","workspace");

}