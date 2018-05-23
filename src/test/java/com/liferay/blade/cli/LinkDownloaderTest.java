package com.liferay.blade.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import aQute.lib.io.IO;

public class LinkDownloaderTest {

	@After
	public void cleanUp() throws Exception {
		ConnectorServices.reset();

		if (_testdir.exists()) {
			IO.delete(_testdir);
			Assert.assertFalse(_testdir.exists());
		}
	}
	
	@Test
	public void testMavenInitWorkspaceDirectoryHasFiles() throws Exception {
		
		Path targetFile = new File(_testdir, "bnd.bnd").toPath();
		
		String link = "https://raw.githubusercontent.com/liferay/liferay-blade-cli/master/bnd.bnd";
		
		Util.downloadLink(link, targetFile);
		
		Assert.assertTrue(Files.exists(targetFile));
	}

	@Before
	public void setUp() throws Exception {
		_testdir.mkdirs();

		Assert.assertTrue(new File(_testdir, "afile").createNewFile());
	}
	
	private static File _testdir = IO.getFile("build/test");
}
