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

package com.liferay.blade.extensions.maven.profile;

import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.liferay.blade.cli.TestUtil;

/**
 * @author Christopher Bryan Boyd
 */
public class DeployCommandMavenTest {

	@Test
	public void testInstallJar() throws Exception {
		
		File extensionsDir = extensionsTemporaryFolder.newFolder();
		
		File workspaceDir = temporaryFolder.newFolder();

		String[] args = {"init", "--base", workspaceDir.getPath(), "-b", "maven"};

		TestUtil.runBlade(extensionsDir, args);

		args = new String[] {"server", "init", "--base", workspaceDir.getPath()};

		TestUtil.runBlade(extensionsDir, args);

		File bundlesDirectory = new File(workspaceDir, "bundles");

		Assert.assertTrue(bundlesDirectory.exists());

		File osgiDirectory = new File(bundlesDirectory, "osgi");

		Assert.assertTrue(osgiDirectory.exists());

		File osgiModulesDirectory = new File(osgiDirectory, "modules");

		File modulesDirectory = new File(workspaceDir, "modules");

		Assert.assertTrue(modulesDirectory.exists());

		args = new String[] {"--base", modulesDirectory.getAbsolutePath(), "create", "-t", "soy-portlet", "foo"};

		TestUtil.runBlade(extensionsDir, args);

		File projectDirectory = new File(modulesDirectory, "foo");

		Assert.assertTrue(projectDirectory.exists());

		args = new String[] {"--base", projectDirectory.getAbsolutePath(), "deploy"};

		TestUtil.runBlade(extensionsDir, args);

		System.out.println("test");

		int filesCount = osgiModulesDirectory.list().length;

		Assert.assertEquals(1, filesCount);
	}

	@Test
	public void testInstallWar() throws Exception {
		
		File extensionsDir = extensionsTemporaryFolder.newFolder();
		
		File workspaceDir = temporaryFolder.newFolder();

		String[] args = {"init", "--base", workspaceDir.getPath(), "-b", "maven"};

		TestUtil.runBlade(extensionsDir, args);

		args = new String[] {"server", "init", "--base", workspaceDir.getPath()};

		TestUtil.runBlade(extensionsDir, args);

		File bundlesDirectory = new File(workspaceDir, "bundles");

		Assert.assertTrue(bundlesDirectory.exists());

		File osgiDirectory = new File(bundlesDirectory, "osgi");

		Assert.assertTrue(osgiDirectory.exists());

		File osgiWarDirectory = new File(osgiDirectory, "war");

		File warsDirectory = new File(workspaceDir, "wars");

		Assert.assertTrue(warsDirectory.exists());

		args = new String[] {"--base", warsDirectory.getAbsolutePath(), "create", "-t", "war-mvc-portlet", "foo"};

		TestUtil.runBlade(extensionsDir, args);

		File projectDirectory = new File(warsDirectory, "foo");

		Assert.assertTrue(projectDirectory.exists());

		args = new String[] {"--base", projectDirectory.getAbsolutePath(), "deploy"};

		TestUtil.runBlade(extensionsDir, args);

		int filesCount = osgiWarDirectory.list().length;

		Assert.assertEquals(1, filesCount);
	}

	@Rule
	public final TemporaryFolder extensionsTemporaryFolder = new TemporaryFolder();
	
	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

}