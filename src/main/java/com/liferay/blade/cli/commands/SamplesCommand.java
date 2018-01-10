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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.liferay.blade.cli.Util;
import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.SamplesArgs;

/**
 * @author David Truong
 */
public class SamplesCommand {

	public static final String DESCRIPTION = "Generate a sample project";

	public SamplesCommand(blade blade, SamplesArgs options)
		throws Exception {

		_blade = blade;
		_options = options;
	}

	public void execute() throws Exception {

		final String sampleName = _options.getSampleName();

		if (downloadBladeRepoIfNeeded()) {
			extractBladeRepo();
		}

		if (sampleName == null) {
			listSamples();
		}
		else {
			copySample(sampleName);
		}
	}

	private void copySample(String sampleName) throws Exception {
		final Path workDir; 
		if (_options.getDir() == null) {
			workDir = _blade.getBase();			
		} else {
			workDir = _options.getDir().toPath();
		}

		Path bladeRepo = _blade.getCacheDir().resolve(_BLADE_REPO_NAME);
		
		Predicate<Path> isCorrectSample = (path) -> Files.isDirectory(path) && path.getFileName().toString().equals(sampleName);

		Path gradleSamples = bladeRepo.resolve("gradle");

		Files.find(gradleSamples, 999, (path, bfa) -> isCorrectSample.test(path)).forEach((path) -> copySampleFiles(workDir, path));
	}

	private void copySampleFiles(final Path workDir, Path path) {
		Path fileName = path.getFileName();
		
		Path dest = workDir.resolve(fileName);

		try {
			FileUtils.copyDirectory(path.toFile(), dest.toFile());
			
			updateBuildGradle(dest);

			if (!Util.hasGradleWrapper(dest)) {
				addGradleWrapper(dest);
			}
			
		} catch (Exception e) {
			_blade.error(e.getMessage());
		}
	}

	private void addGradleWrapper(Path dest) throws Exception {
		InputStream in = SamplesCommand.class.getResourceAsStream("/wrapper.zip");

		Util.copy(in, dest);

		dest.resolve("gradlew").toFile().setExecutable(true);
	}

	private String deindent(String s) {
		return s.replaceAll("(?m)^\t", "");
	}

	private boolean downloadBladeRepoIfNeeded() throws Exception {
		Path bladeRepoArchive = 
			_blade.getCacheDir().resolve(_BLADE_REPO_ARCHIVE_NAME);


		long diff = System.currentTimeMillis() - Files.getLastModifiedTime(bladeRepoArchive).toMillis();

		if (Files.notExists(bladeRepoArchive) || (diff > _FILE_EXPIRATION_TIME)) {
			FileUtils.copyURLToFile(new URL(_BLADE_REPO_URL), bladeRepoArchive.toFile());

			return true;
		}

		return false;
	}

	private void extractBladeRepo() throws Exception {
		Path bladeRepoArchive = 
			_blade.getCacheDir().resolve(_BLADE_REPO_ARCHIVE_NAME);

		Util.unzip(bladeRepoArchive, _blade.getCacheDir(), null);
	}

	private void listSamples() {
		Path bladeRepo = _blade.getCacheDir().resolve(_BLADE_REPO_NAME);

		Path gradleSamples = bladeRepo.resolve("gradle");

		List<String> samples;
		try {
			samples = Files.find(gradleSamples, 999, (path, bfa) -> 
				 (Files.isDirectory(path))).map(path -> path.getFileName().toString()).filter(string -> string.startsWith("blade.")).collect(Collectors.toList());
			_blade.out().println(
					"Please provide the sample project name to create, " +
						"e.g. \"blade samples blade.rest\"\n");
				_blade.out().println("Currently available samples:");
				_blade.out().println(
					WordUtils.wrap(StringUtils.join(samples, ", "), 80));
		} catch (IOException e) {
			_blade.error(e.getMessage());
		}

	}

	private String parseGradleScript(
		String script, String section, boolean contentsOnly) {

		int begin = script.indexOf(section + " {");
		int end = begin;
		int count = 0;

		if (contentsOnly) {
			begin += section.length() + 2;
		}

		while (true) {
			char c = script.charAt(end);

			if ((count != 0) && (c == '}')) {
				count--;
			}
			else if (c == '{') {
				count++;
			}

			if ((count == 0) && (c == '}')) {
				if (!contentsOnly) {
					end++;
				}

				break;
			}

			end++;
		}

		String newScript = script.substring(begin, end);

		if (contentsOnly) {
			return deindent(newScript);
		}

		return newScript;
	}

	private String removeGradleSection(String script, String section) {
		int begin = script.indexOf(section + " {");
		int end = begin;
		int count = 0;

		if (begin == -1) {
			return script;
		}

		while (true) {
			char c = script.charAt(end);

			if ((count != 0) && (c == '}')) {
				count--;
			}
			else if (c == '{') {
				count++;
			}

			end++;

			if ((count == 0) && (c == '}')) {
				break;
			}
		}

		return removeGradleSection(
			script.substring(0, begin) + script.substring(end, script.length()),
			section);
	}

	private void updateBuildGradle(Path dir) throws Exception {

		Path bladeRepo = _blade.getCacheDir().resolve(_BLADE_REPO_NAME);

		Path sampleGradleFile = dir.resolve("build.gradle");

		String script = Util.read(sampleGradleFile);

		if (!Util.isWorkspace(dir)) {
			Path parentBuildGradleFile = 
				bladeRepo.resolve(Paths.get("gradle", "build.gradle"));

			String parentBuildScript = parseGradleScript(
				Util.read(parentBuildGradleFile), "buildscript", false);

			String parentSubprojectsScript = parseGradleScript(
				Util.read(parentBuildGradleFile), "subprojects", true);

			parentSubprojectsScript = removeGradleSection(
				parentSubprojectsScript, "buildscript");

			System.out.println(parentSubprojectsScript);

			script = parentBuildScript + parentSubprojectsScript + script;
		}

		Files.write(sampleGradleFile, script.getBytes());
	}

	private static final String _BLADE_REPO_ARCHIVE_NAME =
		"liferay-blade-samples-2.x.zip";

	private static final String _BLADE_REPO_NAME =
		"liferay-blade-samples-2.x";

	private static final String _BLADE_REPO_URL =
		"https://github.com/liferay/liferay-blade-samples/archive/2.x.zip";

	private static final long _FILE_EXPIRATION_TIME = 604800000;

	private final blade _blade;
	private final SamplesArgs _options;

}