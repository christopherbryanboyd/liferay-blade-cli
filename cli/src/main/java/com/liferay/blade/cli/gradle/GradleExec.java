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

package com.liferay.blade.cli.gradle;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.util.BladeUtil;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;

/**
 * @author David Truong
 */
public class GradleExec {

	public GradleExec(BladeCLI blade) {
		_blade = blade;
	}

	public ProcessResult executeGradleCommand(String cmd) throws Exception {
		return executeGradleCommand(cmd, _blade.getBase());
	}

	public ProcessResult executeGradleCommand(String cmd, File dir) throws Exception {
		
		String executable = getGradleExecutable(dir);
		
		Process process = BladeUtil.startProcess(_blade, "\"" + executable + "\" " + cmd, dir, true);

		int returnCode = process.waitFor();
		
		String output = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
		
		return new ProcessResult(returnCode, output);
	}
	
	private String getGradleExecutable(File dir) throws NoSuchElementException {
		File gradlew = null;
		String executable = null;
				
		gradlew = BladeUtil.getGradleWrapper(dir);
		
		if (gradlew == null) {
			gradlew = BladeUtil.getGradleWrapper(_blade.getBase());
		}

		if (gradlew != null && gradlew.exists()) {
			try {
				executable = gradlew.getCanonicalPath();
			}
			catch (Throwable th) {
				executable = "gradle";
			}
		}
		else {
			executable = "gradle";
		}
		
		if ("gradle".equals(executable)) {
			if (isGradleInstalled()) {
				_blade.out("Could not find gradle wrapper, using gradle");
			} else {				
				throw new NoSuchElementException("Gradle Wrapper not found, and Gradle is not installed.");
			}
		} else {
			if (gradlew != null) {
				if (!gradlew.canExecute()) {
					gradlew.setExecutable(true);
				}
			}
		}
		
		return executable;
	}
	
	public static boolean isGradleInstalled() {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			
			if (BladeUtil.isWindows()) {
			    builder.command("cmd.exe", "/c", "gradle -version");
			} else {
			    builder.command("sh", "-c", "gradle -version");
			}
			
			builder.directory(new File(System.getProperty("user.home")));
			
			Process process = builder.start();
			
			InputStream inputStream = process.getInputStream();
			
			InputStream errorStream = process.getErrorStream();
			
			StringBuilder output = new StringBuilder();
			
			String stdOutString = IOUtils.toString(inputStream, Charset.defaultCharset());
			String stdErrString = IOUtils.toString(errorStream, Charset.defaultCharset());
			
			output.append(stdOutString);
			output.append(stdErrString);
			
			int code = process.waitFor();
			
			if (code != 0) {
				return false;
			} 
			else {
				
				String result = output.toString();
	
				return result != null && result.contains("version");
			}
		} 
		catch (Exception e) {
			return false;
		}
		
	}
	
	private BladeCLI _blade;
}