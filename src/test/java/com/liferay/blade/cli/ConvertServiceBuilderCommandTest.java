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

import org.junit.After;
import org.junit.Test;

import com.liferay.blade.cli.util.FilesUtil;
import com.liferay.project.templates.internal.util.FileUtil;

/**
 * @author Terry Jia
 */
public class ConvertServiceBuilderCommandTest {

	public static final String SB_PROJECT_NAME = "sample-service-builder-portlet";

	@After
	public void cleanUp() throws Exception {
		if (Files.exists(workspaceDir.getParent())) {
			FileUtil.deleteDir(workspaceDir.getParent());
			assertTrue(Files.notExists(workspaceDir.getParent()));
		}
	}

	@Test
	public void testConvertServiceBuilderTasksPortletDefaultName() throws Exception {
		Path testdir = Paths.get("build","test-tasks-portlet-conversion");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		String[] args = {"-b", testdir.toString(), "init", "-u"};

		new bladenofail().run(args);

		Path pluginsSdkDir = testdir.resolve("plugins-sdk");

		FilesUtil.copy(Paths.get("test-resources","projects","tasks-plugins-sdk"), pluginsSdkDir);

		assertTrue(Files.exists(testdir.resolve(Paths.get("plugins-sdk","portlets","tasks-portlet"))));

		String[] convertArgs = {"-b", testdir.toString(), "convert", "tasks-portlet"};

		new bladenofail().run(convertArgs);

		assertTrue(Files.exists(testdir.resolve(Paths.get("modules","tasks","tasks-api","build.gradle"))));

		assertTrue(Files.exists(testdir.resolve(Paths.get("modules","tasks","tasks-api","src","main","java","com","liferay","tasks","exception"))));

		assertTrue(Files.exists(testdir.resolve(Paths.get("modules","tasks","tasks-service","src","main","java","com","liferay","tasks","model","impl","TasksEntryModelImpl.java"))));

		assertTrue(Files.exists(testdir.resolve(Paths.get("modules","tasks","tasks-service","src","main","java","com","liferay","tasks","service","impl","TasksEntryServiceImpl.java"))));

		assertTrue(Files.exists(testdir.resolve(Paths.get("modules","tasks","tasks-service","service.xml"))));

		assertFalse(Files.exists(testdir.resolve(Paths.get("wars","tasks-portlet","src","main","webapp","WEB-INF","service.xml"))));

		assertTrue(Files.exists(testdir.resolve(Paths.get("wars","tasks-portlet", "src","main","webapp","WEB-INF","portlet.xml"))));

		Path portletGradleFile = testdir.resolve(Paths.get("wars","tasks-portlet","build.gradle"));

		assertTrue(Files.exists(portletGradleFile));

		String content = new String(Files.readAllBytes(portletGradleFile));

		assertTrue(content.contains("compileOnly project(\":modules:tasks:tasks-api\")"));
	}

	@Test
	public void testConvertServiceBuilderTasksPortletCustomName() throws Exception {
		Path testdir = Paths.get("build","test-tasks-portlet-conversion");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		String[] args = {"-b", testdir.toString(), "init", "-u"};

		new bladenofail().run(args);

		Path pluginsSdkDir = testdir.resolve("plugins-sdk");

		FilesUtil.copy(Paths.get("test-resources","projects","tasks-plugins-sdk"), pluginsSdkDir);

		assertTrue(Files.exists(pluginsSdkDir.resolve(Paths.get("portlets","tasks-portlet"))));

		String[] convertArgs = {"-b", testdir.toString(), "convert", "tasks-portlet", "foo"};

		new bladenofail().run(convertArgs);

		assertTrue(Files.exists(testdir.resolve(Paths.get("modules","foo","foo-api","build.gradle"))));
		
	}

	@Test
	public void testConvertServiceBuilder() throws Exception {
		Path testdir = Paths.get("build","testMigrateServiceBuilder");

		if (Files.exists(testdir)) {
			FileUtil.deleteDir(testdir);
			assertTrue(Files.notExists(testdir));
		}

		Util.unzip(Paths.get("test-resources","projects","plugins-sdk-with-git.zip"), testdir);

		assertTrue(Files.exists(testdir));

		Path projectDir = testdir.resolve("plugins-sdk-with-git");

		String[] args = {"-b", projectDir.toString(), "init", "-u"};

		new bladenofail().run(args);

		args = new String[] {"-b", projectDir.toString(), "convert", SB_PROJECT_NAME};

		new bladenofail().run(args);

		Path sbWar = projectDir.resolve(Paths.get("wars","sample-service-builder-portlet"));

		assertTrue(Files.exists(sbWar));

		assertTrue(Files.notExists(sbWar.resolve("build.xml")));

		assertTrue(Files.exists(sbWar.resolve("build.gradle")));

		assertTrue(Files.notExists(sbWar.resolve("docroot")));

		args = new String[] {"-b", projectDir.toString(), "convert", SB_PROJECT_NAME};

		new bladenofail().run(args);

		Path moduleDir = projectDir.resolve("modules");

		Path newSbDir = moduleDir.resolve("sample-service-builder");

		Path sbServiceDir = newSbDir.resolve("sample-service-builder-service");
		Path sbApiDir = newSbDir.resolve("sample-service-builder-api");

		assertTrue(Files.exists(sbServiceDir));
		assertTrue(Files.exists(sbApiDir));

		assertTrue(Files.exists(sbServiceDir.resolve("service.xml")));
		assertTrue(Files.exists(sbServiceDir.resolve(Paths.get("src","main","resources","service.properties"))));
		assertTrue(Files.exists(sbServiceDir.resolve(Paths.get("src","main","resources","META-INF","portlet-model-hints.xml"))));
		assertTrue(Files.exists(sbServiceDir.resolve(Paths.get("src","main","java","com","liferay","sampleservicebuilder","service","impl","FooLocalServiceImpl.java"))));
		assertTrue(Files.exists(sbServiceDir.resolve(Paths.get("src","main","java","com","liferay","sampleservicebuilder","service","impl","FooServiceImpl.java"))));
		assertTrue(Files.exists(sbServiceDir.resolve(Paths.get("src","main","java","com","liferay","sampleservicebuilder","model","impl","FooImpl.java"))));

		Path bndBnd = sbApiDir.resolve("bnd.bnd");

		assertTrue(Files.exists(bndBnd));

		String bndContent = new String(Files.readAllBytes(bndBnd));

		assertTrue(bndContent, bndContent.contains("com.liferay.sampleservicebuilder.exception"));
	}

	private final Path workspaceDir = Paths.get("build","test","workspace");

}