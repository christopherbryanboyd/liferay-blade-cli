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

package com.liferay.project.templates.sample;

import com.liferay.blade.cli.BladeTest;
import com.liferay.blade.cli.BladeTest.BladeTestBuilder;
import com.liferay.blade.cli.util.BladeUtil;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class TemplatesTest {

	@Before
	public void setUp() throws Exception {
		File homeDir = _getHomeDir();

		File settingsDir = temporaryFolder.getRoot();

		Path settingsDirPath = settingsDir.toPath();

		BladeTestBuilder bladeTestBuilder = BladeTest.builder();

		bladeTestBuilder.setSettingsDir(settingsDirPath);

		Path homeDirPath = homeDir.toPath();

		
		bladeTestBuilder.setBladeHomeDir(homeDirPath);

		_bladeTest = bladeTestBuilder.build();
	}

	@Test
	public void testProjectTemplatesBuiltIn() throws Exception {
		Map<String, String> templates = BladeUtil.getTemplates(_bladeTest);

		Assert.assertNotNull(templates);

		Assert.assertEquals(templates.toString(), _NUM_BUILTIN_TEMPLATES, templates.size());
	}

	@Test
	public void testProjectTemplatesWithCustom() throws Exception {
		_setupTestExtensions();

		Map<String, String> templates = BladeUtil.getTemplates(_bladeTest);

		Assert.assertNotNull(templates);

		Assert.assertEquals(templates.toString(), _NUM_BUILTIN_TEMPLATES + 1, templates.size());
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static void _setupTestExtension(Path extensionsPath, String jarPath) throws IOException {
		File sampleJarFile = new File(jarPath);

		Assert.assertTrue(sampleJarFile.getAbsolutePath() + " does not exist.", sampleJarFile.exists());

		Path sampleJarPath = extensionsPath.resolve(sampleJarFile.getName());

		Files.copy(sampleJarFile.toPath(), sampleJarPath, StandardCopyOption.REPLACE_EXISTING);

		Assert.assertTrue(Files.exists(sampleJarPath));
	}

	private File _getHomeDir() {
		File homeDir = new File(temporaryFolder.getRoot(), ".blade");
		

		if (!homeDir.exists()) {
			homeDir.mkdir();
		}

		return homeDir;
	}

	private void _setupTestExtensions() throws Exception {
		File homeDir = _getHomeDir();

		homeDir.mkdirs();

		Assert.assertTrue("Unable to create test extensions dir.", homeDir.exists());

		Path extensionsPath = homeDir.toPath();

		_setupTestExtension(extensionsPath, System.getProperty("sampleTemplateJarFile"));
	}

	private static final int _NUM_BUILTIN_TEMPLATES = 34;

	private BladeTest _bladeTest;

}