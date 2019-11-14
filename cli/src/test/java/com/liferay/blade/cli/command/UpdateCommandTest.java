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

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.BladeTestResults;
import com.liferay.blade.cli.TestUtil;
import com.liferay.blade.cli.WorkspaceProvider;
import com.liferay.blade.cli.gradle.GradleWorkspaceProvider;
import com.liferay.blade.cli.util.BladeVersions;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Christopher Bryan Boyd
 * @author Vernon Singleton
 * @author Gregory Amerson
 */
@PrepareForTest({ServiceLoader.class, UpdateCommand.class, BladeVersions.class, BladeCLI.class})
@RunWith(PowerMockRunner.class)
public class UpdateCommandTest {

	@Before
	public void setUp() throws Exception {
		_rootDir = temporaryFolder.getRoot();

		_extensionsDir = temporaryFolder.newFolder(".blade", "extensions");

		PowerMock.mockStatic(ServiceLoader.class);

		_setupWorkspaceProviderServiceLoader();

		_setupCommandServiceLoader();

		PowerMock.replayAll();
	}

	@Test
	public void testCurrentMajorLessThanUpdatedMajorDefault() {
		String currentVersion = "1.5.9.1.2.3.4.5.6.7.8.9";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.1.1.5.6.7-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));
		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorLessThanUpdatedMajorReleaseToRelease() {
		String currentVersion = "1.5.9.1.2.3.4.5.6.7.8.9";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.1.1.5.6.7-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorLessThanUpdatedMajorReleaseToSnapshot() {
		String currentVersion = "1.5.9.1.2.3.4.5.6.7.8.9";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.1.1.5.6.7-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-s", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorLessThanUpdatedMajorSnapshotToRelease() {
		String currentVersion = "1.5.9.1.2.3.4.5.6.7.8.9-snapshot";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.1.1.5.6.7-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorLessThanUpdatedMajorSnapshotToSnapshot() {
		String currentVersion = "1.5.9.1.2.3.4.5.6.7.8.9-snapshot";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.1.1.5.6.7-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-s", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorMoreThanUpdatedMajorDefault() {
		String currentVersion = "3.0.0.2018.10.23.1234";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.5.9-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorMoreThanUpdatedMajorRelease() {
		String currentVersion = "3.0.0.2018.10.23.1234";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.5.9-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMajorMoreThanUpdatedMajorSnapshot() {
		String currentVersion = "3.0.0.2018.10.23.1234";
		String releaseUpdateVersion = "2.1.1.4.5.6";
		String snapshotUpdateVersion = "2.5.9-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-s", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMinorLessThanUpdatedMinorDefault() {
		String currentVersion = "12.1.9.SCHWIBBY";
		String releaseUpdateVersion = "12.2.0";
		String snapshotUpdateVersion = "12.2.1-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMinorLessThanUpdatedMinorRelease() {
		String currentVersion = "12.1.9.SCHWIBBY";
		String releaseUpdateVersion = "12.2.0";
		String snapshotUpdateVersion = "12.2.1-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMinorLessThanUpdatedMinorSnapshot() {
		String currentVersion = "12.1.9.SCHWIBBY";
		String releaseUpdateVersion = "12.2.0";
		String snapshotUpdateVersion = "12.2.1-snapshot";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-s", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMinorMoreThanUpdatedMinorDefault() {
		String currentVersion = "3.6.0.001810231234";
		String releaseUpdateVersion = "3.5.9-SCHNAPS";
		String snapshotUpdateVersion = "3.5.9-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMinorMoreThanUpdatedMinorRelease() {
		String currentVersion = "3.6.0.001810231234";
		String releaseUpdateVersion = "3.5.9-SCHNAPS";
		String snapshotUpdateVersion = "3.5.9-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentMinorMoreThanUpdatedMinorSnapshot() {
		String currentVersion = "3.6.0.001810231234";
		String releaseUpdateVersion = "3.5.9-SCHNAPS";
		String snapshotUpdateVersion = "3.5.9-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-s", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentPatchLessThanUpdatedPatchDefault() {
		String currentVersion = "123.10.10.SCHOOBY";
		String releaseUpdateVersion = "123.10.20-whiff";
		String snapshotUpdateVersion = "123.10.20-whiff-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentPatchLessThanUpdatedPatchRelease() {
		String currentVersion = "123.10.10.SCHOOBY";
		String releaseUpdateVersion = "123.10.20-whiff";
		String snapshotUpdateVersion = "123.10.20-whiff-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentPatchLessThanUpdatedPatchSnapshot() {
		String currentVersion = "123.10.10.SCHOOBY";
		String releaseUpdateVersion = "123.10.20-whiff";
		String snapshotUpdateVersion = "123.10.20-whiff-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-s", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentPatchMoreThanUpdatedPatchDefault() {
		String currentVersion = "3.5.9.001810231234";
		String releaseUpdateVersion = "3.5.8.999999";
		String snapshotUpdateVersion = "3.5.8.999999-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testCurrentSnapshot() {
		String currentVersion = "3.4.0.SNAPSHOT201812060746";
		String releaseUpdateVersion = "3.4.0-20181206.074623-13";
		String snapshotUpdateVersion = "3.4.0-20181206.074623-13";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testTwoSnapshotVersions() {
		String currentVersion = "3.3.1.SNAPSHOT201811211846";

		String releaseUpdateVersion = "3.3.1-20181128.214621-308";
		String snapshotUpdateVersion = "3.3.1-20181128.214621-308-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + releaseUpdateVersion,
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Test
	public void testUpdateSnapshotButNotRelease() {
		String currentVersion = "3.8.0-SNAPSHOT";
		String releaseUpdateVersion = "3.7.9";
		String snapshotUpdateVersion = "3.8.1-SNAPSHOT";

		_setupStaticVersionsMock(currentVersion, releaseUpdateVersion, snapshotUpdateVersion);

		BladeTestResults results = TestUtil.runBlade(_rootDir, _extensionsDir, false, "update", "-r", "--check");

		String resultsOutput = results.getOutput();

		Assert.assertFalse(
			"currentVersion = " + currentVersion + " should NOT be updated.",
			resultsOutput.contains("A new release update is available for blade: " + releaseUpdateVersion));

		Assert.assertTrue(
			"currentVersion = " + currentVersion + " should be updated to " + snapshotUpdateVersion,
			resultsOutput.contains("A new snapshot update is available for blade: " + snapshotUpdateVersion));
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void _setupCommandServiceLoader() {
		ServiceLoader<BaseCommand> commandServiceLoader = PowerMock.createNiceMock(ServiceLoader.class);

		Collection<BaseCommand> commands = new ArrayList<>();

		commands.add(new UpdateCommand());

		EasyMock.expect(
			commandServiceLoader.iterator()
		).andReturn(
			commands.iterator()
		).once();

		Capture<Class<BaseCommand>> classCapture = EasyMock.newCapture();

		EasyMock.expect(
			ServiceLoader.load(EasyMock.capture(classCapture), EasyMock.anyObject())
		).andReturn(
			commandServiceLoader
		).once();
	}

	private static void _setupStaticVersionsMock(
		String currentVersion, String releaseUpdateVersion, String snapshotUpdateVersion) {

		try {
			PowerMock.mockStaticPartialNice(UpdateCommand.class, "_getVersions");

			IExpectationSetters<Object> andReturn = PowerMock.expectPrivate(UpdateCommand.class, "_getVersions");

			andReturn.andReturn(new BladeVersions(currentVersion, releaseUpdateVersion, snapshotUpdateVersion));

			PowerMock.replayAll();
		}
		catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void _setupWorkspaceProviderServiceLoader() {
		ServiceLoader<WorkspaceProvider> workspaceServiceLoader = PowerMock.createNiceMock(ServiceLoader.class);

		Collection<WorkspaceProvider> workspaceProviders = new ArrayList<>();

		workspaceProviders.add(new GradleWorkspaceProvider());

		EasyMock.expect(
			workspaceServiceLoader.iterator()
		).andReturn(
			workspaceProviders.iterator()
		).once();

		Capture<Class<WorkspaceProvider>> workspaceClassCapture = EasyMock.newCapture();

		EasyMock.expect(
			ServiceLoader.load(EasyMock.capture(workspaceClassCapture), EasyMock.anyObject())
		).andReturn(
			workspaceServiceLoader
		).once();
	}

	private File _extensionsDir = null;
	private File _rootDir = null;

}