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
@Parameters(commandDescription = "Generates the XML profile entry with the given details.", commandNames = "extension createentry", hidden=true)
public class GenerateExtensionXmlArgs extends BaseArgs {

	public String getPathToXml() {
		return _pathToXml;
	}
	
	public String getVersion() {
		return _version;
	}

	public String getAuthor() {
		return _author;
	}
	
	public String getGithubProjectUrl() {
		return _author;
	}

	@Parameter(
		description = "Specify the path to the XML file to contain the new entry.", names = {"-p", "--path"}
	)
	private String _pathToXml;

	@Parameter(
		description = "The version of the new entry.", names = {"-v", "--version"}, required=true
	)
	private String _version;

	@Parameter(
		description = "The author of the new entry.", names = {"-a", "--author"}, required=true
	)
	private String _author;

	@Parameter(
		description = "The github URL link to the project.", names = {"-l", "--link"}, required=true
	)
	private String _githubProjectUrl;
}