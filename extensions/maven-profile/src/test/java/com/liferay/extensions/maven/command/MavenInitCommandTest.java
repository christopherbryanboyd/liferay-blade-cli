package com.liferay.extensions.maven.command;

import java.io.File;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.liferay.blade.cli.BladeSettings;
import com.liferay.blade.cli.BladeTest;
import com.liferay.blade.cli.MavenRunnerUtil;

import aQute.lib.io.IO;

public class MavenInitCommandTest {
	@Before
	public void setUp() throws Exception {
		_workspaceDir = temporaryFolder.newFolder("build", "test", "workspace");
	}
	

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testMavenInitWithNameWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", _workspaceDir.getPath(), "init", "-f", "-b", "maven", "newproject"};

		File newproject = new File(_workspaceDir, "newproject");

		Assert.assertTrue(newproject.mkdirs());

		BladeTest bladeTest = new BladeTest();

		bladeTest.run(args);

		Assert.assertTrue(new File(newproject, "pom.xml").exists());

		Assert.assertTrue(new File(newproject, "modules").exists());

		String contents = new String(Files.readAllBytes(new File(newproject, "pom.xml").toPath()));

		Assert.assertTrue(contents, contents.contains("3.2.1"));

		File metadataFile = new File(_workspaceDir, "newproject/.blade/settings.properties");

		Assert.assertTrue(metadataFile.exists());

		BladeSettings bladeSettings = bladeTest.getSettings();

		Assert.assertEquals("maven", bladeSettings.getProfileName());
	}

	@Test
	public void testMavenInitWithNameWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", _workspaceDir.getPath(), "init", "-b", "maven", "newproject"};

		Assert.assertTrue(new File(_workspaceDir, "newproject").mkdirs());

		Assert.assertTrue(new File(_workspaceDir, "newproject/foo").createNewFile());

		new BladeTest().run(args);

		Assert.assertFalse(new File(_workspaceDir, "newproject/pom.xml").exists());
	}

	@Test
	public void testMavenInitWithNameWorkspaceNotExists() throws Exception {
		String[] args = {"--base", _workspaceDir.getPath(), "init", "-b", "maven", "newproject"};

		new BladeTest().run(args);

		Assert.assertTrue(new File(_workspaceDir, "newproject/pom.xml").exists());

		Assert.assertTrue(new File(_workspaceDir, "newproject/modules").exists());
	}

	@Test
	public void testMavenInitWorkspaceDirectoryEmpty() throws Exception {
		String[] args = {"--base", _workspaceDir.getPath(), "init", "-b", "maven"};

		new BladeTest().run(args);

		Assert.assertTrue(new File(_workspaceDir, "pom.xml").exists());

		Assert.assertTrue(new File(_workspaceDir, "modules").exists());

		Assert.assertFalse(new File(_workspaceDir, "build.gradle").exists());

		Assert.assertFalse(new File(_workspaceDir, "gradle.properties").exists());

		Assert.assertFalse(new File(_workspaceDir, "gradle-local.properties").exists());

		Assert.assertFalse(new File(_workspaceDir, "settings.gradle").exists());

		_verifyMavenBuild();
	}

	@Test
	public void testMavenInitWorkspaceDirectoryHasFiles() throws Exception {
		String[] args = {"--base", _workspaceDir.getPath(), "init", "-b", "maven"};

		Assert.assertTrue(new File(_workspaceDir, "foo").createNewFile());

		new BladeTest().run(args);

		Assert.assertFalse(new File(_workspaceDir, "pom.xml").exists());
	}

	@Test
	public void testMavenInitWorkspaceDirectoryHasFilesForce() throws Exception {
		String[] args = {"--base", _workspaceDir.getPath(), "init", "-f", "-b", "maven"};

		new BladeTest().run(args);

		Assert.assertTrue(_workspaceDir.exists());

		Assert.assertTrue(new File(_workspaceDir, "pom.xml").exists());

		Assert.assertTrue(new File(_workspaceDir, "modules").exists());

		Assert.assertFalse(new File(_workspaceDir, "build.gradle").exists());

		Assert.assertFalse(new File(_workspaceDir, "gradle.properties").exists());

		Assert.assertFalse(new File(_workspaceDir, "gradle-local.properties").exists());

		Assert.assertFalse(new File(_workspaceDir, "settings.gradle").exists());

		_verifyMavenBuild();
	}

	private void _verifyMavenBuild() throws Exception {
		_createMavenBundle();

		String projectPath = _workspaceDir.getPath() + "/modules/foo";

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");
	}
	
	private void _createMavenBundle() throws Exception {
		String projectPath = new File(_workspaceDir, "modules").getAbsolutePath();

		String[] args = {"create", "-t", "mvc-portlet", "-d", projectPath, "-b", "maven", "foo"};

		new BladeTest().run(args);

		File file = IO.getFile(projectPath + "/foo");
		File bndFile = IO.getFile(projectPath + "/foo/bnd.bnd");

		Assert.assertTrue(file.exists());

		Assert.assertTrue(bndFile.exists());
	}

	private File _workspaceDir = null;
}
