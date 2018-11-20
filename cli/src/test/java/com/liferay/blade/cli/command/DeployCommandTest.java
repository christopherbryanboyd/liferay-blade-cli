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

package com.liferay.blade.cli.command;

import java.io.File;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.liferay.blade.cli.TestUtil;

/**
 * @author Christopher Bryan Boyd
 */
public class DeployCommandTest {
	
	@Test
	public void testInstallJar() throws Exception {
		
		File _workspaceDir = temporaryFolder.newFolder();

		String[] args = {"--base", _workspaceDir.getPath(), "init"};

		TestUtil.runBlade(args);
		
		args = new String[] {"--base", _workspaceDir.getPath(), "server", "init"};
		
		TestUtil.runBlade(args);

		File bundlesDirectory = new File(_workspaceDir.getPath(), "bundles");

		Assert.assertTrue(bundlesDirectory.exists());
		
		File osgiDirectory = new File(bundlesDirectory, "osgi");
		
		Assert.assertTrue(osgiDirectory.exists());
		
		File osgiModulesDirectory = new File(osgiDirectory, "modules");
		
		Assert.assertTrue(osgiModulesDirectory.exists());
		
		int filesCount = osgiModulesDirectory.list().length;
		
		Assert.assertEquals(filesCount, 0);
		
		File modulesDirectory = new File(_workspaceDir, "modules");
		
		Assert.assertTrue(modulesDirectory.exists());

		args = new String[] {"--base", modulesDirectory.getAbsolutePath(), "create", "-t", "soy-portlet", "foo"};

		TestUtil.runBlade(args);
		
		File projectDirectory = new File(modulesDirectory, "foo");
		
		Assert.assertTrue(projectDirectory.exists());
		
		args = new String[] {"--base", modulesDirectory.getAbsolutePath(), "deploy"};

		TestUtil.runBlade(args);
		
		filesCount = osgiModulesDirectory.list().length;
		
		Assert.assertEquals(filesCount, 1);
	}

	@Test
	public void testInstallWar() throws Exception {
		File _workspaceDir = temporaryFolder.newFolder();

		String[] args = {"--base", _workspaceDir.getPath(), "init"};

		TestUtil.runBlade(args);
		
		args = new String[] {"--base", _workspaceDir.getPath(), "server", "init"};
		
		TestUtil.runBlade(args);

		File bundlesDirectory = new File(_workspaceDir.getPath(), "bundles");

		Assert.assertTrue(bundlesDirectory.exists());
		
		File osgiDirectory = new File(bundlesDirectory, "osgi");
		
		Assert.assertTrue(osgiDirectory.exists());
		
		File osgiWarDirectory = new File(osgiDirectory, "war");
		
		Assert.assertTrue(osgiWarDirectory.exists());
		
		int filesCount = osgiWarDirectory.list().length;
		
		Assert.assertEquals(filesCount, 0);
		
		File warsDirectory = new File(_workspaceDir, "wars");
		
		Assert.assertTrue(warsDirectory.exists());
		
		args = new String[] {"--base", warsDirectory.getAbsolutePath(), "create", "-t", "war-mvc-portlet", "foo"};

		TestUtil.runBlade(args);

		File projectDirectory = new File(warsDirectory, "foo");
		
		Assert.assertTrue(projectDirectory.exists());
		
		args = new String[] {"--base", warsDirectory.getAbsolutePath(), "deploy"};

		TestUtil.runBlade(args);
		
		filesCount = osgiWarDirectory.list().length;
		
		Assert.assertEquals(filesCount, 1);
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
}