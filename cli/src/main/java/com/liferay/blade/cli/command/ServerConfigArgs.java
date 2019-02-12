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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * @author Christopher Bryan Boyd
 */
@Parameters(commandDescription = "Configure the application server Liferay project", commandNames = "server config")
public class ServerConfigArgs extends BaseArgs {

	public int getDebugPort() {
		return _debugPort;
	}

	public Boolean isDebug() {
		return _debug;
	}

	public boolean isSuspend() {
		return _suspend;
	}

	public void setDebug(Boolean debug) {
		_debug = debug;
	}

	@Parameter(description = "Start server in debug mode", names = {"-d", "--debug"})
	private boolean _debug = false;

	@Parameter(description = "Debug port", names = {"-p", "--port"})
	private int _debugPort = -1;

	@Parameter(
		description = "When in debug mode, suspend the started server until the debugger is connected",
		names = {"-s", "--suspend"}
	)
	private boolean _suspend = false;

}