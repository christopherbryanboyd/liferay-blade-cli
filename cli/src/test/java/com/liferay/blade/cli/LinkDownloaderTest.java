package com.liferay.blade.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.liferay.blade.cli.util.BladeUtil;


public class LinkDownloaderTest {
	@Test
	public void testDownloadGithubFile() throws Exception {
		File destinationDirectory = temporaryFolder.newFolder();
		
		Path destinationDirectoryPath = destinationDirectory.toPath();
		
		Path destinationFilePath = destinationDirectoryPath.resolve("build.gradle");
		
		BladeUtil.downloadGithubFile(_TEST_URL, destinationFilePath);
		
		boolean fileExists = Files.exists(destinationFilePath);
		
		Assert.assertTrue("Downloaded file should exist", fileExists);
		
		List<String> lines = Files.readAllLines(destinationFilePath);
		
		boolean isEmpty = lines.isEmpty();
		
		Assert.assertFalse("Downloaded file should not be empty", isEmpty);
	}
	
	private static final String _TEST_URL = "https://github.com/liferay/liferay-blade-cli/blob/master/build.gradle";

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();
}
