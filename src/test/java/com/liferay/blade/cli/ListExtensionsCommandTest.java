package com.liferay.blade.cli;

import java.io.File;
import org.gradle.tooling.internal.consumer.ConnectorServices;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import aQute.lib.io.IO;

public class ListExtensionsCommandTest {

	@After
	public void cleanUp() throws Exception {
		ConnectorServices.reset();

		if (_testdir.exists()) {
			IO.delete(_testdir);
			Assert.assertFalse(_testdir.exists());
		}
	}
	
	@Test
	public void testListExtensions() throws Exception {
		String[] args =
			{"extension", "list", "src/test/resources/com/liferay/blade/cli/extensions/extensions.xml"};

		String content = TestUtil.runBlade(args);

		Assert.assertTrue(content.contains("ext1 description"));
		Assert.assertTrue(content.contains("ext1 location"));
		Assert.assertTrue(content.contains("ext2 description"));
		Assert.assertTrue(content.contains("ext2 location"));
	}
	

	@Before
	public void setUp() throws Exception {
		_testdir.mkdirs();
		
		File newFile = new File(_testdir, "afile");
		
		if (newFile.exists())
			newFile.delete();

		Assert.assertTrue(newFile.createNewFile());
	}
	
	private static File _testdir = IO.getFile("build/test");
}
