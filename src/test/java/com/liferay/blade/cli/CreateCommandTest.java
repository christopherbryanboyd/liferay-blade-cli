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

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Domain;
import aQute.bnd.osgi.Jar;
import aQute.lib.io.IO;

import com.liferay.blade.cli.util.FilesUtil;
import com.liferay.project.templates.ProjectTemplates;
import com.liferay.project.templates.internal.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gregory Amerson
 */
/*public class CreateCommandTest {
	private Path testdir = Paths.get("build","test");

	@Before
	public void setUp() throws Exception {
		Files.createDirectories(testdir);

		Path afile = testdir.resolve("afile");
		
		if (Files.notExists(afile))
		Files.createFile(afile);
	}

	@After
	public void cleanUp() throws Exception {
		ConnectorServices.reset();
		File testdir = this.testdir.toFile();
		if (testdir.exists()) {
			IO.delete(testdir);
			assertFalse(testdir.exists());
		}
	}

	@Test
	public void testCreateActivator() throws Exception {

		Path testPath = Paths.get("build", "test");
		
		String[] gradleArgs = {
			"create", "-d", testPath.toString(), "-t", "activator",
			"bar-activator"
		};

		String[] mavenArgs = {
			"create", "-d", testPath.toString(), "-b", "maven", "-t", "activator",
			"bar-activator"
		};

		Path projectPath = testPath.resolve("bar-activator");

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		Path implPath = projectPath.resolve(Paths.get("src","main","java","bar","activator","BarActivator.java"));
		
		contains(assertPathExists(implPath),
			".*^public class BarActivator implements BundleActivator.*$");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "bar.activator-1.0.0.jar");
		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","bar.activator-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			assertPathExists(
				projectPath.resolve(Paths.get("src","main","java","bar","activator","BarActivator.java"))),
			".*^public class BarActivator implements BundleActivator.*$");

		MavenRunnerUtil.executeMavenPackage(projectPath, new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(projectPath, "bar-activator-1.0.0.jar");
		verifyImportPackage(projectPath.resolve(Paths.get("target","bar-activator-1.0.0.jar")));
	}

	@Test
	public void testCreateApi() throws Exception {

		Path testPath = Paths.get("build", "test");
		
		String[] gradleArgs = {
			"create", "-d", testPath.toString(), "-t", "api", "foo"
		};

		String[] mavenArgs = {
			"create", "-d", testPath.toString(), "-b", "maven", "-t", "api", "foo"
		};

		Path projectPath = testPath.resolve("foo");

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
				assertPathExists(
				projectPath.resolve(Paths.get("src","main","java","foo","api","Foo.java"))),
				".*^public interface Foo.*");

		contains(
			assertPathExists(
				projectPath.resolve(Paths.get("src","main","resources","foo","api","packageinfo"))),
				"version 1.0.0");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		try (Jar jar = new Jar(projectPath.resolve(Paths.get("build","libs","foo-1.0.0.jar")).toFile())) {
			assertEquals(
				"foo.api;version=\"1.0.0\"",
				jar.getManifest().getMainAttributes().getValue("Export-Package"));
		}

		FileUtil.deleteDir(projectPath);

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			assertPathExists(
				projectPath.resolve(Paths.get("src","main","java","foo","api","Foo.java"))),
				".*^public interface Foo.*");

		contains(
				assertPathExists(
						projectPath.resolve(Paths.get("src", "main","resources","foo","api","packageinfo"))),
				"version 1.0.0");

		MavenRunnerUtil.executeMavenPackage(projectPath, new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");
		verifyImportPackage(projectPath.resolve(Paths.get("target","foo-1.0.0.jar")));

		try (Jar jar = new Jar(projectPath.resolve(Paths.get("target","foo-1.0.0.jar")).toFile())) {
			assertEquals(
				"foo.api;version=\"1.0.0\"",
				jar.getManifest().getMainAttributes().getValue("Export-Package"));
		}
	}

	@Test
	public void testCreateFragment() throws Exception {

		Path testPath = Paths.get("build", "test");
		
		String[] gradleArgs = {
			"create", "-d", testPath.toString(), "-t", "fragment", "-h",
			"com.liferay.login.web", "-H", "1.0.0", "loginHook"
		};

		String[] mavenArgs = {
			"create", "-d", testPath.toString(), "-b", "maven", "-t", "fragment", "-h",
			"com.liferay.login.web", "-H", "1.0.0", "loginHook"
		};

		Path projectPath = testPath.resolve("loginHook");

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
				assertPathExists(projectPath.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		contains(
			assertPathExists(projectPath.resolve("build.gradle")),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "loginhook-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","loginhook-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			assertPathExists(projectPath.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		MavenRunnerUtil.executeMavenPackage(projectPath, new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(projectPath, "loginHook-1.0.0.jar");
		verifyImportPackage(projectPath.resolve(Paths.get("target","loginHook-1.0.0.jar")));
	}

	@Test
	public void testCreateMVCPortlet() throws Exception {
		Path testPath = Paths.get("build", "test");
		
		String[] gradleArgs = {
			"create", "-d", testPath.toString(), "-t", "mvc-portlet", "foo"
		};

		String[] mavenArgs = {
			"create", "-d", testPath.toString(), "-b", "maven", "-t",
			"mvc-portlet", "foo"
		};

		Path projectPath = testPath.resolve("foo");

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			assertPathExists(projectPath.resolve(Paths.get("src","main","java","foo","portlet","FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(
				assertPathExists(projectPath.resolve("build.gradle")),
			".*^apply plugin: \"com.liferay.plugin\".*");

		Path resourcesPath = projectPath.resolve(Paths.get("src","main","resources","META-INF","resources"));
		
		assertPathExists(resourcesPath.resolve("view.jsp"));

		assertPathExists(resourcesPath.resolve("init.jsp"));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","foo-1.0.0.jar")));

		FileUtil.deleteDir(projectPath);

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
				assertPathExists(projectPath.resolve(Paths.get("src","main","java","foo","portlet","FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		assertPathExists(resourcesPath.resolve("view.jsp"));

		assertPathExists(resourcesPath.resolve("init.jsp"));

		MavenRunnerUtil.executeMavenPackage(projectPath, new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(projectPath, "foo-1.0.0.jar");
		verifyImportPackage(projectPath.resolve(Paths.get("target","foo-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleMVCPortletProjectWithPackage()
		throws Exception {

		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "mvc-portlet", "-p",
			"com.liferay.test", "foo"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("foo");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		contains(
				assertPathExists(
				projectPath.resolve(Paths.get("src","main","java","com","liferay","test","portlet","FooPortlet.java"))),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(
				assertPathExists(projectPath.resolve("build.gradle")),
			".*^apply plugin: \"com.liferay.plugin\".*");
		
		Path resourcesDir = projectPath.resolve(Paths.get("src","main","resources","META-INF","resources"));

		assertPathExists(
				resourcesDir.resolve("view.jsp"));

		assertPathExists(
				resourcesDir.resolve("init.jsp"));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "com.liferay.test-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","com.liferay.test-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleMVCPortletProjectWithPortletSuffix() throws Exception {
		Path testPath = Paths.get("build", "test");
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "mvc-portlet", "portlet-portlet"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("portlet-portlet");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		contains(
			assertPathExists(projectPath.resolve(Paths.get("src","main","java","portlet","portlet","portlet","PortletPortlet.java"))),
			".*^public class PortletPortlet extends MVCPortlet.*$");

		contains(
				assertPathExists(projectPath.resolve("build.gradle")),
			".*^apply plugin: \"com.liferay.plugin\".*");

		Path resourcesDir = projectPath.resolve(Paths.get("src","main","resources","META-INF","resources"));

		assertPathExists(
				resourcesDir.resolve("view.jsp"));

		assertPathExists(
				resourcesDir.resolve("init.jsp"));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "portlet.portlet-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","portlet.portlet-1.0.0.jar")));
	}

	@Test
	public void testCreateGradlePortletProject() throws Exception {
		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "portlet", "-c", "Foo",
			"gradle.test"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("gradle.test");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("build.gradle"));

		contains(
				assertPathExists(
				projectPath.resolve(Paths.get("src","main","java","gradle","test","portlet","FooPortlet.java"))),
			new String[] {
				"^package gradle.test.portlet;.*",
				".*javax.portlet.display-name=gradle.test.*",
				".*^public class FooPortlet .*",
				".*printWriter.print\\(\\\"gradle.test Portlet.*"
			});

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "gradle.test-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","gradle.test-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleServiceBuilderDashes() throws Exception {
		Path testPath = Paths.get("build", "test");
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "service-builder", "-p",
			"com.liferay.backend.integration", "backend-integration"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("backend-integration");

		contains(
				assertPathExists(projectPath.resolve("settings.gradle")),
			"include \"backend-integration-api\", " +
			"\"backend-integration-service\"");

		contains(
				assertPathExists(projectPath.resolve(Paths.get("backend-integration-api","bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*",
				".*com.liferay.backend.integration.exception,\\\\.*",
				".*com.liferay.backend.integration.model,\\\\.*",
				".*com.liferay.backend.integration.service,\\\\.*",
				".*com.liferay.backend.integration.service.persistence.*"
			});

		contains(
			assertPathExists(
				projectPath.resolve(Paths.get("backend-integration-service","bnd.bnd"))),
				".*Liferay-Service: true.*");

		BuildTask buildServiceTask = GradleRunnerUtil.executeGradleRunner(projectPath, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildServiceTask);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("backend-integration-api"),
				"com.liferay.backend.integration.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("backend-integration-service"),
				"com.liferay.backend.integration.service-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("backend-integration-service","build","libs","com.liferay.backend.integration.service-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleServiceBuilderDefault() throws Exception {

		Path testPath = Paths.get("build", "test");
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "service-builder", "-p",
			"com.liferay.docs.guestbook", "guestbook"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("guestbook");

		contains(
			assertPathExists(projectPath.resolve("settings.gradle")),
			"include \"guestbook-api\", \"guestbook-service\"");

		contains(
				assertPathExists(projectPath.resolve(Paths.get("guestbook-api","bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*",
				".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*",
				".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
				assertPathExists(projectPath.resolve(Paths.get("guestbook-service","bnd.bnd"))),
				".*Liferay-Service: true.*");

		contains(
				assertPathExists(projectPath.resolve(Paths.get("guestbook-service","build.gradle"))),
				".*compileOnly project\\(\":guestbook-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(projectPath, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("guestbook-api"), "com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("guestbook-service"), "com.liferay.docs.guestbook.service-1.0.0.jar");

		Path serviceJar = projectPath.resolve(Paths.get("guestbook-service","build","libs","com.liferay.docs.guestbook.service-1.0.0.jar"));

		verifyImportPackage(serviceJar);

		try(JarFile serviceJarFile = new JarFile(serviceJar.toFile())) {
			String springContext = serviceJarFile.getManifest().getMainAttributes().getValue("Liferay-Spring-Context");

			assertTrue(springContext.equals("META-INF/spring"));
		}
	}

	@Test
	public void testCreateGradleServiceBuilderDots() throws Exception {
		
		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "service-builder", "-p",
			"com.liferay.docs.guestbook", "com.liferay.docs.guestbook"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("com.liferay.docs.guestbook");

		contains(
			assertPathExists(projectPath.resolve("settings.gradle")),
			"include \"com.liferay.docs.guestbook-api\", " +
			"\"com.liferay.docs.guestbook-service\"");

		contains(
				assertPathExists(
				projectPath.resolve(Paths.get("com.liferay.docs.guestbook-api","bnd.bnd"))),
			new String[] {
				".*Export-Package:\\\\.*",
				".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*",
				".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
				assertPathExists(
				projectPath.resolve(Paths.get("com.liferay.docs.guestbook-service","bnd.bnd"))),
				".*Liferay-Service: true.*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(projectPath, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("com.liferay.docs.guestbook-api"),
				"com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("com.liferay.docs.guestbook-service"),
				"com.liferay.docs.guestbook.service-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("com.liferay.docs.guestbook-service","build","libs","com.liferay.docs.guestbook.service-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleService() throws Exception {
		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "service", "-s",
			"com.liferay.portal.kernel.events.LifecycleAction", "-c",
			"FooAction", "servicepreaction"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("servicepreaction");

		assertPathExists(projectPath.resolve("build.gradle"));

		Path file = projectPath.resolve(Paths.get("src","main","java","servicepreaction","FooAction.java"));

		contains(
				assertPathExists(file),
			new String[] {
				"^package servicepreaction;.*",
				".*^import com.liferay.portal.kernel.events.LifecycleAction;$.*",
				".*service = LifecycleAction.class.*",
				".*^public class FooAction implements LifecycleAction \\{.*"
			});

		List<String> lines = new ArrayList<String>();
	

		Files.lines(file).forEach(line ->
		{
			lines.add(line);
			if (line.equals("import com.liferay.portal.kernel.events.LifecycleAction;")) {
				lines.add("import com.liferay.portal.kernel.events.LifecycleEvent;");
				lines.add("import com.liferay.portal.kernel.events.ActionException;");
			}

			if (line.equals("public class FooAction implements LifecycleAction {")) {
				String s = new StringBuilder()
				           .append("@Override\n")
				           .append("public void processLifecycleEvent(LifecycleEvent lifecycleEvent)\n")
				           .append("throws ActionException {\n")
				           .append("System.out.println(\"login.event.pre=\" + lifecycleEvent);\n")
				           .append("}\n")
				           .toString();
				lines.add(s);
			}
		});
	

		try(Writer writer = Files.newBufferedWriter(file)) {
			for (String string : lines) {
				writer.write(string + "\n");
			}
		}

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "servicepreaction-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","servicepreaction-1.0.0.jar")));
	}

	@Test
	public void testCreateGradleServiceWrapper() throws Exception {

		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "service-wrapper", "-s",
			"com.liferay.portal.kernel.service.UserLocalServiceWrapper",
			"serviceoverride"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("serviceoverride");

		assertPathExists(projectPath.resolve("build.gradle"));

		contains(
			assertPathExists(
				projectPath.resolve(Paths.get("src", "main","java","serviceoverride","Serviceoverride.java"))),
			new String[] {
				"^package serviceoverride;.*",
				".*^import com.liferay.portal.kernel.service.UserLocalServiceWrapper;$.*",
				".*service = ServiceWrapper.class.*",
				".*^public class Serviceoverride extends UserLocalServiceWrapper \\{.*",
				".*public Serviceoverride\\(\\) \\{.*"
			});

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "serviceoverride-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","serviceoverride-1.0.0.jar")));
	}

	@Test
	public void testCreateOnExistFolder() throws Exception {
		String[] args = {
			"create", "-d", "build", "-t", "activator", "exist"
		};

		Path projectPath = Paths.get("build","exist");
		Path existFile = projectPath.resolve("file.txt");

		if(Files.notExists(existFile)) {
			Files.createDirectories(projectPath);
			Files.createFile(existFile);
			assertPathExists(existFile);
		}

		new bladenofail().run(args);


		assertPathDoesNotExist(projectPath.resolve("bnd.bnd"));
	}

	@Test
	public void testCreateGradleSymbolicName() throws Exception {

		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-p", "foo.bar", "barfoo"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("barfoo");

		assertPathExists(projectPath.resolve("build.gradle"));

		contains(
			assertPathExists(projectPath.resolve("bnd.bnd")),
			".*Bundle-SymbolicName: foo.bar.*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "foo.bar-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","foo.bar-1.0.0.jar")));
	}

	@Test
	public void testCreateNpmAngular() throws Exception {
		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "npm-angular-portlet", "npmangular"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("npmangular");

		assertPathExists(projectPath.resolve("build.gradle"));

		Path jsp = assertPathExists(projectPath.resolve(Paths.get("src","main","resources","META-INF","resources","view.jsp")));

		contains(jsp, ".*<aui:script require=\"npmangular@1.0.0\">.*");

		contains(jsp, ".*npmangular100.default.*");
	}

	@Test
	public void testCreatePortletConfigurationIcon() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "portlet-configuration-icon", "-p", "blade.test", "icontest"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("icontest");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		Path componentFile = assertPathExists(
			projectPath.resolve(Paths.get("src","main","java","blade","test","portlet","configuration","icon",
				"IcontestPortletConfigurationIcon.java")));

		contains(
			componentFile,
			".*^public class IcontestPortletConfigurationIcon.*extends BasePortletConfigurationIcon.*$");

		Path gradleBuildFile = assertPathExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreatePortletToolbarContributor() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "portlet-toolbar-contributor", "-p", "blade.test",  "toolbartest"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("toolbartest");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		Path componentFile = assertPathExists(
			projectPath.resolve(Paths.get("src","main","java","blade","test","portlet","toolbar","contributor",
				"ToolbartestPortletToolbarContributor.java")));

		contains(
			componentFile,
			".*^public class ToolbartestPortletToolbarContributor.*implements PortletToolbarContributor.*$");

		Path gradleBuildFile = assertPathExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreateProjectAllDefaults() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "hello-world-portlet"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("hello-world-portlet");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		contains(
			assertPathExists(
					projectPath.resolve(Paths.get("src","main","java","hello","world","portlet","portlet",
						"HelloWorldPortlet.java"))),
			".*^public class HelloWorldPortlet extends MVCPortlet.*$");

		contains(assertPathExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		Path resourcesDir = projectPath.resolve(Paths.get("src","main","resources","META-INF","resources"));
		
		assertPathExists(resourcesDir.resolve("view.jsp"));

		assertPathExists(resourcesDir.resolve("init.jsp"));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "hello.world.portlet-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","hello.world.portlet-1.0.0.jar")));
	}

	@Test
	public void testCreateProjectWithRefresh() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
				"create", "-d", testPath.toString(), "hello-world-refresh"
			};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("hello-world-refresh");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		contains(
				assertPathExists(
						projectPath.resolve(Paths.get("src","main","java","hello","world","refresh","portlet",
							"HelloWorldRefreshPortlet.java"))),
			".*^public class HelloWorldRefreshPortlet extends MVCPortlet.*$");

		contains(assertPathExists(projectPath.resolve("build.gradle")), ".*^apply plugin: \"com.liferay.plugin\".*");

		Path resourcesPath = projectPath.resolve(Paths.get("src","main","resources","META-INF","resources"));
		
		assertPathExists(
				resourcesPath.resolve("view.jsp"));

		assertPathExists(
				resourcesPath.resolve("init.jsp"));

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(projectPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "hello.world.refresh-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","hello.world.refresh-1.0.0.jar")));
	}

	@Test
	public void testCreateSimulationPanelEntry() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "simulation-panel-entry", "-p", "test.simulator", "simulator"
		};

		new bladenofail().run(args);

		Path projectPath =  testPath.resolve("simulator");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		Path componentFile = assertPathExists(
			projectPath.resolve(Paths.get("src","main","java","test","simulator","application","list",
				"SimulatorSimulationPanelApp.java")));

		contains(
			componentFile,
			".*^public class SimulatorSimulationPanelApp.*extends BaseJSPPanelApp.*$");

		Path gradleBuildFile = assertPathExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "test.simulator-1.0.0.jar");
	}

	@Test
	public void testCreateSpringMvcPortlet() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "spring-mvc-portlet", "-p", "test.spring.portlet", "spring-test"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("spring-test");

		assertPathExists(projectPath);

		assertPathExists(
			projectPath.resolve(Paths.get("src","main","java","test","spring","portlet","portlet","" +
				"SpringTestPortletViewController.java")));

		assertPathExists(projectPath.resolve("build.gradle"));

		verifyBuild(projectPath, projectPath, "spring-test.war");
	}

	@Test
	public void testCreateTemplateContextContributor() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "template-context-contributor", "blade-test"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("blade-test");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		Path componentFile = assertPathExists(
			projectPath.resolve(Paths.get("src","main","java","blade","test","context","contributor","BladeTestTemplateContextContributor.java")));

		contains(
			componentFile,
			".*^public class BladeTestTemplateContextContributor.*implements TemplateContextContributor.*$");

		Path gradleBuildFile = assertPathExists(projectPath.resolve("build.gradle"));

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreateTheme() throws Exception {

		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "theme", "theme-test"
		};

		new bladenofail().run(args);
		
		Path projectPath = testPath.resolve("theme-test");

		assertPathExists(projectPath);

		assertPathDoesNotExist(projectPath.resolve("bnd.bnd"));

		assertPathExists(projectPath.resolve(Paths.get("src","main","webapp","css","_custom.scss")));

		Path properties = assertPathExists(
			projectPath.resolve(Paths.get("src","main","webapp","WEB-INF","liferay-plugin-package.properties")));

		contains(properties, ".*^name=theme-test.*");

		Path buildFile = projectPath.resolve("build.gradle");

		String dataStr = System.lineSeparator() + "buildTheme { jvmArgs " + '"' + "-Djava.awt.headless=true" + '"' + " }";

		Files.write(buildFile, dataStr.getBytes(), StandardOpenOption.APPEND);

		verifyBuild(projectPath,projectPath, "theme-test.war");
	}

	@Test
	public void testCreateThemeContributor() throws Exception {
		Path testPath = Paths.get("build", "test"); 
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "theme-contributor", "-C", "foobar",
			"theme-contributor-test"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("theme-contributor-test");

		assertPathExists(projectPath);

		Path bnd = assertPathExists(projectPath.resolve("bnd.bnd"));

		contains(bnd, ".*Liferay-Theme-Contributor-Type: foobar.*");

		verifyBuild(projectPath, projectPath, "theme.contributor.test-1.0.0.jar");
	}

	@Test
	public void testCreateWorkspaceGradleFragment() throws Exception {
		
		Path workspace = Paths.get("build","test","workspace");
		Path projectPath = workspace.resolve(Paths.get("modules","extensions"));
		

		String[] args = {
			"create", "-d", projectPath.toString(), "-t",
			"fragment", "-h", "com.liferay.login.web", "-H", "1.0.0", "loginHook"
		};


		makeWorkspace(workspace);

		new bladenofail().run(args);
		
		Path loginHookDir = assertPathExists(projectPath.resolve("loginHook"));

		contains(
				assertPathExists(loginHookDir.resolve("bnd.bnd")),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		Path buildGradle = assertPathExists(loginHookDir.resolve("build.gradle"));

		lacks(
			buildGradle,
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(loginHookDir, "loginhook-1.0.0.jar");

		verifyImportPackage(loginHookDir.resolve(Paths.get("build","libs","loginhook-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradlePortletProject() throws Exception {
		Path workspace = Paths.get("build","test","workspace");

		Path projectPath = workspace.resolve(Paths.get("modules","apps"));

		String[] args = {
			"create", "-d", projectPath.toString(), "-t",
			"portlet", "-c", "Foo", "gradle.test"
		};


		makeWorkspace(workspace);

		new bladenofail().run(args);

		Path gradleTestPath = assertPathExists(projectPath.resolve("gradle.test"));

		assertPathDoesNotExist(gradleTestPath.resolve("gradlew"));

		contains(
				assertPathExists(
				gradleTestPath.resolve(Paths.get("src","main","java","gradle","test","portlet","FooPortlet.java"))),
			new String[] {
				"^package gradle.test.portlet;.*",
				".*javax.portlet.display-name=gradle.test.*",
				".*^public class FooPortlet .*",
				".*printWriter.print\\(\\\"gradle.test Portlet.*"
			});

		lacks(
			assertPathExists(gradleTestPath.resolve("build.gradle")),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(gradleTestPath, "gradle.test-1.0.0.jar");

		verifyImportPackage(gradleTestPath.resolve(Paths.get("build","libs","gradle.test-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectApiPath()
		throws Exception {

		Path workspace = Paths.get("build","test","workspace");
		
		Path projectPath = workspace.resolve(Paths.get("modules","nested","path"));
		
		
		String[] args = {
			"create", "-d", projectPath.toString(),
			"-t", "service-builder", "-p", "com.liferay.sample", "sample"
		};


		makeWorkspace(workspace);

		Files.createDirectories(projectPath);

		new bladenofail().run(args);
		
		Path samplePath = projectPath.resolve("sample");

		assertPathExists(samplePath.resolve("build.gradle"));

		assertPathDoesNotExist(samplePath.resolve("settings.gradle"));

		assertPathExists(samplePath.resolve(Paths.get("sample-api","build.gradle")));

		assertPathExists(samplePath.resolve(Paths.get("sample-service","build.gradle")));

		contains(
				samplePath.resolve(Paths.get("sample-service","build.gradle")),
				".*compileOnly project\\(\":modules:nested:path:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(samplePath.resolve("sample-api"), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(samplePath.resolve("sample-service"), "com.liferay.sample.service-1.0.0.jar");

		verifyImportPackage(samplePath.resolve(Paths.get("sample-service","build","libs","com.liferay.sample.service-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDashes()
		throws Exception {

		Path workspace = Paths.get("build","test","workspace");
		
		Path projectPath = workspace.resolve("modules");
		String[] args = {
			"create", "-d", projectPath.toString(), "-t",
			"service-builder", "-p", "com.sample", "workspace-sample"
		};


		makeWorkspace(workspace);

		new bladenofail().run(args);
		
		Path workspaceSample = projectPath.resolve("workspace-sample");

		assertPathExists(workspaceSample.resolve("build.gradle"));

		assertPathDoesNotExist(
				workspaceSample.resolve("settings.gradle"));

		assertPathExists(workspaceSample.resolve(Paths.get("workspace-sample-api","build.gradle")));

		assertPathExists(workspaceSample.resolve(Paths.get("workspace-sample-service","build.gradle")));

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(workspaceSample.resolve("workspace-sample-api"),
				"com.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(workspaceSample.resolve("workspace-sample-service"),
				"com.sample.service-1.0.0.jar");

		verifyImportPackage(workspaceSample.resolve(Paths.get("workspace-sample-service","build","libs","com.sample.service-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDefault()
		throws Exception {

		Path workspace = Paths.get("build","test","workspace");
		
		Path projectPath = workspace.resolve("modules");
		
		String[] args = {
			"create", "-d", projectPath.toString(), "-t",
			"service-builder", "-p", "com.liferay.sample", "sample"
		};


		makeWorkspace(workspace);

		new bladenofail().run(args);

		Path samplePath = projectPath.resolve("sample");

		assertPathExists(samplePath.resolve("build.gradle"));

		assertPathDoesNotExist(samplePath.resolve("settings.gradle"));

		assertPathExists(samplePath.resolve(Paths.get("sample-api","build.gradle")));

		

		contains(
				assertPathExists(samplePath.resolve(Paths.get("sample-service","build.gradle"))),
				".*compileOnly project\\(\":modules:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(samplePath.resolve("sample-api"), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(samplePath.resolve("sample-service"), "com.liferay.sample.service-1.0.0.jar");

		Path serviceJar = samplePath.resolve(Paths.get("sample-service","build","libs","com.liferay.sample.service-1.0.0.jar"));

		verifyImportPackage(serviceJar);

		try (JarFile serviceJarFile = new JarFile(serviceJar.toFile())) {
			String springContext = serviceJarFile.getManifest().getMainAttributes().getValue("Liferay-Spring-Context");

			assertTrue(springContext.equals("META-INF/spring"));
		}
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDots()
		throws Exception {

		Path workspace = Paths.get("build","test","workspace");
		
		Path projectPath = workspace.resolve("modules");

		String[] args = {
			"create", "-d", projectPath.toString(), "-t",
			"service-builder", "-p", "com.sample", "workspace.sample"
		};


		makeWorkspace(workspace);

		new bladenofail().run(args);
		
		Path samplePath = assertPathExists(projectPath.resolve("workspace.sample"));

		assertPathExists(samplePath.resolve("build.gradle"));

		assertPathDoesNotExist(
				samplePath.resolve("settings.gradle"));

		assertPathExists(
				samplePath.resolve(Paths.get("workspace.sample-api","build.gradle")));

		assertPathExists(
				samplePath.resolve(Paths.get("workspace.sample-service","build.gradle")));

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace, "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(samplePath.resolve("workspace.sample-api"),
				"com.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(samplePath.resolve("workspace.sample-service"),
				"com.sample.service-1.0.0.jar");

		verifyImportPackage(samplePath.resolve(Paths.get("workspace.sample-service","build","libs","com.sample.service-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceModuleLocation() throws Exception {
		
		Path workspace = Paths.get("build","test","workspace");
		
		String[] args = {"-b", workspace.toString(), "create", "foo"};

		makeWorkspace(workspace);

		new bladenofail().run(args);

		Path projectPath = workspace.resolve("modules");

		Path fooPath = assertPathExists(projectPath.resolve("foo"));

		assertPathExists(fooPath.resolve("bnd.bnd"));

		Path portletFile = assertPathExists(
				fooPath.resolve(Paths.get("src","main","java","foo","portlet","FooPortlet.java")));

		contains(
			portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		Path gradleBuildFile = assertPathExists(
				fooPath.resolve("build.gradle"));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(fooPath, "foo-1.0.0.jar");

		verifyImportPackage(fooPath.resolve(Paths.get("build","libs","foo-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceProjectAllDefaults() throws Exception {

		Path projectPath = Paths.get("build","test","workspace","modules","apps");

		String[] args = {
			"create", "-d", projectPath.toString(), "foo"
		};

		Path workspace = Paths.get("build","test","workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);


		assertPathExists(projectPath.resolve("foo"));

		assertPathExists(projectPath.resolve(Paths.get("foo", "bnd.bnd")));

		Path portletFile = assertPathExists(
			projectPath.resolve(Paths.get("foo","src","main","java","foo","portlet","FooPortlet.java")));

		contains(
			portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		Path gradleBuildFile = assertPathExists(
			projectPath.resolve(Paths.get("foo","build.gradle")));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath.resolve("foo"), "foo-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("foo","build","libs","foo-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceProjectWithRefresh() throws Exception {
		
		Path workspace = Paths.get("build","test","workspace");
		
		Path apps = workspace.resolve(Paths.get("modules","apps"));

		String[] args = {
			"create", "-d", apps.toString(),
			"foo-refresh"
		};


		makeWorkspace(workspace);

		new bladenofail().run(args);

		Path projectPath = apps.resolve("foo-refresh");

		assertPathExists(projectPath);

		assertPathExists(projectPath.resolve("bnd.bnd"));

		Path portletFile = assertPathExists(
				projectPath.resolve(Paths.get(
				"src","main","java","foo","refresh","portlet","FooRefreshPortlet.java")));

		contains(
			portletFile,
			".*^public class FooRefreshPortlet extends MVCPortlet.*$");

		Path gradleBuildFile = assertPathExists(
			projectPath.resolve("build.gradle"));

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "foo.refresh-1.0.0.jar");

		verifyImportPackage(projectPath.resolve(Paths.get("build","libs","foo.refresh-1.0.0.jar")));
	}

	@Test
	public void testCreateWorkspaceThemeLocation() throws Exception {
		Path workspace = Paths.get("build","test","workspace");

		String[] args = {
				"-b", workspace.toString(), "create", "-t", "theme",
				"theme-test"};

		makeWorkspace(workspace);

		new bladenofail().run(args);

		Path projectPath = Paths.get("build","test","workspace","wars","theme-test");

		assertPathExists(projectPath);

		assertPathDoesNotExist(projectPath.resolve("bnd.bnd"));

		assertPathExists(projectPath.resolve(Paths.get("src","main","webapp","css","_custom.scss")));

		Path properties = 
			projectPath.resolve(Paths.get("src","main","webapp","WEB-INF","liferay-plugin-package.properties"));

		assertPathExists(properties);
		
		contains(properties, ".*^name=theme-test.*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace, "war");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, "theme-test.war");
	}

	@Test
	public void testListTemplates() throws Exception {
		String[] args = {"create", "-l"};

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(output);

		blade blade = new bladenofail(ps);

		blade.run(args);

		String templateList = new String(output.toByteArray());

		List<String> templateNames =
			new ArrayList<>(ProjectTemplates.getTemplates().keySet());

		assertTrue(templateNames.stream().allMatch(templateList::contains));
	}

	@Test
	public void testWrongTemplateTyping() throws Exception {
		
		Path testPath = Paths.get("build", "test");
		
		String[] args = {
			"create", "-d", testPath.toString(), "-t", "activatorXXX", "wrong-activator"
		};

		new bladenofail().run(args);

		Path projectPath = testPath.resolve("wrong-activator");

		assertPathDoesNotExist(projectPath);
	}
	
	private static Path assertPathExists(Path path) {
		assertTrue(Files.exists(path));
		return path;
	}
	
	private static Path assertPathDoesNotExist(Path path) {
		assertTrue(Files.notExists(path));
		return path;
	}

	private static void checkGradleBuildFiles(Path path) {
		assertPathExists(path);
		assertPathExists(path.resolve("bnd.bnd"));
		assertPathExists(path.resolve("build.gradle"));
		assertPathExists(path.resolve("gradlew"));
		assertPathExists(path.resolve("gradlew.bat"));
	}

	private static void checkMavenBuildFiles(Path path) {
		assertPathExists(path);
		assertPathExists(path.resolve("bnd.bnd"));
		assertPathExists(path.resolve("pom.xml"));
		assertPathExists(path.resolve("mvnw"));
		assertPathExists(path.resolve("mvnw.cmd"));
	}


	private void contains(Path file, String pattern) throws Exception {
		String content = new String(Files.readAllBytes(file));

		contains(content, pattern);
	}

	private void contains(Path file, String[] patterns) throws Exception {
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

	private void lacks(Path file, String pattern) throws Exception {
		String content = new String(Files.readAllBytes(file));

		assertFalse(
			Pattern.compile(
				pattern,
				Pattern.MULTILINE | Pattern.DOTALL).matcher(content).matches());
	}

	private void makeWorkspace(Path workspace) throws Exception {
		String[] args = {"-b", workspace.getParent().toString(), "init", workspace.getFileName().toString()};

		new bladenofail().run(args);

		assertTrue(Util.isWorkspace(workspace));
	}

	private void verifyBuild(Path runnerPath, Path projectPath, String outputFileName) {
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(runnerPath, "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(projectPath, outputFileName);
	}

	private void verifyImportPackage(Path serviceJar) throws Exception {
		try (Jar jar = new Jar(serviceJar.toFile())) {
			Manifest m = jar.getManifest();
			Domain domain = Domain.domain(m);
			Parameters imports = domain.getImportPackage();

			for (String key : imports.keySet()) {
				assertFalse(key.isEmpty());
			}
		}
	}
}*/

