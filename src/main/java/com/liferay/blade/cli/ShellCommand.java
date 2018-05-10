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

import com.liferay.blade.cli.util.GogoShellClient;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Gregory Amerson
 */
public class ShellCommand extends BaseCommand<ShellCommandArgs> {

	public ShellCommand(BladeCLI blade, ShellCommandArgs options) throws Exception {
		super(blade, options);

		_host = options.getHost() != null ? options.getHost() : "localhost";
		_port = options.getPort() != 0 ? options.getPort() : 11311;
	}

	public void execute() throws Exception {
		if (!Util.canConnect(_host, _port)) {
			_addError("sh", "Unable to connect to gogo shell on " + _host + ":" + _port);

			return;
		}

		String gogoCommand = StringUtils.join(_args.getArgs(), " ");

		_executeCommand(gogoCommand);
	}

	@Override
	public Class<ShellCommandArgs> getArgsClass() {
		return ShellCommandArgs.class;
	}

	private void _addError(String prefix, String msg) {
		_blade.addErrors(prefix, Collections.singleton(msg));
	}

	private void _executeCommand(String cmd) throws Exception {
		try (final GogoShellClient client = new GogoShellClient(_host, _port)) {
			String response = client.send(cmd);

			_blade.out(response);
		}
	}

	private final String _host;
	private final int _port;

}