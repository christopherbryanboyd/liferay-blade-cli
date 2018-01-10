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

import aQute.lib.getopt.Description;
import aQute.lib.getopt.Options;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.JCommander.Builder;
import com.liferay.blade.cli.commands.ConvertCommand;
import com.liferay.blade.cli.commands.CreateCommand;
import com.liferay.blade.cli.commands.DeployCommand;
import com.liferay.blade.cli.commands.GradleCommand;
import com.liferay.blade.cli.commands.InitCommand;
import com.liferay.blade.cli.commands.InstallCommand;
import com.liferay.blade.cli.commands.OpenCommand;
import com.liferay.blade.cli.commands.OutputsCommand;
import com.liferay.blade.cli.commands.SamplesCommand;
import com.liferay.blade.cli.commands.ServerStartCommand;
import com.liferay.blade.cli.commands.ServerStopCommand;
import com.liferay.blade.cli.commands.ShellCommand;
import com.liferay.blade.cli.commands.UpdateCommand;
import com.liferay.blade.cli.commands.UpgradePropsCommand;
import com.liferay.blade.cli.commands.arguments.ConvertArgs;
import com.liferay.blade.cli.commands.arguments.CreateArgs;
import com.liferay.blade.cli.commands.arguments.DeployArgs;
import com.liferay.blade.cli.commands.arguments.GradleArgs;
import com.liferay.blade.cli.commands.arguments.InitArgs;
import com.liferay.blade.cli.commands.arguments.InstallArgs;
import com.liferay.blade.cli.commands.arguments.OpenArgs;
import com.liferay.blade.cli.commands.arguments.OutputsArgs;
import com.liferay.blade.cli.commands.arguments.SamplesArgs;
import com.liferay.blade.cli.commands.arguments.ServerStartArgs;
import com.liferay.blade.cli.commands.arguments.ServerStopArgs;
import com.liferay.blade.cli.commands.arguments.ShellArgs;
import com.liferay.blade.cli.commands.arguments.UpdateArgs;
import com.liferay.blade.cli.commands.arguments.UpgradePropsArgs;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;

/**
 * @author Gregory Amerson
 * @author David Truong
 */

public class blade implements Runnable {

	public void run(String[] args) {
		
		List<Object> argsList = Arrays.asList(
				new CreateArgs(),
				new ConvertArgs(),
				new DeployArgs(), 
				new GradleArgs(),
				new InitArgs(),
				new InstallArgs(),
				new OpenArgs(),
				new OutputsArgs(),
				new SamplesArgs(),
				new ServerStartArgs(),
				new ServerStopArgs(),
				new ShellArgs(),
				new UpdateArgs(),
				new UpgradePropsArgs());
		Builder builder=
				JCommander.newBuilder();
		 	for (Object o : argsList) {
		 		builder.addCommand(o);
		 	}
		 JCommander commander =	builder
		  .addObject(_bladeArgs)
		  .build();
		 commander
		  .parse(args);
		 	
		 String command = commander.getParsedCommand();
		 Object commandArgs = commander.getCommands().get(command).getObjects().get(0);
		 
		_command = command;
		_commandArgs = commandArgs;
		
		run();
	}
	
	public void _create(CreateArgs options) throws Exception {
		new CreateCommand(this, options).execute();
	}

	public void _deploy(DeployArgs options) throws Exception {
		new DeployCommand(this, options).execute();
	}

	public void _gw(GradleArgs options) throws Exception {
		new GradleCommand(this, options).execute();
	}
	
	public void error(String error) {
		err().println(error);
	}
	
	public void addErrors(String prefix, Collection<String> data) {
		err().println("Error: " + prefix);
		data.forEach(err()::println);
	}
	
	public void error(String string, String name, String message) {
		err().println(string + " [" + name + "]");
		err().println(message);
		
	}
	
	public Path getBase() {
		return Paths.get(_bladeArgs.getBase());
	}
	
	public void _help(Options options) throws Exception {
		options._help();
	}

	public void _init(InitArgs options) throws Exception {
		new InitCommand(this, options).execute();
	}

