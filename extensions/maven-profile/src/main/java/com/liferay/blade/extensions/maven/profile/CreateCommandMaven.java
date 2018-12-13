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

package com.liferay.blade.extensions.maven.profile;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.command.BaseArgs;
import com.liferay.blade.cli.command.BladeProfile;
import com.liferay.blade.cli.command.CreateArgs;
import com.liferay.blade.cli.command.CreateCommand;
import com.liferay.blade.extensions.maven.profile.internal.MavenUtil;
import com.liferay.project.templates.ProjectTemplatesArgs;

import java.io.File;
import java.io.IOException;

import java.util.Properties;

/**
 * @author Gregory Amerson
 * @author David Truong
 * @author Christopher Bryan Boyd
 * @author Charles Wu
 */
@BladeProfile("maven")
public class CreateCommandMaven extends CreateCommand {

	public CreateCommandMaven() {
	}

	public CreateCommandMaven(BladeCLI bladeCLI) {
		super(bladeCLI);
	}

	@Override
	public void execute() throws Exception {
		CreateArgs createArgs = getArgs();

		createArgs.setBuild("maven");

		super.execute();
	}

	@Override
	protected ProjectTemplatesArgs getProjectTemplateArgs(CreateArgs createArgs, BladeCLI bladeCLI, String template,
			String name, File dir) throws IOException {

		ProjectTemplatesArgs projectTemplatesArgs = new ProjectTemplatesArgs();

		projectTemplatesArgs.setGradle(false);
		projectTemplatesArgs.setMaven(true);

		projectTemplatesArgs.setClassName(createArgs.getClassname());
		projectTemplatesArgs.setContributorType(createArgs.getContributorType());
		projectTemplatesArgs.setDestinationDir(dir.getAbsoluteFile());

		projectTemplatesArgs.setDependencyManagementEnabled(false);
		projectTemplatesArgs.setHostBundleSymbolicName(createArgs.getHostBundleBSN());
		projectTemplatesArgs.setLiferayVersion(getLiferayVersion(bladeCLI, createArgs));
		projectTemplatesArgs.setOriginalModuleName(createArgs.getOriginalModuleName());
		projectTemplatesArgs.setOriginalModuleVersion(createArgs.getOriginalModuleVersion());
		projectTemplatesArgs.setHostBundleVersion(createArgs.getHostBundleVersion());
		projectTemplatesArgs.setName(name);
		projectTemplatesArgs.setPackageName(createArgs.getPackageName());
		projectTemplatesArgs.setService(createArgs.getService());
		projectTemplatesArgs.setTemplate(template);

		return projectTemplatesArgs;
	}

	@Override
	protected Properties getWorkspaceProperties() {
		BaseArgs baseArgs = getArgs();

		File baseDir = new File(baseArgs.getBase());

		return MavenUtil.getMavenProperties(baseDir);
	}

}