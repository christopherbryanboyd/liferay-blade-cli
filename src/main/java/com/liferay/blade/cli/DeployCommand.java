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

import aQute.bnd.header.Attrs;
import aQute.bnd.osgi.Domain;

import com.liferay.blade.cli.FileWatcher.Consumer;
import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.gradle.GradleTooling;
import com.liferay.blade.cli.gradle.MavenExec;
import com.liferay.blade.cli.util.PomUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import java.net.URI;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.dto.BundleDTO;

/**
 * @author Gregory Amerson
 */
public class DeployCommand {

	public DeployCommand(BladeCLI blade, DeployCommandArgs args) throws Exception {
		_blade = blade;
		_args = args;

		_host = "localhost";
		_port = 11311;
	}

	public void execute() throws Exception {
		if (!Util.canConnect(_host, _port)) {
			_addError("deploy", "Unable to connect to gogo shell on " + _host + ":" + _port);

			return;
		}

		Path currentPath = Paths.get("./");
		if (Util.isProjectGradle(currentPath)) {
			GradleExec gradleExec = new GradleExec(_blade);

			Set<File> outputFiles = GradleTooling.getOutputFiles(_blade.getCacheDir(), _blade.getBase());

			if (_args.isWatch()) {
				_deployGradleWatch(gradleExec, outputFiles);
			}
			else {
				_deployGradle(gradleExec, outputFiles);
			}
		} else if (Util.isProjectMaven(currentPath)) {
			
			if (_args.isWatch()) {
				_deployMavenWatch(currentPath);
			}
			else
			{
				
				_deployMaven(currentPath);
			}
		}
		else {
			_blade.err("Unknown Project Type");
		}

	}

	private void _deployMaven(Path currentPath) throws Exception {
		MavenExec mavenExec = new MavenExec(_blade);
		
		if (mavenExec.executeMavenCommand("clean") == 0) {
		
			if (mavenExec.executeMavenCommand("package") == 0) { 
				
				installFromMavenCurrentPath(currentPath);

			}
		}
	}
	
	private void _deployMavenWatch(Path currentPath) throws Exception {

		if (PomUtil.addPluginToPom(currentPath.resolve("pom.xml"))) {

			MavenExec mavenExec = new MavenExec(_blade);

			if (mavenExec.executeMavenCommand("clean") == 0) {
				
				if (mavenExec.executeMavenCommand("package") == 0) {

					installFromMavenCurrentPath(currentPath);
	
					Process pr = mavenExec.executeMavenCommandAsync("fizzed-watcher:run");
		
					try (BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
					
						CompletableFuture.runAsync(() -> {
							String line;
							try {
								while ((line = in.readLine()) != null) {
									_blade.out(line);
									if (line.contains("BUILD SUCCESS")) {
										installFromMavenCurrentPath(currentPath);
									}
								}
							} catch (IOException e) {
								_blade.err(e.getMessage());
								e.printStackTrace(_blade.err());
							}
						});
						pr.waitFor();
					}
					
				} 
				else {
					_blade.err("Unable to build project.");
				}
			}
			else {
				_blade.err("Unable to clean project.");
			}
		} 
		else {
			_blade.err("Unable to add Maven Watcher Plugin to pom.xml");
		}
	}

	private void installFromMavenCurrentPath(Path currentPath) {
		File outputFile = Util.getMavenOutputFile(currentPath).toFile();

		try {
			_installOrUpdate(outputFile);
		} catch (Exception e) {
			PrintStream error = _blade.err();

			error.println(e.getMessage());

			e.printStackTrace(error);
		}
	}

	private static void _deployWar(File file, LiferayBundleDeployer deployer) throws Exception {
		URI uri = file.toURI();

		long bundleId = deployer.install(uri);

		if (bundleId > 0) {
			deployer.start(bundleId);
		}
		else {
			throw new Exception("Failed to deploy war: " + file.getAbsolutePath());
		}
	}

	private void _addError(String msg) {
		_blade.addErrors("deploy", Collections.singleton(msg));
	}

	private void _addError(String prefix, String msg) {
		_blade.addErrors(prefix, Collections.singleton(msg));
	}

