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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.gradle.testkit.runner.BuildTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.liferay.blade.cli.util.FilesUtil;
import com.liferay.project.templates.internal.util.FileUtil;

import aQute.lib.io.IO;

/**
 * @author David Truong
 */
public class SamplesCommandTest {
	private Path testDir;

	@BeforeClass
	public static void setUpClass() throws Exception {
		FilesUtil.copy(Paths.get("wrapper.zip"), Paths.get("build","classes","java","test","wrapper.zip"));
	}

	@Before
	public void setUp() throws Exception {
		testDir = Files.createTempDirectory("samplestest");
	}

	@After
	public void cleanUp() throws Exception {
		if (Files.exists(testDir)) {
			FileUtil.deleteDir(testDir);
			assertTrue(Files.notExists(testDir));
		}
	}

	@AfterClass
	public static void cleanUpClass() throws Exception {
		Files.deleteIfExists(Paths.get("build","wrapper.zip"));
	}

	@Test
	public void testGetSample() throws Exception {
		String[] args = {
			"samples", "-d", testDir.resolve("test").toString(), "blade.friendlyurl"
		};

		new bladenofail().run(args);

		Path projectDir = testDir.resolve(Paths.get("test","blade.friendlyurl"));

		assertTrue(Files.exists(projectDir));

		Path buildFile = projectDir.resolve("build.gradle");

		assertTrue(Files.exists(buildFile));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectDir, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectDir, "blade.friendlyurl-1.0.0.jar");
	}

	@Test
	public void testGetSampleWithGradleWrapper() throws Exception {
		String[] args = {"samples", "-d", testDir.resolve("test").toString(), "blade.authenticator.shiro"};

		new bladenofail().run(args);

		Path projectDir = testDir.resolve(Paths.get("test","blade.authenticator.shiro"));

		assertTrue(Files.exists(projectDir));

		Path buildFile = projectDir.resolve("build.gradle");

		Path gradleWrapperJar = projectDir.resolve(Paths.get("gradle","wrapper","gradle-wrapper.jar"));

		Path gradleWrapperProperties = projectDir.resolve(Paths.get("gradle","wrapper","gradle-wrapper.properties"));

		Path gradleWrapperShell =projectDir.resolve("gradlew");

		assertTrue(Files.exists(buildFile));
		assertTrue(Files.exists(gradleWrapperJar));
		assertTrue(Files.exists(gradleWrapperProperties));
		assertTrue(Files.exists(gradleWrapperShell));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectDir, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectDir, "blade.authenticator.shiro-1.0.0.jar");
	}

	@Test
	public void testGetSampleWithGradleWrapperExisting() throws Exception {
		String[] initArgs = {"-b", testDir.resolve(Paths.get("test","workspace")).toString(), "init"};

		new bladenofail().run(initArgs);

		String[] samplesArgs = {"samples", "-d", testDir.resolve(Paths.get("test","workspace","modules")).toString(), "blade.authfailure"};

		new bladenofail().run(samplesArgs);

		Path projectDir = testDir.resolve(Paths.get("test","workspace","modules","blade.authfailure"));

		assertTrue(Files.exists(projectDir));

		Path buildFile = projectDir.resolve("build.gradle");

		Path gradleWrapperJar = projectDir.resolve(Paths.get("gradle","wrapper","gradle-wrapper.jar"));

		Path gradleWrapperProperties = projectDir.resolve(Paths.get("gradle","wrapper","gradle-wrapper.properties"));

		Path gradleWrapperShell = projectDir.resolve("gradlew");

		assertTrue(Files.exists(buildFile));
		assertTrue(Files.notExists(gradleWrapperJar));
		assertTrue(Files.notExists(gradleWrapperProperties));
		assertTrue(Files.notExists(gradleWrapperShell));

		Path workspaceDir = testDir.resolve(Paths.get("test","workspace"));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspaceDir, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectDir, "blade.authfailure-1.0.0.jar");
	}

	@Test
	public void testGetSampleWithDependencies() throws Exception {
		/*String[] args = {"samples", "-d", testDir.resolve("test").toString(), "blade.rest"};

		new bladenofail().run(args);

		Path projectDir = testDir.resolve(Paths.get("test","blade.rest"));

		assertTrue(Files.exists(projectDir));

		Path buildFile = projectDir.resolve("build.gradle");

		assertTrue(Files.exists(buildFile));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectDir, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectDir, "blade.rest-1.0.0.jar");*/
		
		String[] args = {"samples", "-d", testDir.toString() + "/test", "blade.rest"};

		new bladenofail().run(args);

		File projectDir = new File(testDir.toFile(), "test/blade.rest");

		assertTrue(projectDir.exists());

		File buildFile = IO.getFile(projectDir, "build.gradle");

		assertTrue(buildFile.exists());

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectDir.toPath(), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectDir.toPath(), "blade.rest-1.0.0.jar");

	}

	@Test
	public void testListSamples() throws Exception {
		String[] args = {"samples"};

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);

		new bladenofail(ps).run(args);

		String content = baos.toString();

		assertTrue(content.contains("blade.portlet.ds"));
	}

}