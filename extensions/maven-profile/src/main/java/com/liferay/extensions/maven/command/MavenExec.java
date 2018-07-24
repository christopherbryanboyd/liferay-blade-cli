package com.liferay.extensions.maven.command;


import java.io.File;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.util.BladeUtil;

/**
 * @author Christopher Bryan Boyd
 */
public class MavenExec {
	public MavenExec(BladeCLI blade) {
		_blade = blade;

		File mavenw = BladeUtil.getMavenWrapper(blade.getBase());

		if (mavenw != null) {
			try {
				_executable = mavenw.getCanonicalPath();
			}
			catch (Exception e) {
				blade.out("Could not find maven wrapper, using maven");

				_executable = "mvn";
			}
		}
		else {
			blade.out("Could not find maven wrapper, maven gradle");

			_executable = "mvn";
		}
	}

	public int executeMavenCommand(String cmd) throws Exception {
		Process process = BladeUtil.startProcess(_blade, "\"" + _executable + "\" " + cmd);

		return process.waitFor();
	}
	
	public Process executeMavenCommandAsync(String cmd) throws Exception {
		return BladeUtil.startProcessAsync(_blade, "\"" + _executable + "\" " + cmd);
	}

	private BladeCLI _blade;
	private String _executable;
}

