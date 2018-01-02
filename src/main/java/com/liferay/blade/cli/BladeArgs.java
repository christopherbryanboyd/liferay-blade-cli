package com.liferay.blade.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Options valid for all commands. Must be given before sub command")
public class BladeArgs {

	@Parameter(
			names = {"-t", "--trace"},
			description ="Print exception stack traces when they occur.")
		private boolean trace;


		public boolean isTrace() {
		return trace;
	}

	public String getBase() {
		return base;
	}

	public String getFailok() {
		return failok;
	}

		@Parameter(
			names = {"-b", "--base"},
			description ="Specify a new base directory (default working directory).")
		private String base = ".";

		@Parameter(
			names = {"-f", "--failok"},
			description ="Do not return error status for error that match this given regular expression.")
		private String failok;

	
}
