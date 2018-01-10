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

package com.liferay.blade.cli.commands;

import com.liferay.blade.cli.blade;
import com.liferay.blade.cli.commands.arguments.UpgradePropsArgs;
import com.liferay.properties.locator.PropertiesLocator;
import com.liferay.properties.locator.PropertiesLocatorArgs;

import java.io.File;
import java.util.Collections;

import aQute.lib.getopt.Description;
import aQute.lib.getopt.Options;
import aQute.lib.justif.Justif;

/**
 * @author Gregory Amerson
 */
public class UpgradePropsCommand {

	public static final String DESCRIPTION =
		"Helps to upgrade portal properties from Liferay server 6.x to 7.x versions";

	public UpgradePropsCommand(blade blade, UpgradePropsArgs options)
		throws Exception {

		File bundleDir = options.getBundleDir();
		File propertiesFile = options.getPropertiesFile();

		if (bundleDir == null || propertiesFile == null) {
			blade.addErrors("upgradeProps", Collections.singleton("bundleDir and propertiesFile options both required."));
			//options._command().help(new Justif().formatter(), blade);
			// TODO: What to do here?
			return;
		}

		PropertiesLocatorArgs args = new PropertiesLocatorArgs();

		args.setBundleDir(options.getBundleDir());
		args.setOutputFile(options.getOutputFile());
		args.setPropertiesFile(options.getPropertiesFile());

		new PropertiesLocator(args);
	}

}