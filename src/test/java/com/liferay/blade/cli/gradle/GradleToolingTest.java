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

package com.liferay.blade.cli.gradle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.liferay.blade.cli.util.FilesUtil;
import com.liferay.project.templates.internal.util.FileUtil;

/**
 * @author Gregory Amerson
 */
public class GradleToolingTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		FilesUtil.copy(Paths.get("deps.zip"), Paths.get("build","classes","java","test","deps.zip"));
		Path testws = Paths.get("build","testws1");
		if (Files.exists(testws))
			FileUtil.deleteDir(testws);
		Files.createDirectories(testws);
		FilesUtil.copy(
			Paths.get("test-resources","projects","testws1"), testws);
	}

	@Test
	public void testGetOutputFiles() throws Exception {
		Set<Path> files = GradleTooling.getOutputFiles(
			Paths.get("build"), Paths.get("build","testws1"));

		assertNotNull(files);
		assertEquals(1, files.size());
	}

	@Test
	public void testGetPluginClassNames() throws Exception {
		Set<String> pluginClassNames = GradleTooling.getPluginClassNames(
			Paths.get("build"), Paths.get("build","testws1","modules","testportlet"));

		assertNotNull(pluginClassNames);
		assertTrue(pluginClassNames.contains("com.liferay.gradle.plugins.LiferayOSGiPlugin"));
	}

	@Test 
	public void testIsLiferayModule() throws Exception {
		boolean isModule = GradleTooling.isLiferayModule (
			Paths.get("build"), Paths.get("build","testws1","modules","testportlet"));

		assertTrue(isModule);
	}

	@Test
	public void testIsNotLiferayModule() throws Exception {
		boolean isModule = GradleTooling.isLiferayModule (
			Paths.get("build"), Paths.get("build","testws1","modules"));

		assertFalse(isModule);
	}
}
