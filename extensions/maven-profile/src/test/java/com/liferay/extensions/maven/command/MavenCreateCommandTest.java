package com.liferay.extensions.maven.command;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.liferay.blade.cli.BladeTest;
import com.liferay.blade.cli.MavenRunnerUtil;

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Domain;
import aQute.bnd.osgi.Jar;
import aQute.lib.io.IO;

public class MavenCreateCommandTest {

	@Test
	public void testCreateActivator() throws Exception {
		File tempRoot = temporaryFolder.getRoot();
		
		String[] mavenArgs =
		{"create", "-d", tempRoot.getAbsolutePath(), "-b", "maven", "-t", "activator", "bar-activator"};

		String projectPath = new File(tempRoot, "bar-activator").getAbsolutePath();

		new BladeTest().run(mavenArgs);
		
		_checkMavenBuildFiles(projectPath);

		_contains(
			_checkFileExists(projectPath + "/src/main/java/bar/activator/BarActivator.java"),
			".*^public class BarActivator implements BundleActivator.*$");

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "bar-activator-1.0.0.jar");

		_verifyImportPackage(new File(projectPath, "target/bar-activator-1.0.0.jar"));
	}

	@Test
	public void testCreateApi() throws Exception {
		File tempRoot = temporaryFolder.getRoot();

		String[] mavenArgs = {"create", "-d", tempRoot.getAbsolutePath(), "-b", "maven", "-t", "api", "foo"};

		String projectPath = new File(tempRoot, "foo").getAbsolutePath();
		
		new BladeTest().run(mavenArgs);

		_checkMavenBuildFiles(projectPath);

		_contains(_checkFileExists(projectPath + "/src/main/java/foo/api/Foo.java"), ".*^public interface Foo.*");

		_contains(_checkFileExists(projectPath + "/src/main/resources/foo/api/packageinfo"), "version 1.0.0");

		MavenRunnerUtil.executeGoals(projectPath, new String[] {"clean", "package"});

		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		_verifyImportPackage(new File(projectPath, "target/foo-1.0.0.jar"));

		try (Jar jar = new Jar(new File(projectPath, "target/foo-1.0.0.jar"))) {
			Manifest manifest = jar.getManifest();

			Attributes mainAttributes = manifest.getMainAttributes();

			Assert.assertEquals("foo.api;version=\"1.0.0\"", mainAttributes.getValue("Export-Package"));
		}
	}

	@Test
	public void testCreateFragment() throws Exception {
		File tempRoot = temporaryFolder.getRoot();

		String[] mavenArgs = {
			"create", "-d", tempRoot.getAbsolutePath(), "-b", "maven", "-t", "fragment", "-h", "com.liferay.login.web",
			"-H", "1.0.0", "loginHook"
		};

		String projectPath = new File(tempRoot, "loginHook").getAbsolutePath();

	}
	private File _checkFileDoesNotExists(String path) {
		File file = IO.getFile(path);

		Assert.assertFalse(file.exists());

		return file;
	}

	private File _checkFileExists(String path) {
		File file = IO.getFile(path);

		Assert.assertTrue(file.exists());

		return file;
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
	private void _checkMavenBuildFiles(String projectPath) {
		_checkFileExists(projectPath);
		_checkFileExists(projectPath + "/bnd.bnd");
		_checkFileExists(projectPath + "/pom.xml");
		_checkFileExists(projectPath + "/mvnw");
		_checkFileExists(projectPath + "/mvnw.cmd");
	}
	
	private void _contains(File file, String pattern) throws Exception {
		String content = new String(IO.read(file));

		_contains(content, pattern);
	}
	
	private void _contains(String content, String regex) throws Exception {
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);

		Assert.assertTrue(pattern.matcher(content).matches());
	}
	
	private void _verifyImportPackage(File serviceJar) throws Exception {
		try (Jar jar = new Jar(serviceJar)) {
			Manifest m = jar.getManifest();

			Domain domain = Domain.domain(m);

			Parameters imports = domain.getImportPackage();

			for (String key : imports.keySet()) {
				Assert.assertFalse(key.isEmpty());
			}
		}
	}
}
