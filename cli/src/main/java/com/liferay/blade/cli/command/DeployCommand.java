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
import java.io.PrintStream;
import java.net.ConnectException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.gradle.GradleTooling;
import com.liferay.blade.cli.gradle.LiferayBundleDeployerImpl;
import com.liferay.blade.cli.gradle.ProcessResult;
import com.liferay.blade.cli.util.BladeUtil;
import com.liferay.blade.cli.util.FileWatcher;
import com.liferay.blade.cli.util.FileWatcher.Consumer;

/**
 * @author Gregory Amerson
 */
public class DeployCommand extends BaseCommand<DeployArgs> {

	public DeployCommand() {
	}

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();
		String host = "localhost";
		int port = 11311;

		if (!BladeUtil.canConnect(host, port)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Unable to connect to gogo shell on " + host + ":" + port);
			sb.append(System.lineSeparator());
			sb.append("Liferay may not be running, or the gogo shell may need to be enabled. ");
			sb.append("Please see this link for more details: ");
			sb.append("https://dev.liferay.com/en/develop/reference/-/knowledge_base/7-1/using-the-felix-gogo-shell");
			sb.append(System.lineSeparator());

			_addError(sb.toString());

			PrintStream err = bladeCLI.err();

			new ConnectException(sb.toString()).printStackTrace(err);

			return;
		}

		GradleExec gradleExec = new GradleExec(bladeCLI);

		Set<File> outputFiles = GradleTooling.getOutputFiles(bladeCLI.getCacheDir(), bladeCLI.getBase());

		DeployArgs deployArgs = getArgs();

		if (deployArgs.isWatch()) {
			_deployWatch(gradleExec, outputFiles, host, port);
		}
		else {
			_deploy(gradleExec, outputFiles, host, port);
		}
	}

	@Override
	public Class<DeployArgs> getArgsClass() {
		return DeployArgs.class;
	}


	private void _addError(String msg) {
		getBladeCLI().addErrors("deploy", Collections.singleton(msg));
	}

	private void _addError(String prefix, String msg) {
		getBladeCLI().addErrors(prefix, Collections.singleton(msg));
	}

	private void _deploy(GradleExec gradle, Set<File> outputFiles, String host, int port) throws Exception {
		ProcessResult processResult = gradle.executeGradleCommand("assemble -x check");

		int resultCode = processResult.getResultCode();

		BladeCLI bladeCLI = getBladeCLI();

		if (resultCode > 0) {
			String errorMessage = "Gradle assemble task failed.";

			_addError(errorMessage);

			PrintStream err = bladeCLI.err();

			_addError(processResult.getError());

			new ConnectException(errorMessage).printStackTrace(err);

			return;
		}

		Stream<File> stream = outputFiles.stream();

		stream.filter(
			File::exists
		).forEach(
			outputFile -> {
				try {
					_installOrUpdate(outputFile, host, port);
				}
				catch (Exception e) {
					String message = e.getMessage();

					Class<?> exceptionClass = e.getClass();

					if (message == null) {
						message = "DeployCommand._deploy threw " + exceptionClass.getSimpleName();
					}

					_addError(message);

					PrintStream err = bladeCLI.err();

					e.printStackTrace(err);
				}
			}
		);
	}

	private void _deployWatch(final GradleExec gradleExec, final Set<File> outputFiles, String host, int port)
		throws Exception {

		_deploy(gradleExec, outputFiles, host, port);

		Stream<File> stream = outputFiles.stream();

		Collection<Path> outputPaths = stream.map(
			File::toPath
		).collect(
			Collectors.toSet()
		);

		BladeCLI bladeCLI = getBladeCLI();

		new Thread() {

			@Override
			public void run() {
				try {
					gradleExec.executeGradleCommand("assemble -x check -t");
				}
				catch (Exception e) {
					String message = e.getMessage();

					if (message == null) {
						message = "Gradle build task failed.";
					}

					_addError("deploy watch", message);

					PrintStream err = bladeCLI.err();

					e.printStackTrace(err);
				}
			}

		}.start();

		Consumer<Path> consumer = new Consumer<Path>() {

			@Override
			public void consume(Path modified) {
				try {
					File file = modified.toFile();

					File modifiedFile = file.getAbsoluteFile();

					if (outputPaths.contains(modifiedFile.toPath())) {
						bladeCLI.out("installOrUpdate " + modifiedFile);

						_installOrUpdate(modifiedFile, host, port);
					}
				}
				catch (Exception e) {
					String exceptionMessage = e.getMessage() == null ? "" : (System.lineSeparator() + e.getMessage());

					String message = "Error: Bundle Insatllation failed: " + modified + exceptionMessage;

					_addError(message);

					PrintStream err = bladeCLI.err();

					e.printStackTrace(err);
				}
			}

		};

		File base = bladeCLI.getBase();

		new FileWatcher(base.toPath(), true, consumer);
	}



	private void _installOrUpdate(File file, String host, int port) throws Exception {
		file = file.getAbsoluteFile();

		LiferayBundleDeployerImpl.installOrUpdate(file, getBladeCLI(), host, port);
	}



}