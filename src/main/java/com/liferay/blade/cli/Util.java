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

import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Processor;
import aQute.bnd.osgi.Resource;
import aQute.lib.getopt.Options;
import aQute.lib.io.IO;
import aQute.lib.justif.Justif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Gregory Amerson
 * @author David Truong
 */
public class Util {

	private static final Path work = Paths.get(System.getProperty("user.dir"));
	public static final String APP_SERVER_PARENT_DIR_PROPERTY =
		"app.server.parent.dir";

	public static final String APP_SERVER_TYPE_PROPERTY = "app.server.type";

	public static boolean canConnect(String host, int port) {
		InetSocketAddress address = new InetSocketAddress(
			host, Integer.valueOf(port));
		InetSocketAddress local = new InetSocketAddress(0);

		InputStream in = null;

		try (Socket socket = new Socket()) {
			socket.bind(local);
			socket.connect(address, 3000);
			in = socket.getInputStream();

			return true;
		}
		catch (Exception e) {
		}

		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception e) {
				}
			}
		}

		return false;
	}
	
	public static Path getPath(Path base, String file) {
		Path home = Paths.get(System.getProperty("user.home"));
		if (file.startsWith("~/")) {
			file = file.substring(2);
			if (!file.startsWith("~/")) {
				return getPath(home, file);
			}
		}
		if (file.startsWith("~")) {
			file = file.substring(1);
			return getPath(home.getParent(), file);
		}

		Path p = Paths.get(file);
		if (p.isAbsolute())
			return p;

		if (base == null)
			base = work;

		p = base.toAbsolutePath();

		for (int n; (n = file.indexOf('/')) > 0;) {
			String first = file.substring(0, n);
			file = file.substring(n + 1);
			if (first.equals(".."))
				p = p.getParent();
			else
				p = p.resolve(first);
		}
		if (file.equals(".."))
			return p.getParent();
		return p.resolve(file).toAbsolutePath();
	}

	public static void copy(InputStream in, Path outputDir) throws Exception {
		try (Jar jar = new Jar("dot", in)) {
			for (Entry<String, Resource> e : jar.getResources().entrySet()) {
				String path = e.getKey();

				Resource r = e.getValue();

				File dest = Processor.getFile(outputDir.toFile(), path);

				if ((dest.lastModified() < r.lastModified()) ||
					(r.lastModified() <= 0)) {

					File dp = dest.getParentFile();

					if (!dp.exists() && !dp.mkdirs()) {
						throw new Exception("Could not create directory " + dp);
					}

					IO.copy(r.openInputStream(), dest);
				}
			}
		}
	}

	public static Path findParentFile(
		Path dir, String[] fileNames, boolean checkParents) {
	

		if (dir == null) {
			return null;
		}

		for (String fileName : fileNames) {
			File file = new File(dir.toFile(), fileName);

			if (file.exists()) {
				return dir;
			}
		}

		if (checkParents) {
			File parentFile = dir.toFile().getParentFile();
			if (parentFile == null)
				return null;
			else
				return findParentFile(parentFile.toPath(), fileNames, checkParents);
		}

		return null;
	}

	public static List<Properties> getAppServerProperties(Path dir) {
		Path projectRoot = findParentFile(
			dir, _APP_SERVER_PROPERTIES_FILE_NAMES, true);

		List<Properties> properties = new ArrayList<>();

		for (String fileName : _APP_SERVER_PROPERTIES_FILE_NAMES) {
			Path file = projectRoot.resolve(fileName);

			if (Files.exists(file)) {
				properties.add(getProperties(file));
			}
		}

		return properties;

	}

	public static Properties getGradleProperties(Path dir) {
		Path file = getGradlePropertiesFile(dir);

		return getProperties(file);
	}

	public static Path getGradlePropertiesFile(Path dir) {
		Path gradlePropertiesFile = 
			getWorkspaceDir(dir).resolve(_GRADLE_PROPERTIES_FILE_NAME);

		return gradlePropertiesFile;
	}

	public static Path getGradleWrapper(Path dir) {
		Path gradleRoot = findParentFile(
			dir,
			new String[] {
				_GRADLEW_UNIX_FILE_NAME, _GRADLEW_WINDOWS_FILE_NAME },
			true);

		if (gradleRoot != null) {
			if (isWindows()) {
				return gradleRoot.resolve(_GRADLEW_WINDOWS_FILE_NAME);
			}
			else {
				return gradleRoot.resolve(_GRADLEW_UNIX_FILE_NAME);
			}
		}

		return null;
	}

	public static Properties getProperties(Path file) {
		try (InputStream inputStream = Files.newInputStream(file)) {
			Properties properties = new Properties();

			properties.load(inputStream);

			return properties;
		}
		catch (Exception e) {
			return null;
		}
	}

	public static Path getWorkspaceDir(blade blade) {
		return getWorkspaceDir(blade.getBase());
	}

	public static Path getWorkspaceDir(Path dir) {
		return findParentFile(
			dir,
			new String[] {
				_SETTINGS_GRADLE_FILE_NAME, _GRADLE_PROPERTIES_FILE_NAME
			},
			true);
	}

	public static boolean hasGradleWrapper(Path dir) {
		if (Files.exists(dir.resolve("gradlew")) &&
			Files.exists(dir.resolve("gradlew.bat"))) {
			return true;
		}
		else {
			Path parent = dir.getParent();

			if (parent != null && Files.exists(parent)) {
				return hasGradleWrapper(parent);
			}
		}

		return false;
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	public static boolean isWorkspace(blade blade) {
		return isWorkspace(blade.getBase());
	}

	public static boolean isWorkspace(Path dir) {
		
		File workspaceDir = getWorkspaceDir(dir).toFile();

		File gradleFile = new File(workspaceDir, _SETTINGS_GRADLE_FILE_NAME);

		if (!gradleFile.exists()) {
			return false;
		}

		try {
			String script = read(gradleFile.toPath());

			Matcher matcher = Workspace.PATTERN_WORKSPACE_PLUGIN.matcher(
				script);

			if (matcher.find()) {
				return true;
			}
			else {
				//For workspace plugin < 1.0.5

				gradleFile = new File(workspaceDir, _BUILD_GRADLE_FILE_NAME);

				script = read(gradleFile.toPath());

				matcher = Workspace.PATTERN_WORKSPACE_PLUGIN.matcher(script);

				return matcher.find();
			}
		}
		catch (Exception e) {
			return false;
		}
	}

	public static void printHelp(
			blade blade, Options options, String cmd,
			Class< ? extends Options> optionClass)
		throws Exception {

		Justif j = new Justif();

		try (Formatter f = j.formatter()) {
			options._command().help(f, null, cmd, optionClass);

			j.wrap();

			blade.err().println(f);
		}
	}

	public static String read(Path file) throws IOException {
		return new String(Files.readAllBytes(file));
	}

	public static CompletableFuture<Void> readProcessStream(
		final InputStream is, final PrintStream ps) {

		return CompletableFuture.runAsync(() -> {
			try (InputStreamReader isr = new InputStreamReader(is);
				 BufferedReader br = new BufferedReader(isr)) {

				br.lines().forEach(ps::println);

				is.close();
			}

			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		});
	}

	public static void setShell(ProcessBuilder processBuilder, String cmd) {
		Map<String, String> env = processBuilder.environment();

		List<String> commands = new ArrayList<>();

		if (Util.isWindows()) {
			commands.add("cmd.exe");
			commands.add("/c");
		}
		else {
			env.put("PATH", env.get("PATH") + ":/usr/local/bin");

			commands.add("sh");
			commands.add("-c");
		}

		commands.add(cmd);

		processBuilder.command(commands);
	}

	public static Process startProcess(blade blade, String command)
		throws Exception {

		return startProcess(blade, command, blade.getBase(), null, true);
	}

	public static Process startProcess(
			blade blade, String command, Path dir, boolean inheritIO)
		throws Exception {

		return startProcess(blade, command, dir, null, inheritIO);
	}

	public static Process startProcess(
			blade blade, String command, Path dir,
			Map<String, String> environment)
		throws Exception {

		return startProcess(blade, command, dir, environment, true);
	}

	public static Process startProcess(
			blade blade, String command, Path dir,
			Map<String, String> environment, boolean inheritIO)
		throws Exception {

		ProcessBuilder processBuilder = new ProcessBuilder();

		Map<String, String> env = processBuilder.environment();

		if (environment != null) {
			env.putAll(environment);
		}

		if ((dir != null) && Files.exists(dir)) {
			processBuilder.directory(dir.toFile());
		}

		setShell(processBuilder, command);

		if(inheritIO) {
			processBuilder.inheritIO();
		}

		Process process = processBuilder.start();

		if (!inheritIO) {
			readProcessStream(process.getInputStream(), blade.out());
			readProcessStream(process.getErrorStream(), blade.err());
		}

		process.getOutputStream().close();

		return process;
	}

	public static void unzip(Path srcFile, Path destDir) throws IOException {
		unzip(srcFile, destDir, null);
	}

	public static void unzip(Path srcFile, Path destDir, String entryToStart)
		throws IOException {
		try (final ZipFile zip = new ZipFile(srcFile.toFile())) {
			final Enumeration<? extends ZipEntry> entries = zip.entries();

			boolean foundStartEntry = entryToStart == null;

			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();

				String entryName = entry.getName();

				if (!foundStartEntry) {
					foundStartEntry = entryToStart.equals(entryName);
					continue;
				}

				if (entry.isDirectory() ||
					((entryToStart != null) &&
					 !entryName.startsWith(entryToStart))) {

					continue;
				}

				if (entryToStart != null) {
					entryName = entryName.replaceFirst(entryToStart, "");
				}

				final File f = new File(destDir.toFile(), entryName);

				if (f.exists()) {
					IO.delete(f);

					if (f.exists()) {
						throw new IOException(
							"Could not delete " + f.getAbsolutePath());
					}
				}

				final File dir = f.getParentFile();

				if (!dir.exists() && !dir.mkdirs()) {
					final String msg = "Could not create dir: " + dir.getPath();
					throw new IOException(msg);
				}

				try (final InputStream in = zip.getInputStream(entry);
					 final FileOutputStream out = new FileOutputStream(f)) {

					final byte[] bytes = new byte[1024];
					int count = in.read(bytes);

					while (count != -1) {
						out.write(bytes, 0, count);
						count = in.read(bytes);
					}

					out.flush();
				}

			}
		}
	}

	private static final String[] _APP_SERVER_PROPERTIES_FILE_NAMES = {
		"app.server." + System.getProperty("user.name") + ".properties",
		"app.server." + System.getenv("COMPUTERNAME") + ".properties",
		"app.server." + System.getenv("HOST") + ".properties",
		"app.server." + System.getenv("HOSTNAME") + ".properties",
		"app.server.properties",
		"build." + System.getProperty("user.name") + ".properties",
		"build." + System.getenv("COMPUTERNAME") + ".properties",
		"build." + System.getenv("HOST") + ".properties",
		"build." + System.getenv("HOSTNAME") + ".properties",
		"build.properties"
	};

	private static final String _BUILD_GRADLE_FILE_NAME = "build.gradle";

	private static final String _GRADLE_PROPERTIES_FILE_NAME =
		"gradle.properties";

	private static final String _GRADLEW_UNIX_FILE_NAME = "gradlew";

	private static final String _GRADLEW_WINDOWS_FILE_NAME = "gradlew.bat";

	private static final String _SETTINGS_GRADLE_FILE_NAME = "settings.gradle";

}