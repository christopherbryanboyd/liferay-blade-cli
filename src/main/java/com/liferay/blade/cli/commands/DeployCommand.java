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

package com.liferay.blade.cli.commands;

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Jar;
import com.liferay.blade.cli.FileWatcher;
import com.liferay.blade.cli.GogoTelnetClient;
import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.FileWatcher.Consumer;
import com.liferay.blade.cli.commands.arguments.DeployArgs;
import com.liferay.blade.cli.gradle.GradleExec;
import com.liferay.blade.cli.gradle.GradleTooling;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.dto.BundleDTO;

/**
 * @author Gregory Amerson
 */
public class DeployCommand {

	public static final String DESCRIPTION =
		"Builds and deploys bundles to the Liferay module framework.";

	public DeployCommand(blade blade, DeployArgs options) throws Exception {
		_blade = blade;
		_options = options;
		_host = /*options.host() != null ? options.host() : */"localhost";
		_port = /*options.port() != 0 ? options.port() : */11311;
	}

	public void deploy(GradleExec gradle, Set<Path> outputFiles)
		throws Exception {

		int retcode = gradle.executeGradleCommand("build -x check");

		if (retcode > 0) {
			addError("Gradle jar task failed.");
			return;
		}

		for (Path outputFile : outputFiles) {
			installOrUpdate(outputFile);
		}
	}

	public void deployWatch(
			final GradleExec gradleExec, final Set<Path> outputFiles)
		throws Exception {

		deploy(gradleExec, outputFiles);

		Exception[] exception = new Exception[1];
		CountDownLatch latch = new CountDownLatch(1);
		CompletableFuture.runAsync(() -> {
			try {
				gradleExec.executeGradleCommand("build -x check -t");
			}
			catch (Exception e) {
				exception[0] = e;
			}
			latch.countDown();
		});
		latch.await();

		Consumer<Path> consumer = (modified) -> {
			if (outputFiles.contains(modified)) {
				_blade.out().println("installOrUpdate " + modified);

				try {
					installOrUpdate(modified);
				} catch (Exception e) {

					_blade.error(e.getMessage());
				}
			}
		};

		new FileWatcher(_blade.getBase(), true, consumer);
	}

	public void execute() throws Exception {
		if (!Util.canConnect(_host, _port)) {
			addError(
				"deploy",
				"Unable to connect to gogo shell on " + _host + ":" + _port);
			return;
		}

		GradleExec gradleExec = new GradleExec(_blade);

		Set<Path> outputFiles = GradleTooling.getOutputFiles(
			_blade.getCacheDir(), _blade.getBase());

		if (_options.isWatch()) {
			deployWatch(gradleExec, outputFiles);
		}
		else {
			deploy(gradleExec, outputFiles);
		}
	}


	private void addError(String msg) {
		_blade.addErrors("deploy", Collections.singleton(msg));
	}

	private void addError(String prefix, String msg) {
		_blade.addErrors(prefix, Collections.singleton(msg));
	}

	private void installOrUpdate(Path outputFile) throws Exception {
		boolean isFragment = false;
		String fragmentHost = null;
		String bsn = null;
		String hostBSN = null;

		try(Jar bundle = new Jar(outputFile.toString())) {
			Manifest manifest = bundle.getManifest();
			Attributes mainAttributes = manifest.getMainAttributes();

			fragmentHost = mainAttributes.getValue("Fragment-Host");

			isFragment = fragmentHost != null;

			bsn = bundle.getBsn();

			if(isFragment) {
				hostBSN =
						new Parameters(fragmentHost).keySet().iterator().next();
			}
		}

		GogoTelnetClient client = new GogoTelnetClient(_host, _port);

		List<BundleDTO> bundles = getBundles(client);

		long hostId = getBundleId(bundles, hostBSN);

		long existingId = getBundleId(bundles,bsn);

		String bundleURL = outputFile.toUri().toASCIIString();

		if (existingId > 0) {
			if (isFragment && hostId > 0) {
				String response =
						client.send("update " + existingId + " " + bundleURL);

				_blade.out().println(response);

				response = client.send("refresh " + hostId);

				_blade.out().println(response);
			}
			else {
				String response = client.send("stop " + existingId);

				_blade.out().println(response);

				response =
						client.send("update " + existingId + " " + bundleURL);

				_blade.out().println(response);

				response = client.send("start " + existingId);

				_blade.out().println(response);
			}

			_blade.out().println("Updated bundle " + existingId);
		}
		else {
			String response = client.send("install " + bundleURL);

			_blade.out().println(response);

			if (isFragment && hostId > 0) {
				response = client.send("refresh " + hostId);

				_blade.out().println(response);
			}
			else {
				existingId = getBundleId(getBundles(client),bsn);

				if(existingId > 1) {
					response = client.send("start " + existingId);
					_blade.out().println(response);
				}
				else {
					_blade.out().println("Error: fail to install "+bsn);
				}
			}
		}

		client.close();
	}

	private long getBundleId(List<BundleDTO> bundles, String bsn)
			throws IOException {
		long existingId = -1;

		if(bundles != null && bundles.size() > 0 ) {
			for (BundleDTO bundle : bundles) {
				if (bundle.symbolicName.equals(bsn)) {
					existingId = bundle.id;
					break;
				}
			}
		}

		return existingId;
	}

	private List<BundleDTO> getBundles(GogoTelnetClient client)
		throws IOException {

		List<BundleDTO> bundles = new ArrayList<>();

		String output = client.send("lb -s -u");

		String lines[] = output.split("\\r?\\n");

		for (String line : lines) {
			try {
				String[] fields = line.split("\\|");

				//ID|State|Level|Symbolic name
				BundleDTO bundle = new BundleDTO();

				bundle.id = Long.parseLong(fields[0].trim());
				bundle.state = getState(fields[1].trim());
				bundle.symbolicName = fields[3];

				bundles.add(bundle);
			}
			catch (Exception e) {
			}
		}

		return bundles;
	}

	private int getState(String state) {
		String bundleState = state.toUpperCase();

		if ("ACTIVE".equals(bundleState)) {
			return Bundle.ACTIVE;
		}
		else if ("INSTALLED".equals(Bundle.INSTALLED)) {
			return Bundle.INSTALLED;
		}
		else if ("RESOLVED".equals(Bundle.RESOLVED)) {
			return Bundle.RESOLVED;
		}
		else if ("STARTING".equals(Bundle.STARTING)) {
			return Bundle.STARTING;
		}
		else if ("STOPPING".equals(Bundle.STOPPING)) {
			return Bundle.STOPPING;
		}
		else if ("UNINSTALLED".equals(Bundle.UNINSTALLED)) {
			return Bundle.UNINSTALLED;
		}

		return 0;
	}

	private final blade _blade;
	private final String _host;
	private final DeployArgs _options;
	private final int _port;

}