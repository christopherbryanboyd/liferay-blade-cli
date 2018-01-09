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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.liferay.blade.cli.FileWatcher.Consumer;
import com.liferay.blade.cli.util.FilesUtil;

/**
 * @author Greg Amerson
 */
public class FileWatcherTest {

	@Before
	public void setUp() throws Exception {
		if (Files.notExists(testdir))
		{
			Files.createDirectories(testdir);
			assertTrue(Files.exists(testdir));
			Files.createFile(testfile);
			assertTrue(Files.exists(testfile));
		}
	}

	@After
	public void cleanUp() throws Exception {
		if (Files.exists(testdir))
		{
			FilesUtil.delete(testdir);
		}
		assertTrue(Files.notExists(testdir));
	}

	@Test
	@Ignore
	public void testFileWatcherMultipleFiles() throws Exception {
		Files.write(testfile, "foobar".getBytes());

		final Map<Path, Boolean> changed = new HashMap<>();

		changed.put(testfile, false);
		changed.put(testsecondfile, false);

		final CountDownLatch latch = new CountDownLatch(2);

		final Consumer<Path> consumer = new Consumer<Path>() {

			@Override
			public void consume(Path modified) {
				changed.put(modified, true);
				latch.countDown();
			}

		};

		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					new FileWatcher(testdir, false, consumer);
				}
				catch (IOException ioe) {
				}
			}

		};

		t.setDaemon(true);
		t.start();

		// let the file watcher get all registered before we touch the file

		Thread.sleep(2000);

		Files.write(testfile, "touch".getBytes());
		Files.write(testsecondfile, "second file content".getBytes());

		latch.await();

		for (Path path : changed.keySet()) {
			assertTrue(changed.get(path));
		}
	}

	@Test
	@Ignore
	public void testFileWatcherSingleFile() throws Exception {
		final boolean[] changed = new boolean[1];
		final CountDownLatch latch = new CountDownLatch(1);

		final Consumer<Path> consumer = new Consumer<Path>() {

			@Override
			public void consume(Path modified) {
				changed[0] = true;
				latch.countDown();
			}

		};

		assertFalse(changed[0]);

		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					new FileWatcher(
						testdir, testfile, false, consumer);
				}
				catch (IOException ioe) {
				}
			}

		};

		t.setDaemon(true);
		t.start();

		// let the file watcher get all registered before we touch the file

		Thread.sleep(1000);

		Files.write(testfile, "touch".getBytes());

		latch.await();

		assertTrue(changed[0]);
	}

	private final Path testdir = Paths.get("build/watch");
	private final Path testfile = testdir.resolve("file.txt");
	private final Path testsecondfile = testdir.resolve("second.txt");

}