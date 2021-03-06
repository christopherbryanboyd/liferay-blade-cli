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

import com.liferay.blade.cli.command.validator.ParameterPossibleValues;
import com.liferay.blade.cli.command.validator.TemplateNameValidator;

import java.io.File;

/**
 * @author Gregory Amerson
 * @author Charles Wu
 * @author Simon Jiang
 */
@Parameters(
	commandDescription = "Creates a new Liferay module project from several available templates.",
	commandNames = "create"
)
public class CreateArgs extends BaseArgs {

	public String getClassname() {
		return _classname;
	}

	public String getContributorType() {
		return _contributorType;
	}

	public String getDependencyInjector() {
		return _dependencyInjector;
	}

	public File getDir() {
		return _dir;
	}

	public String getFramework() {
		return _framework;
	}

	public String getFrameworkDependencies() {
		return _frameworkDependencies;
	}

	public String getHostBundleBSN() {
		return _hostBundleBSN;
	}

	public String getHostBundleVersion() {
		return _hostBundleVersion;
	}

	public String getLiferayVersion() {
		return _liferayVersion;
	}

	public String getName() {
		return _name;
	}

	public String getOriginalModuleName() {
		return _originalModuleName;
	}

	public String getOriginalModuleVersion() {
		return _originalModuleVersion;
	}

	public String getPackageName() {
		return _packageName;
	}

	public String getService() {
		return _service;
	}

	public String getTemplate() {
		return _template;
	}

	public String getViewType() {
		return _viewType;
	}

	public boolean isListTemplates() {
		return _listTemplates;
	}

	public void setClassName(String className) {
		_classname = className;
	}

	public void setDependencyInjector(String dependencyInjector) {
		_dependencyInjector = dependencyInjector;
	}

	public void setDir(File dir) {
		_dir = dir;
	}

	public void setFramework(String framework) {
		_framework = framework;
	}

	public void setFrameworkDependencies(String frameworkDependencies) {
		_frameworkDependencies = frameworkDependencies;
	}

	public void setHostBundleBSN(String hostBundleBSN) {
		_hostBundleBSN = hostBundleBSN;
	}

	public void setHostBundleVersion(String hostBundleVersion) {
		_hostBundleVersion = hostBundleVersion;
	}

	public void setLiferayVersion(String liferayVersion) {
		_liferayVersion = liferayVersion;
	}

	public void setListTemplates(boolean listTemplates) {
		_listTemplates = listTemplates;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setPackageName(String packageName) {
		_packageName = packageName;
	}

	public void setService(String service) {
		_service = service;
	}

	public void setTemplate(String template) {
		_template = template;
	}

	public void setViewType(String viewType) {
		_viewType = viewType;
	}

	@Parameter(
		description = "If a class is generated in the project, provide the name of the class to be generated. If not provided defaults to project name.",
		names = {"-c", "--classname"}
	)
	private String _classname;

	@Parameter(
		description = "Used to identify your module as a Theme Contributor. Also, used to add the Liferay-Theme-Contributor-Type and Web-ContextPath bundle headers.",
		names = {"-C", "--contributor-type"}
	)
	private String _contributorType;

	@Parameter(
		description = "For Service Builder projects, specify the preferred dependency injection method (ds | spring). Default is DS",
		names = "--dependency-injector"
	)
	private String _dependencyInjector = "ds";

	@Parameter(description = "The directory where to create the new project.", names = {"-d", "--dir"})
	private File _dir;

	@Parameter(description = "The name of the framework to use in the generated project.", names = "--framework")
	private String _framework;

	@Parameter(
		description = "The way that the framework dependencies will be configured.", names = "--framework-dependencies"
	)
	private String _frameworkDependencies = "embedded";

	@Parameter(
		description = "If a new jsp hook fragment needs to be created, provide the name of the host bundle symbolic name. Required for \"-t fragment\".",
		names = {"-h", "--host-bundle-bsn"}
	)
	private String _hostBundleBSN;

	@Parameter(
		description = "If a new jsp hook fragment needs to be created, provide the name of the host bundle version. Required for \"-t fragment\".",
		names = {"-H", "--host-bundle-version"}
	)
	private String _hostBundleVersion;

	@Parameter(
		description = "The version of Liferay to target when creating the project. Available options are 7.0, 7.1, 7.2.",
		names = {"-v", "--liferay-version"}
	)
	private String _liferayVersion;

	@Parameter(
		description = "Prints a list of available project templates", hidden = true, names = {"-l", "--list-templates"}
	)
	private boolean _listTemplates;

	@Parameter(description = "The project name", required = true)
	private String _name;

	@Parameter(
		description = "Sets the name of the original module when creating a project with modules-ext template.",
		names = {"-m", "--original-module-name"}
	)
	private String _originalModuleName;

	@Parameter(
		description = "Sets the version of the original module when creating a project with modules-ext template.",
		names = {"-M", "--original-module-version"}
	)
	private String _originalModuleVersion;

	@Parameter(description = "The Java package to use when generating Java source.", names = {"-p", "--package-name"})
	private String _packageName;

	@Parameter(
		description = "If a new DS component needs to be created, provides the name of the service to be implemented.",
		names = {"-s", "--service"}
	)
	private String _service;

	@Parameter(
		description = "The project template to use when creating the project. To see the list of templates available use blade create <-l | --list-templates>",
		names = {"--template", "-t"}, required = true, validateValueWith = TemplateNameValidator.class
	)
	@ParameterPossibleValues(TemplateNameValidator.class)
	private String _template;

	@Parameter(
		description = "Choose the view technology that will be used in the generated project.", names = "--view-type"
	)
	private String _viewType;

}