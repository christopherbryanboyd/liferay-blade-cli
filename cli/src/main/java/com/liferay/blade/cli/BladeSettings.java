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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;

import com.liferay.blade.cli.command.BaseArgs;
import com.liferay.blade.cli.util.CollectUsage;
import com.liferay.blade.cli.util.Prompter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author Christopher Bryan Boyd
 * @author Gregory Amerson
 */
public class BladeSettings {

	public static String[] getUsageArgs(String[] args, Class<? extends BaseArgs> clazz, Collection<ParameterDescription> parameters) {
		Class<?> localClazz = clazz;
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		List<String> newArgsList = new ArrayList<>();
		String finalParameter = null;
		while (BaseArgs.class.isAssignableFrom(localClazz)) {
			for (Field field : localClazz.getDeclaredFields()) {
				Parameter annotation = field.getAnnotation(Parameter.class);
				

				if (annotation != null) {
					String[] names = annotation.names();

					if ((names != null) && (names.length > 0)) {
						
						String nameUsed = null;

						for (String name : names) {
							if (argsList.contains(name)) {
								nameUsed = name;

								break;
							}
						}

						if (nameUsed != null) {
							CollectUsage collectUsageAnnotation = field.getAnnotation(CollectUsage.class);
							

							if (collectUsageAnnotation != null) {
								Class<?> type = field.getType();

								if (!type.equals(boolean.class)) {
									newArgsList.add(nameUsed);
									

									if (collectUsageAnnotation.censor()) {
										newArgsList.add("<censored>");
									}
									else {
										newArgsList.add(argsList.get(argsList.indexOf(nameUsed) + 1));
									}
								}
								else {
									newArgsList.add(nameUsed);
								}
							}
						}
					}
					else {
						CollectUsage collectUsageAnnotation = field.getAnnotation(CollectUsage.class);
						

						if (collectUsageAnnotation != null) {
								

							if (collectUsageAnnotation.censor()) {
								finalParameter = "<final parameter censored>";
							}
							else {
								finalParameter = argsList.get(newArgsList.size() - 1);
							}
						}
					}
				}
			}

			localClazz = localClazz.getSuperclass();
		}

		if (finalParameter != null) {
			newArgsList.add(finalParameter);
		}

		return newArgsList.toArray(new String[0]);
	}

	public BladeSettings(BladeCLI bladeCLI, File bladeUserHomeDir, File... settingsFiles) throws IOException {
		_bladeCLI = bladeCLI;
		_bladeUserHomeDir = bladeUserHomeDir;
		
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

	public String getProfileName() {
		return _properties.getProperty(_PROFILE_NAME);
	}

	public boolean getSubmitUsageStats() {
		return Boolean.valueOf(_properties.getProperty(_SUBMIT_STATS_KEY));
	}

	public void load() throws IOException {
		List<File> loadFirstList = new ArrayList<>();
		List<File> loadLastList = new ArrayList<>();
		

		for (File file : _settingsFiles) {
			if (file.exists()) {
				WorkspaceProvider workspaceProvider = _bladeCLI.getWorkspaceProvider(file);
				

				if ((workspaceProvider != null) && workspaceProvider.isWorkspace(file)) {
					loadLastList.add(file);
				}
				else {
					loadFirstList.add(file);
				}
			}
		}

		for (File file : loadFirstList) {
			try (FileInputStream fileInputStream = new FileInputStream(file)) {
				
				Properties properties = new Properties();

				properties.load(fileInputStream);
				
				_properties.putAll(properties);
			}
		}

		for (File file : loadLastList) {
			try (FileInputStream fileInputStream = new FileInputStream(file)) {
				
				Properties properties = new Properties();

				properties.load(fileInputStream);
				
				_properties.putAll(properties);
			}
		}
		
	}

	public void migrateWorkspaceIfNecessary() throws IOException {
		

		for (File settingsFile : _settingsFiles) {
			WorkspaceProvider workspaceProvider = _bladeCLI.getWorkspaceProvider(settingsFile);
	

			if ((workspaceProvider != null) && workspaceProvider.isWorkspace(_bladeCLI)) {
				File workspaceDirectory = workspaceProvider.getWorkspaceDir(settingsFile);

	
				File pomFile = new File(workspaceDirectory, "pom.xml");
	
				boolean shouldPrompt = false;
	

				if (pomFile.exists()) {
					if (!settingsFile.exists()) {
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
		for (File settingsFile : _settingsFiles) {
			if (!settingsFile.exists()) {
				File parentDir = settingsFile.getParentFile();

	
				parentDir.mkdirs();
			}

			WorkspaceProvider workspaceProvider = _bladeCLI.getWorkspaceProvider(settingsFile);
			

			if ((workspaceProvider != null) && workspaceProvider.isWorkspace(_bladeCLI)) {
				Properties properties = new Properties();
				String propertyValue = _properties.getProperty(_LIFERAY_VERSION_DEFAULT_KEY);

				properties.put(_LIFERAY_VERSION_DEFAULT_KEY, propertyValue);
				propertyValue = _properties.getProperty(_PROFILE_NAME);
				
				properties.put(_PROFILE_NAME, propertyValue);

				try (FileOutputStream out = new FileOutputStream(settingsFile)) {
					properties.store(out, null);
				}
			}
			else {
				Properties properties = new Properties();
				
				String submitUsageStats = String.valueOf(getSubmitUsageStats());

				
				properties.setProperty(_SUBMIT_STATS_KEY, submitUsageStats);
				

				try (FileOutputStream out = new FileOutputStream(settingsFile)) {
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

	private static final String _LIFERAY_VERSION_DEFAULT_KEY = "liferay.version.default";

	private static final String _PROFILE_NAME = "profile.name";

	private static final String _SUBMIT_STATS_KEY = "submit.stats";

	private final BladeCLI _bladeCLI;
	private final File _bladeUserHomeDir;
	private final Properties _properties = new Properties();
	private final Collection<File> _settingsFiles;

}