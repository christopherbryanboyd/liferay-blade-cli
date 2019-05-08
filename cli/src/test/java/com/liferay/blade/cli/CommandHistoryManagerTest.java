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

import com.liferay.blade.cli.util.CommandHistoryDto;
import com.liferay.blade.cli.util.CommandHistoryManager;

import java.io.File;

import java.nio.file.Path;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Christopher Bryan Boyd
 */
public class CommandHistoryManagerTest {

	@Test
	public void testCommandHistoryManager() throws Exception {
		
		File temporaryFile = temporaryFolder.newFile();

		
		Path temporaryPath = temporaryFile.toPath();

		
		CommandHistoryManager manager = new CommandHistoryManager(temporaryPath);

		CommandHistoryDto dto1 = new CommandHistoryDto();

		dto1.setArgs("-P maven");
		dto1.setExitCode(0);
		dto1.setName("init");
		dto1.setTimeInvoked(new Date());
		dto1.setProfile("maven");
		dto1.setExitCode(0);
		
		CommandHistoryDto dto2 = new CommandHistoryDto();

		dto2.setArgs("-t mvc-portlet");
		dto2.setExitCode(0);
		dto2.setName("create");
		dto2.setTimeInvoked(new Date());
		dto2.setProfile("maven");
		dto2.setExitCode(0);
		
		manager.add(dto1);
		manager.add(dto2);
		
		manager.save();
		
		manager.load();
		
		List<CommandHistoryDto> dtos = manager.getDtoList();

		Assert.assertEquals("Expected to find 2 entries", 2, dtos.size());
		
		Assert.assertTrue(dtos.contains(dto1));
		Assert.assertTrue(dtos.contains(dto2));
	}

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

}
