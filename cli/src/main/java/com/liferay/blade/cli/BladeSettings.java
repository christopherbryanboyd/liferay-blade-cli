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

import com.liferay.blade.cli.util.Prompter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class BladeSettings {

	public BladeSettings(BladeCLI bladeCli, File bladeUserHomeDir, File... settingsFiles) throws IOException {
		_bladeCLI = bladeCli;
		
		_settingsFiles = Arrays.asList(settingsFiles);

		if (_settingsFiles.stream().anyMatch(File::exists)) {
			load();
		}
	}

	public String getLiferayVersionDefault() {
		if (_properties.getProperty(_LIFERAY_VERSION_DEFAULT_KEY) != null) {
			return _properties.getProperty(_LIFERAY_VERSION_DEFAULT_KEY);
		}
		else {
			return "7.1";
		}
	}
	
	public boolean getSubmitUsageStats() {
		return Boolean.valueOf(_properties.getProperty(_SUBMIT_STATS_KEY));
	}

	public String getProfileName() {
		return _properties.getProperty(_PROFILE_NAME);
	}

	public void load() throws IOException {
		for (File file : _settingsFiles) {
			if (file.exists()) {
				try (FileInputStream fileInputStream = new FileInputStream(file)) {
					_properties.load(fileInputStream);
				}
			}
		}
	}

	public void migrateWorkspaceIfNecessary() throws IOException {
		
		for (File _settingsFile : _settingsFiles) {
			WorkspaceProvider workspaceProvider = _bladeCLI.getWorkspaceProvider(_settingsFile);
	
			if ((workspaceProvider != null) && workspaceProvider.isWorkspace(_bladeCLI)) {
				File workspaceDirectory = workspaceProvider.getWorkspaceDir(_settingsFile);
	
				File pomFile = new File(workspaceDirectory, "pom.xml");
	
				boolean shouldPrompt = false;
	
				if (pomFile.exists()) {
					if (!_settingsFile.exists()) {
						shouldPrompt = true;
					}
					else {
						String profilePromptDisabled = _properties.getProperty("profile.prompt.disabled", "false");
	
						if (!"true".equals(profilePromptDisabled)) {
							String profileName = getProfileName();
	
							if (!"maven".equals(profileName)) {
								shouldPrompt = true;
							}
						}
					}
				}
	
				if (shouldPrompt) {
					String question =
						"WARNING: blade commands will not function properly in a Maven workspace unless the blade " +
							"profile is set to \"maven\". Should the settings for this workspace be updated?";
	
					if (Prompter.confirm(question, true)) {
						setProfileName("maven");
						save();
					}
					else {
						question = "Should blade remember this setting for this workspace?";
	
						if (Prompter.confirm(question, true)) {
							_properties.setProperty("profile.prompt.disabled", "true");
							save();
						}
					}
				}
			}
		}
	}

	public void save() throws IOException {
		for (File _settingsFile : _settingsFiles) {
			if (!_settingsFile.exists()) {
				File parentDir = _settingsFile.getParentFile();
	
				parentDir.mkdirs();
			}
			WorkspaceProvider workspaceProvider = _bladeCLI.getWorkspaceProvider(_settingsFile);
			
			if ((workspaceProvider != null) && workspaceProvider.isWorkspace(_bladeCLI)) {

				Properties properties = new Properties();
				String propertyValue = _properties.getProperty(_LIFERAY_VERSION_DEFAULT_KEY);
				properties.put(_LIFERAY_VERSION_DEFAULT_KEY, propertyValue);
				propertyValue = _properties.getProperty(_PROFILE_NAME);
				properties.put(_PROFILE_NAME, propertyValue);
				try (FileOutputStream out = new FileOutputStream(_settingsFile)) {
					properties.store(out, null);
				}
			}
			else {
				Properties properties = new Properties();
				
				String submitUsageStats = getSubmitUsageStats();
				
				properties.setProperty(_SUBMIT_STATS_KEY, submitUsageStats);
				
				try (FileOutputStream out = new FileOutputStream(_settingsFile)) {
					properties.store(out, null);
				}
			}
		}
	}

	public void setLiferayVersionDefault(String liferayVersion) {
		_properties.setProperty(_LIFERAY_VERSION_DEFAULT_KEY, liferayVersion);
	}

	public void setProfileName(String profileName) {
		_properties.setProperty(_PROFILE_NAME, profileName);
	}
	
	public void setSubmitUsageStats(boolean submitStats) {
		_properties.setProperty(_SUBMIT_STATS_KEY, String.valueOf(submitStats));
	}
	private static final String _PROFILE_NAME = "profile.name";
	private static final String _SUBMIT_STATS_KEY = "submit.stats";

	private static final String _LIFERAY_VERSION_DEFAULT_KEY = "liferay.version.default";
	private final BladeCLI _bladeCLI;
	private final File _bladeUserHomeDir;
	private final Properties _properties = new Properties();
	private final Collection<File> _settingsFiles;

}