public class CreateCommandTest {
	private File testdir = IO.getFile("build/test");

	@Before
	public void setUp() throws Exception {
		testdir.mkdirs();

		assertTrue(new File(testdir, "afile").createNewFile());
	}

	@After
	public void cleanUp() throws Exception {
		ConnectorServices.reset();

		if (testdir.exists()) {
			IO.delete(testdir);
			assertFalse(testdir.exists());
		}
	}

	@Test
	public void testCreateActivator() throws Exception {
		String[] gradleArgs = {
			"create", "-d", "build/test", "-t", "activator",
			"bar-activator"
		};

		String[] mavenArgs = {
			"create", "-d", "build/test", "-b", "maven", "-t", "activator",
			"bar-activator"
		};

		String projectPath = "build/test/bar-activator";

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(
				projectPath + "/src/main/java/bar/activator/BarActivator.java"),
			".*^public class BarActivator implements BundleActivator.*$");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "bar.activator-1.0.0.jar");
		verifyImportPackage(new File(projectPath + "/build/libs/bar.activator-1.0.0.jar"));

		FileUtils.deleteDirectory(IO.getFile(projectPath));

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(
				projectPath + "/src/main/java/bar/activator/BarActivator.java"),
			".*^public class BarActivator implements BundleActivator.*$");

		MavenRunnerUtil.executeMavenPackage(Paths.get(projectPath), new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "bar-activator-1.0.0.jar");
		verifyImportPackage(new File(projectPath + "/target/bar-activator-1.0.0.jar"));
	}

	@Test
	public void testCreateApi() throws Exception {
		String[] gradleArgs = {
			"create", "-d", "build/test", "-t", "api", "foo"
		};

		String[] mavenArgs = {
			"create", "-d", "build/test", "-b", "maven", "-t", "api", "foo"
		};

		String projectPath = "build/test/foo";

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(
				projectPath + "/src/main/java/foo/api/Foo.java"),
				".*^public interface Foo.*");

		contains(
			checkFileExists(
				projectPath + "/src/main/resources/foo/api/packageinfo"),
				"version 1.0.0");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "foo-1.0.0.jar");

		try (Jar jar = new Jar(new File(projectPath + "/build/libs/foo-1.0.0.jar"))) {
			assertEquals(
				"foo.api;version=\"1.0.0\"",
				jar.getManifest().getMainAttributes().getValue("Export-Package"));
		}

		FileUtils.deleteDirectory(IO.getFile(projectPath));

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(
				projectPath + "/src/main/java/foo/api/Foo.java"),
				".*^public interface Foo.*");

		contains(
			checkFileExists(
				projectPath + "/src/main/resources/foo/api/packageinfo"),
				"version 1.0.0");

		MavenRunnerUtil.executeMavenPackage(Paths.get(projectPath), new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "foo-1.0.0.jar");
		verifyImportPackage(new File(projectPath + "/target/foo-1.0.0.jar"));

		try (Jar jar = new Jar(new File(projectPath + "/target/foo-1.0.0.jar"))) {
			assertEquals(
				"foo.api;version=\"1.0.0\"",
				jar.getManifest().getMainAttributes().getValue("Export-Package"));
		}
	}

	@Test
	public void testCreateFragment() throws Exception {
		String[] gradleArgs = {
			"create", "-d", "build/test", "-t", "fragment", "-h",
			"com.liferay.login.web", "-H", "1.0.0", "loginHook"
		};

		String[] mavenArgs = {
			"create", "-d", "build/test", "-b", "maven", "-t", "fragment", "-h",
			"com.liferay.login.web", "-H", "1.0.0", "loginHook"
		};

		String projectPath = "build/test/loginHook";

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath + "/bnd.bnd"),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		contains(
			checkFileExists(projectPath + "/build.gradle"),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "loginhook-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/loginhook-1.0.0.jar"));

		FileUtils.deleteDirectory(IO.getFile(projectPath));

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath + "/bnd.bnd"),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		MavenRunnerUtil.executeMavenPackage(Paths.get(projectPath), new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "loginHook-1.0.0.jar");
		verifyImportPackage(new File(projectPath + "/target/loginHook-1.0.0.jar"));
	}

	@Test
	public void testCreateMVCPortlet() throws Exception {
		String[] gradleArgs = {
			"create", "-d", "build/test", "-t", "mvc-portlet", "foo"
		};

		String[] mavenArgs = {
			"create", "-d", "build/test", "-b", "maven", "-t",
			"mvc-portlet", "foo"
		};

		String projectPath = "build/test/foo";

		new bladenofail().run(gradleArgs);

		checkGradleBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath + "/src/main/java/foo/portlet/FooPortlet.java"),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(
			checkFileExists(projectPath + "/build.gradle"),
			".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/init.jsp");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "foo-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/foo-1.0.0.jar"));

		FileUtils.deleteDirectory(IO.getFile(projectPath));

		new bladenofail().run(mavenArgs);

		checkMavenBuildFiles(projectPath);

		contains(
			checkFileExists(projectPath + "/src/main/java/foo/portlet/FooPortlet.java"),
			".*^public class FooPortlet extends MVCPortlet.*$");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/init.jsp");

		MavenRunnerUtil.executeMavenPackage(Paths.get(projectPath), new String[]{"clean", "package"});
		MavenRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "foo-1.0.0.jar");
		verifyImportPackage(new File(projectPath + "/target/foo-1.0.0.jar"));
	}

	@Test
	public void testCreateGradleMVCPortletProjectWithPackage()
		throws Exception {

		String[] args = {
			"create", "-d", "build/test", "-t", "mvc-portlet", "-p",
			"com.liferay.test", "foo"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/foo";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		contains(
			checkFileExists(
				projectPath + "/src/main/java/com/liferay/test/portlet/FooPortlet.java"),
			".*^public class FooPortlet extends MVCPortlet.*$");

		contains(
			checkFileExists("build/test/foo/build.gradle"),
			".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/init.jsp");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "com.liferay.test-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/com.liferay.test-1.0.0.jar"));
	}

	@Test
	public void testCreateGradleMVCPortletProjectWithPortletSuffix() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "mvc-portlet", "portlet-portlet"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/portlet-portlet";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		contains(
			checkFileExists(projectPath + "/src/main/java/portlet/portlet/portlet/PortletPortlet.java"),
			".*^public class PortletPortlet extends MVCPortlet.*$");

		contains(
			checkFileExists(projectPath + "/build.gradle"),
			".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/init.jsp");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "portlet.portlet-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/portlet.portlet-1.0.0.jar"));
	}

	@Test
	public void testCreateGradlePortletProject() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "portlet", "-c", "Foo",
			"gradle.test"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/gradle.test";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/build.gradle");

		contains(
			checkFileExists(
				projectPath + "/src/main/java/gradle/test/portlet/FooPortlet.java"),
			new String[] {
				"^package gradle.test.portlet;.*",
				".*javax.portlet.display-name=gradle.test.*",
				".*^public class FooPortlet .*",
				".*printWriter.print\\(\\\"gradle.test Portlet.*"
			});

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "gradle.test-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/gradle.test-1.0.0.jar"));
	}

	@Test
	public void testCreateGradleServiceBuilderDashes() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "service-builder", "-p",
			"com.liferay.backend.integration", "backend-integration"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/backend-integration";

		contains(
			checkFileExists(projectPath + "/settings.gradle"),
			"include \"backend-integration-api\", " +
			"\"backend-integration-service\"");

		contains(
			checkFileExists(projectPath + "/backend-integration-api/bnd.bnd"),
			new String[] {
				".*Export-Package:\\\\.*",
				".*com.liferay.backend.integration.exception,\\\\.*",
				".*com.liferay.backend.integration.model,\\\\.*",
				".*com.liferay.backend.integration.service,\\\\.*",
				".*com.liferay.backend.integration.service.persistence.*"
			});

		contains(
			checkFileExists(
				projectPath + "/backend-integration-service/bnd.bnd"),
				".*Liferay-Service: true.*");

		BuildTask buildServiceTask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildServiceTask);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/backend-integration-api"),
				"com.liferay.backend.integration.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/backend-integration-service"),
				"com.liferay.backend.integration.service-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/backend-integration-service/build/libs/com.liferay.backend.integration.service-1.0.0.jar"));
	}

	@Test
	public void testCreateGradleServiceBuilderDefault() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "service-builder", "-p",
			"com.liferay.docs.guestbook", "guestbook"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/guestbook";

		contains(
			checkFileExists(projectPath + "/settings.gradle"),
			"include \"guestbook-api\", \"guestbook-service\"");

		contains(
			checkFileExists(projectPath + "/guestbook-api/bnd.bnd"),
			new String[] {
				".*Export-Package:\\\\.*",
				".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*",
				".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
			checkFileExists(projectPath + "/guestbook-service/bnd.bnd"),
				".*Liferay-Service: true.*");

		contains(
			checkFileExists(projectPath + "/guestbook-service/build.gradle"),
				".*compileOnly project\\(\":guestbook-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/guestbook-api"), "com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/guestbook-service"), "com.liferay.docs.guestbook.service-1.0.0.jar");

		File serviceJar = new File(projectPath + "/guestbook-service/build/libs/com.liferay.docs.guestbook.service-1.0.0.jar");

		verifyImportPackage(serviceJar);

		try(JarFile serviceJarFile = new JarFile(serviceJar)) {
			String springContext = serviceJarFile.getManifest().getMainAttributes().getValue("Liferay-Spring-Context");

			assertTrue(springContext.equals("META-INF/spring"));
		}
	}

	@Test
	public void testCreateGradleServiceBuilderDots() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "service-builder", "-p",
			"com.liferay.docs.guestbook", "com.liferay.docs.guestbook"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/com.liferay.docs.guestbook";

		contains(
			checkFileExists(projectPath + "/settings.gradle"),
			"include \"com.liferay.docs.guestbook-api\", " +
			"\"com.liferay.docs.guestbook-service\"");

		contains(
			checkFileExists(
				projectPath + "/com.liferay.docs.guestbook-api/bnd.bnd"),
			new String[] {
				".*Export-Package:\\\\.*",
				".*com.liferay.docs.guestbook.exception,\\\\.*",
				".*com.liferay.docs.guestbook.model,\\\\.*",
				".*com.liferay.docs.guestbook.service,\\\\.*",
				".*com.liferay.docs.guestbook.service.persistence.*"
			});

		contains(
			checkFileExists(
				projectPath + "/com.liferay.docs.guestbook-service/bnd.bnd"),
				".*Liferay-Service: true.*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/com.liferay.docs.guestbook-api"),
				"com.liferay.docs.guestbook.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/com.liferay.docs.guestbook-service"),
				"com.liferay.docs.guestbook.service-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/com.liferay.docs.guestbook-service/build/libs/com.liferay.docs.guestbook.service-1.0.0.jar"));
	}

	@Test
	public void testCreateGradleService() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "service", "-s",
			"com.liferay.portal.kernel.events.LifecycleAction", "-c",
			"FooAction", "servicepreaction"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/servicepreaction";

		checkFileExists(projectPath + "/build.gradle");

		File file = new File(projectPath + "/src/main/java/servicepreaction/FooAction.java");

		contains(
			checkFileExists(file.getPath()),
			new String[] {
				"^package servicepreaction;.*",
				".*^import com.liferay.portal.kernel.events.LifecycleAction;$.*",
				".*service = LifecycleAction.class.*",
				".*^public class FooAction implements LifecycleAction \\{.*"
			});

		List<String> lines = new ArrayList<String>();
		String line = null;

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while ((line = reader.readLine()) !=null) {
				lines.add(line);
				if (line.equals("import com.liferay.portal.kernel.events.LifecycleAction;")) {
					lines.add("import com.liferay.portal.kernel.events.LifecycleEvent;");
					lines.add("import com.liferay.portal.kernel.events.ActionException;");
				}

				if (line.equals("public class FooAction implements LifecycleAction {")) {
					String s = new StringBuilder()
					           .append("@Override\n")
					           .append("public void processLifecycleEvent(LifecycleEvent lifecycleEvent)\n")
					           .append("throws ActionException {\n")
					           .append("System.out.println(\"login.event.pre=\" + lifecycleEvent);\n")
					           .append("}\n")
					           .toString();
					lines.add(s);
				}
			}
		}

		try(Writer writer = new FileWriter(file)) {
			for(String string : lines){
				writer.write(string + "\n");
			}
		}

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "servicepreaction-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/servicepreaction-1.0.0.jar"));
	}

	@Test
	public void testCreateGradleServiceWrapper() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "service-wrapper", "-s",
			"com.liferay.portal.kernel.service.UserLocalServiceWrapper",
			"serviceoverride"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/serviceoverride";

		checkFileExists(projectPath + "/build.gradle");

		contains(
			checkFileExists(
				projectPath + "/src/main/java/serviceoverride/Serviceoverride.java"),
			new String[] {
				"^package serviceoverride;.*",
				".*^import com.liferay.portal.kernel.service.UserLocalServiceWrapper;$.*",
				".*service = ServiceWrapper.class.*",
				".*^public class Serviceoverride extends UserLocalServiceWrapper \\{.*",
				".*public Serviceoverride\\(\\) \\{.*"
			});

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "serviceoverride-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/serviceoverride-1.0.0.jar"));
	}

	@Test
	public void testCreateOnExistFolder() throws Exception {
		String[] args = {
			"create", "-d", "build", "-t", "activator", "exist"
		};

		File existFile = IO.getFile("build/exist/file.txt");

		if(!existFile.exists()) {
			IO.getFile("build/exist").mkdirs();
			existFile.createNewFile();
			assertTrue(existFile.exists());
		}

		new bladenofail().run(args);

		String projectPath = "build/exist";

		checkFileDoesNotExists(projectPath+"/bnd.bnd");
	}

	@Test
	public void testCreateGradleSymbolicName() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-p", "foo.bar", "barfoo"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/barfoo";

		checkFileExists(projectPath + "/build.gradle");

		contains(
			checkFileExists(projectPath + "/bnd.bnd"),
			".*Bundle-SymbolicName: foo.bar.*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "foo.bar-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/foo.bar-1.0.0.jar"));
	}

	@Test
	public void testCreateNpmAngular() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "npm-angular-portlet", "npmangular"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/npmangular";

		checkFileExists(projectPath + "/build.gradle");

		File jsp = checkFileExists(projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		contains(jsp, ".*<aui:script require=\"npmangular@1.0.0\">.*");

		contains(jsp, ".*npmangular100.default.*");
	}

	@Test
	public void testCreatePortletConfigurationIcon() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "portlet-configuration-icon", "-p", "blade.test", "icontest"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/icontest";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File componentFile = checkFileExists(
			projectPath + "/src/main/java/blade/test/portlet/configuration/icon/" +
				"IcontestPortletConfigurationIcon.java");

		contains(
			componentFile,
			".*^public class IcontestPortletConfigurationIcon.*extends BasePortletConfigurationIcon.*$");

		File gradleBuildFile = checkFileExists(projectPath + "/build.gradle");

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreatePortletToolbarContributor() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "portlet-toolbar-contributor", "-p", "blade.test",  "toolbartest"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/toolbartest";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File componentFile = checkFileExists(
			projectPath + "/src/main/java/blade/test/portlet/toolbar/contributor/" +
				"ToolbartestPortletToolbarContributor.java");

		contains(
			componentFile,
			".*^public class ToolbartestPortletToolbarContributor.*implements PortletToolbarContributor.*$");

		File gradleBuildFile = checkFileExists(projectPath + "/build.gradle");

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreateProjectAllDefaults() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "hello-world-portlet"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/hello-world-portlet";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File portletFile = checkFileExists(
			projectPath + "/src/main/java/hello/world/portlet/portlet/" +
				"HelloWorldPortlet.java");

		contains(
			portletFile,
			".*^public class HelloWorldPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath + "/build.gradle");

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/init.jsp");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "hello.world.portlet-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/hello.world.portlet-1.0.0.jar"));
	}

	@Test
	public void testCreateProjectWithRefresh() throws Exception {
		String[] args = {
				"create", "-d", "build/test", "hello-world-refresh"
			};

		new bladenofail().run(args);

		String projectPath = "build/test/hello-world-refresh";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File portletFile = checkFileExists(
			projectPath + "/src/main/java/hello/world/refresh/portlet/" +
				"HelloWorldRefreshPortlet.java");

		contains(
			portletFile,
			".*^public class HelloWorldRefreshPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(projectPath + "/build.gradle");

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/view.jsp");

		checkFileExists(
			projectPath + "/src/main/resources/META-INF/resources/init.jsp");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(projectPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "hello.world.refresh-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/hello.world.refresh-1.0.0.jar"));
	}

	@Test
	public void testCreateSimulationPanelEntry() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "simulation-panel-entry", "-p", "test.simulator", "simulator"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/simulator";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File componentFile = checkFileExists(
			projectPath + "/src/main/java/test/simulator/application/list/" +
				"SimulatorSimulationPanelApp.java");

		contains(
			componentFile,
			".*^public class SimulatorSimulationPanelApp.*extends BaseJSPPanelApp.*$");

		File gradleBuildFile = checkFileExists(projectPath + "/build.gradle");

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "test.simulator-1.0.0.jar");
	}

	@Test
	public void testCreateSpringMvcPortlet() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "spring-mvc-portlet", "-p", "test.spring.portlet", "spring-test"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/spring-test";

		checkFileExists(projectPath);

		checkFileExists(
			projectPath + "/src/main/java/test/spring/portlet/portlet/" +
				"SpringTestPortletViewController.java");

		checkFileExists(projectPath + "/build.gradle");

		verifyBuild(projectPath, projectPath, "spring-test.war");
	}

	@Test
	public void testCreateTemplateContextContributor() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "template-context-contributor", "blade-test"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/blade-test";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File componentFile = checkFileExists(
			projectPath + "/src/main/java/blade/test/context/contributor/" +
				"BladeTestTemplateContextContributor.java");

		contains(
			componentFile,
			".*^public class BladeTestTemplateContextContributor.*implements TemplateContextContributor.*$");

		File gradleBuildFile = checkFileExists(projectPath + "/build.gradle");

		contains(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		verifyBuild(projectPath, projectPath, "blade.test-1.0.0.jar");
	}

	@Test
	public void testCreateTheme() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "theme", "theme-test"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/theme-test";

		checkFileExists(projectPath);

		checkFileDoesNotExists(projectPath + "/bnd.bnd");

		checkFileExists(projectPath + "/src/main/webapp/css/_custom.scss");

		File properties = checkFileExists(
			projectPath + "/src/main/webapp/WEB-INF/liferay-plugin-package.properties");

		contains(properties, ".*^name=theme-test.*");

		File buildFile = new File(projectPath + "/build.gradle");

		FileWriter fileWriter = new FileWriter(buildFile, true);
		BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		bufferWriter.write("\nbuildTheme { jvmArgs \"-Djava.awt.headless=true\" }");
		bufferWriter.close();

		verifyBuild(projectPath, projectPath, "theme-test.war");
	}

	@Test
	public void testCreateThemeContributor() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "theme-contributor", "-C", "foobar",
			"theme-contributor-test"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/theme-contributor-test";

		checkFileExists(projectPath);

		File bnd = checkFileExists(projectPath + "/bnd.bnd");

		contains(bnd, ".*Liferay-Theme-Contributor-Type: foobar.*");

		verifyBuild(projectPath, projectPath, "theme.contributor.test-1.0.0.jar");
	}

	@Test
	public void testCreateWorkspaceGradleFragment() throws Exception {
		String[] args = {
			"create", "-d", "build/test/workspace/modules/extensions", "-t",
			"fragment", "-h", "com.liferay.login.web", "-H", "1.0.0", "loginHook"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules/extensions";

		checkFileExists(projectPath + "/loginHook");

		contains(
			checkFileExists(projectPath + "/loginHook/bnd.bnd"),
			new String[] {
				".*^Bundle-SymbolicName: loginhook.*$",
				".*^Fragment-Host: com.liferay.login.web;bundle-version=\"1.0.0\".*$"
			});

		checkFileExists(projectPath + "/loginHook/build.gradle");

		lacks(
			checkFileExists(projectPath + "/loginHook/build.gradle"),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/loginHook"), "loginhook-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/loginHook/build/libs/loginhook-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceGradlePortletProject() throws Exception {
		String[] args = {
			"create", "-d", "build/test/workspace/modules/apps", "-t",
			"portlet", "-c", "Foo", "gradle.test"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules/apps";

		checkFileExists(projectPath + "/gradle.test/build.gradle");

		checkFileDoesNotExists(projectPath + "/gradle.test/gradlew");

		contains(
			checkFileExists(
				projectPath + "/gradle.test/src/main/java/gradle/test/portlet/FooPortlet.java"),
			new String[] {
				"^package gradle.test.portlet;.*",
				".*javax.portlet.display-name=gradle.test.*",
				".*^public class FooPortlet .*",
				".*printWriter.print\\(\\\"gradle.test Portlet.*"
			});

		lacks(
			checkFileExists(projectPath + "/gradle.test/build.gradle"),
			".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/gradle.test"), "gradle.test-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/gradle.test/build/libs/gradle.test-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectApiPath()
		throws Exception {

		String[] args = {
			"create", "-d", "build/test/workspace/modules/nested/path",
			"-t", "service-builder", "-p", "com.liferay.sample", "sample"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		assertTrue(
			new File("build/test/workspace/modules/nested/path").mkdirs());

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules/nested/path";

		checkFileExists(projectPath + "/sample/build.gradle");

		checkFileDoesNotExists(projectPath + "/sample/settings.gradle");

		checkFileExists(projectPath + "/sample/sample-api/build.gradle");

		checkFileExists(projectPath + "/sample/sample-service/build.gradle");

		contains(
			checkFileExists(
				projectPath + "/sample/sample-service/build.gradle"),
				".*compileOnly project\\(\":modules:nested:path:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/sample/sample-api"), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/sample/sample-service"), "com.liferay.sample.service-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/sample/sample-service/build/libs/com.liferay.sample.service-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDashes()
		throws Exception {

		String[] args = {
			"create", "-d", "build/test/workspace/modules", "-t",
			"service-builder", "-p", "com.sample", "workspace-sample"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules";

		checkFileExists(projectPath + "/workspace-sample/build.gradle");

		checkFileDoesNotExists(
			projectPath + "/workspace-sample/settings.gradle");

		checkFileExists(
			projectPath + "/workspace-sample/workspace-sample-api/build.gradle");

		checkFileExists(
			projectPath + "/workspace-sample/workspace-sample-service/build.gradle");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/workspace-sample/workspace-sample-api"),
				"com.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/workspace-sample/workspace-sample-service"),
				"com.sample.service-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/workspace-sample/workspace-sample-service/build/libs/com.sample.service-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDefault()
		throws Exception {

		String[] args = {
			"create", "-d", "build/test/workspace/modules", "-t",
			"service-builder", "-p", "com.liferay.sample", "sample"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules";

		checkFileExists(projectPath + "/sample/build.gradle");

		checkFileDoesNotExists(projectPath + "/sample/settings.gradle");

		checkFileExists(projectPath + "/sample/sample-api/build.gradle");

		checkFileExists(projectPath + "/sample/sample-service/build.gradle");

		contains(
			checkFileExists(
				projectPath + "/sample/sample-service/build.gradle"),
				".*compileOnly project\\(\":modules:sample:sample-api\"\\).*");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/sample/sample-api"), "com.liferay.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/sample/sample-service"), "com.liferay.sample.service-1.0.0.jar");

		File serviceJar = new File(projectPath + "/sample/sample-service/build/libs/com.liferay.sample.service-1.0.0.jar");

		verifyImportPackage(serviceJar);

		try (JarFile serviceJarFile = new JarFile(serviceJar)) {
			String springContext = serviceJarFile.getManifest().getMainAttributes().getValue("Liferay-Spring-Context");

			assertTrue(springContext.equals("META-INF/spring"));
		}
	}

	@Test
	public void testCreateWorkspaceGradleServiceBuilderProjectDots()
		throws Exception {

		String[] args = {
			"create", "-d", "build/test/workspace/modules", "-t",
			"service-builder", "-p", "com.sample", "workspace.sample"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules";

		checkFileExists(projectPath + "/workspace.sample/build.gradle");

		checkFileDoesNotExists(
			projectPath + "/workspace.sample/settings.gradle");

		checkFileExists(
			projectPath + "/workspace.sample/workspace.sample-api/build.gradle");

		checkFileExists(
			projectPath + "/workspace.sample/workspace.sample-service/build.gradle");

		BuildTask buildService = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "buildService");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildService);
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/workspace.sample/workspace.sample-api"),
				"com.sample.api-1.0.0.jar");
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/workspace.sample/workspace.sample-service"),
				"com.sample.service-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/workspace.sample/workspace.sample-service/build/libs/com.sample.service-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceModuleLocation() throws Exception {
		String[] args = {"-b", "build/test/workspace", "create", "foo"};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules";

		checkFileExists(projectPath + "/foo");

		checkFileExists(projectPath + "/foo/bnd.bnd");

		File portletFile = checkFileExists(
			projectPath + "/foo/src/main/java/foo/portlet/FooPortlet.java");

		contains(
			portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(
			projectPath + "/foo/build.gradle");

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/foo"), "foo-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/foo/build/libs/foo-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceProjectAllDefaults() throws Exception {
		String[] args = {
			"create", "-d", "build/test/workspace/modules/apps", "foo"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/modules/apps";

		checkFileExists(projectPath + "/foo");

		checkFileExists(projectPath + "/foo/bnd.bnd");

		File portletFile = checkFileExists(
			projectPath + "/foo/src/main/java/foo/portlet/FooPortlet.java");

		contains(
			portletFile, ".*^public class FooPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(
			projectPath + "/foo/build.gradle");

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath + "/foo"), "foo-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/foo/build/libs/foo-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceProjectWithRefresh() throws Exception {
		String[] args = {
			"create", "-d", "build/test/workspace/modules/apps",
			"foo-refresh"
		};

		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath =
				"build/test/workspace/modules/apps/foo-refresh";

		checkFileExists(projectPath);

		checkFileExists(projectPath + "/bnd.bnd");

		File portletFile = checkFileExists(
				projectPath +
				"/src/main/java/foo/refresh/portlet/FooRefreshPortlet.java");

		contains(
			portletFile,
			".*^public class FooRefreshPortlet extends MVCPortlet.*$");

		File gradleBuildFile = checkFileExists(
			projectPath + "/build.gradle");

		lacks(gradleBuildFile, ".*^apply plugin: \"com.liferay.plugin\".*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "jar");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "foo.refresh-1.0.0.jar");

		verifyImportPackage(new File(projectPath + "/build/libs/foo.refresh-1.0.0.jar"));
	}

	@Test
	public void testCreateWorkspaceThemeLocation() throws Exception {
		String[] args = {
				"-b", "build/test/workspace", "create", "-t", "theme",
				"theme-test"};
		File workspace = new File("build/test/workspace");

		makeWorkspace(workspace);

		new bladenofail().run(args);

		String projectPath = "build/test/workspace/wars/theme-test";

		checkFileExists(projectPath);

		checkFileDoesNotExists(projectPath + "/bnd.bnd");

		checkFileExists(projectPath + "/src/main/webapp/css/_custom.scss");

		File properties = checkFileExists(
			projectPath + "/src/main/webapp/WEB-INF/liferay-plugin-package.properties");

		contains(properties, ".*^name=theme-test.*");

		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(workspace.toPath(), "war");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), "theme-test.war");
	}

	@Test
	public void testListTemplates() throws Exception {
		String[] args = {"create", "-l"};

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(output);

		blade blade = new bladenofail(ps);

		blade.run(args);

		String templateList = new String(output.toByteArray());

		List<String> templateNames =
			new ArrayList<>(ProjectTemplates.getTemplates().keySet());

		for (String templateName : templateNames) {
			assertTrue(templateList.contains(templateName));
		}
	}

	@Test
	public void testWrongTemplateTyping() throws Exception {
		String[] args = {
			"create", "-d", "build/test", "-t", "activatorXXX", "wrong-activator"
		};

		new bladenofail().run(args);

		String projectPath = "build/test/wrong-activator";

		checkFileDoesNotExists(projectPath);
	}

	private void checkGradleBuildFiles(String projectPath) {
		checkFileExists(projectPath);
		checkFileExists(projectPath + "/bnd.bnd");
		checkFileExists(projectPath + "/build.gradle");
		checkFileExists(projectPath + "/gradlew");
		checkFileExists(projectPath + "/gradlew.bat");
	}

	private void checkMavenBuildFiles(String projectPath) {
		checkFileExists(projectPath);
		checkFileExists(projectPath + "/bnd.bnd");
		checkFileExists(projectPath + "/pom.xml");
		checkFileExists(projectPath + "/mvnw");
		checkFileExists(projectPath + "/mvnw.cmd");
	}

	private File checkFileDoesNotExists(String path) {
		File file = IO.getFile(path);

		assertFalse(file.exists());

		return file;
	}

	private File checkFileExists(String path) {
		File file = IO.getFile(path);

		assertTrue(file.exists());

		return file;
	}

	private void contains(File file, String pattern) throws Exception {
		String content = new String(IO.read(file));

		contains(content, pattern);
	}

	private void contains(File file, String[] patterns) throws Exception {
		String content = new String(IO.read(file));

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

	private void lacks(File file, String pattern) throws Exception {
		String content = new String(IO.read(file));

		assertFalse(
			Pattern.compile(
				pattern,
				Pattern.MULTILINE | Pattern.DOTALL).matcher(content).matches());
	}

	private void makeWorkspace(File workspace) throws Exception {
		String[] args = {"-b", workspace.getParentFile().getPath(), "init", workspace.getName()};

		new bladenofail().run(args);

		assertTrue(Util.isWorkspace(workspace.toPath()));
	}

	private void verifyBuild(String runnerPath, String projectPath, String outputFileName) {
		BuildTask buildtask = GradleRunnerUtil.executeGradleRunner(Paths.get(runnerPath), "build");
		GradleRunnerUtil.verifyGradleRunnerOutput(buildtask);
		GradleRunnerUtil.verifyBuildOutput(Paths.get(projectPath), outputFileName);
	}

	private void verifyImportPackage(File serviceJar) throws Exception {
		try (Jar jar = new Jar(serviceJar)) {
			Manifest m = jar.getManifest();
			Domain domain = Domain.domain(m);
			Parameters imports = domain.getImportPackage();

			for (String key : imports.keySet()) {
				assertFalse(key.isEmpty());
			}
		}
	}
}