	private void _deployGradle(GradleExec gradle, Set<File> outputFiles) throws Exception {
		int retcode = gradle.executeGradleCommand("assemble -x check");

		if (retcode > 0) {
			_addError("Gradle assemble task failed.");

			return;
		}

		Stream<File> stream = outputFiles.stream();

		stream.filter(
			File::exists
		).forEach(
			outputFile -> {
				try {
					_installOrUpdate(outputFile);
				}
				catch (Exception e) {
					PrintStream err = _blade.err();

					err.println(e.getMessage());

					e.printStackTrace(err);
				}
			}
		);
	}

	private void _deployBundle(File file, LiferayBundleDeployer client, Domain bundle, Entry<String, Attrs> bsn)
		throws Exception {

		Entry<String, Attrs> fragmentHost = bundle.getFragmentHost();

		String hostBsn = null;

		if (fragmentHost != null) {
			hostBsn = fragmentHost.getKey();
		}

		Collection<BundleDTO> bundles = client.getBundles();

		long existingId = client.getBundleId(bundles, bsn.getKey());

		long hostId = client.getBundleId(bundles, hostBsn);

		URI uri = file.toURI();

		if (existingId > 0) {
			_reloadExistingBundle(client, fragmentHost, existingId, hostId, uri);
		}
		else {
			_installNewBundle(client, bsn, fragmentHost, hostId, uri);
		}
	}

	private void _deployGradleWatch(final GradleExec gradleExec, final Set<File> outputFiles) throws Exception {
		_deployGradle(gradleExec, outputFiles);

		Stream<File> stream = outputFiles.stream();

		Collection<Path> outputPaths = stream.map(
			File::toPath
		).collect(
			Collectors.toSet()
		);

		new Thread() {

			@Override
			public void run() {
				try {
					gradleExec.executeGradleCommand("assemble -x check -t");
				}
				catch (Exception e) {
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
						_blade.out("installOrUpdate " + modifiedFile);

						_installOrUpdate(modifiedFile);
					}
				}
				catch (Exception e) {
				}
			}

		};

		File base = _blade.getBase();

		new FileWatcher(base.toPath(), true, consumer);
	}

	private void _installNewBundle(
			LiferayBundleDeployer client, Entry<String, Attrs> bsn, Entry<String, Attrs> fragmentHost, long hostId,
			URI uri)
		throws Exception {

		PrintStream out = _blade.out();

		long existingId = client.install(uri);

		if ((fragmentHost != null) && (hostId > 0)) {
			client.refresh(hostId);

			out.println("Installed fragment bundle " + existingId);
		}
		else {
			long checkedExistingId = client.getBundleId(bsn.getKey());

			try {
				if (!Objects.equals(existingId, checkedExistingId)) {
					out.print("Error: Bundle IDs do not match.");
				}
				else {
					if (checkedExistingId > 1) {
						client.start(checkedExistingId);

						out.println("Installed bundle " + existingId);
					}
					else {
						out.println("Error: Bundle failed to install: " + bsn);
					}
				}
			}
			catch (Exception e) {
				out.println("Error: Bundle failed to install: " + bsn);
				e.printStackTrace(out);
			}
		}
	}

	private void _installOrUpdate(File file) throws Exception {
		file = file.getAbsoluteFile();

		try (LiferayBundleDeployer client = LiferayBundleDeployer.newInstance(_host, _port)) {
			String name = file.getName();

			name = name.toLowerCase();

			Domain bundle = Domain.domain(file);

			Entry<String, Attrs> bsn = bundle.getBundleSymbolicName();

			if (bsn != null) {
				_deployBundle(file, client, bundle, bsn);
			}
			else if (name.endsWith(".war")) {
				_deployWar(file, client);
			}
		}
	}

	private final void _reloadExistingBundle(
			LiferayBundleDeployer client, Entry<String, Attrs> fragmentHost, long existingId, long hostId, URI uri)
		throws Exception {

		if ((fragmentHost != null) && (hostId > 0)) {
			client.reloadFragment(existingId, hostId, uri);
		}
		else {
			client.reloadBundle(existingId, uri);
		}

		PrintStream out = _blade.out();

		out.println("Updated bundle " + existingId);
	}

	private final DeployCommandArgs _args;
	private final BladeCLI _blade;
	private final String _host;
	private final int _port;

}