	public void _install(InstallArgs options) throws Exception {
		new InstallCommand(this, options).execute();
	}

	public void _open(OpenArgs options) throws Exception {
		new OpenCommand(this, options).execute();
	}

	public BladeArgs getBladeArgs() {
		return _bladeArgs;
	}
	public void _outputs(OutputsArgs options) throws Exception {
		new OutputsCommand(this, options).execute();
	}

	public void _samples(SamplesArgs options) throws Exception {
		new SamplesCommand(this, options).execute();
	}

	public void _serverStart(ServerStartArgs options) throws Exception {
		new ServerStartCommand(this, options).execute();
	}
	
	public void _serverStop(ServerStopArgs options) throws Exception {
		new ServerStopCommand(this, options).execute();	
	}

	public void _sh(ShellArgs options) throws Exception {
		new ShellCommand(this, options).execute();
	}

	public void _update(UpdateArgs options) throws Exception {
		new UpdateCommand(this, options).execute();
	}

	public void _upgradeProps(UpgradePropsArgs options) throws Exception {
		new UpgradePropsCommand(this, options);
	}

	public void _convert(ConvertArgs options) throws Exception {
		new ConvertCommand(this, options).execute();
	}

	@Description("Show version information about blade")
	public void _version(Options options) throws IOException {
		Enumeration<URL> e =
			getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");

		while (e.hasMoreElements()) {
			URL u = e.nextElement();
			Manifest m = new Manifest(u.openStream());
			String bsn =
				m.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);

			if ((bsn != null) && bsn.equals("com.liferay.blade.cli")) {
				Attributes attrs = m.getMainAttributes();
				out.printf("%s\n", attrs.getValue(Constants.BUNDLE_VERSION));
				return;
			}
		}

		error("Could not locate version");
	}

	public PrintStream err() {
		return err;
	}

	public Path getBundleDir() {
		String userHome = System.getProperty("user.home");

		return Paths.get(userHome, ".liferay", "bundles");
	}

	public Path getCacheDir() {
		String userHome = System.getProperty("user.home");

		return Paths.get(userHome, ".blade", "cache");
	}

	public PrintStream out() {
		return out;
	}

	@Override
	public void run() {
		try {
			switch (_command) {
			case "create": {
				_create((CreateArgs) _commandArgs);
			}
				break;
			case "convert": {
				_convert((ConvertArgs) _commandArgs);
			}
				break;
			case "deploy": {
				_deploy((DeployArgs) _commandArgs);
			}
				break;
			case "gw": {
				_gw((GradleArgs) _commandArgs);
			}
				break;
			default:
			case "help": {
				// TODO: Print help here?
			}
				break;
			case "init": {
				_init((InitArgs) _commandArgs);
			}
				break;
			case "install": {
				_install((InstallArgs) _commandArgs);
			}
			case "open": {
				_open((OpenArgs) _commandArgs);
			}
				break;
			case "outputs": {
				_outputs((OutputsArgs) _commandArgs);
			}
				break;
			case "samples": {
				_samples((SamplesArgs) _commandArgs);
			}
				break;
			case "server start": {
				_serverStart((ServerStartArgs) _commandArgs);

			}
				break;
			case "server stop": {
				_serverStop((ServerStopArgs) _commandArgs);
			}
				break;
			case "sh": {
				_sh((ShellArgs) _commandArgs);
			}
				break;
			case "update": {
				_update((UpdateArgs) _commandArgs);
			}
				break;
			case "upgradeProps": {
				_upgradeProps((UpgradePropsArgs) _commandArgs);
			}
				break;
			case "version": {
				// TODO: What is this supposed to do?
			}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void trace(String s, Object... args) {
		if (_bladeArgs.isTrace() && (tracer != null)) {
			tracer.format("# " + s + "%n", args);
			tracer.flush();
		}
	}
	private String _command;
	private BladeArgs _bladeArgs = new BladeArgs();
	private Object _commandArgs;
	private final Formatter tracer = new Formatter(System.out);
	private PrintStream out = System.out;
	private PrintStream err = System.err;
}