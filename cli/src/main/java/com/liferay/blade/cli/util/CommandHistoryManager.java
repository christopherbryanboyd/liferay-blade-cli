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

package com.liferay.blade.cli.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import java.lang.reflect.Type;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

/**
 * @author Christopher Bryan Boyd
 */
public class CommandHistoryManager {
	public CommandHistoryManager(Path outputPath) {
		_outputPath = outputPath;
	}

	public void add(CommandHistoryDto dto) {
		_dtoList.add(dto);
	}

	public List<CommandHistoryDto> getDtoList() {
		return _dtoList;
	}

	public void load() {
		StringBuilder stringBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(_outputPath)) {
			Collection<String> strings = stream.filter(
				str -> !str.isEmpty()
			).map(
				String::trim
			).filter(
				str -> !"{}".equals(str)
			).collect(Collectors.toList());

			for (String string : strings) {
				string = string.replaceAll("\\r\\n|\\r|\\n", " ");
				
				stringBuilder.append(string);
				
				stringBuilder.append(System.lineSeparator());
			}
			
			_dtoList.clear();
			
			if (stringBuilder.length() > 0) {
				String json = stringBuilder.toString();

				Type type = new TypeToken<ArrayList<CommandHistoryDto>>() {
				}.getType();
				
				if (json != null) {
					
					json = json.trim();
					
					if (!json.isEmpty()) {
						
						Collection<CommandHistoryDto> commandHistory = new Gson().fromJson(json, type);
						
						_dtoList.addAll(commandHistory);
					}
				}
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void save() {
		try {
			Type type = new TypeToken<ArrayList<CommandHistoryDto>>() {
			}.getType();
			
			String json = _gson.toJson(_dtoList, type);
			
			try {
				Files.deleteIfExists(_outputPath);
				Files.write(_outputPath, json.getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    public void upload() {
    	Client client = ClientBuilder.newClient();

    	WebTarget webTarget = client.target(_USAGE_SERVER_URI);

    	
    	Invocation.Builder builder = webTarget.request();
    	
    	Entity<List<CommandHistoryDto>> entity = Entity.json(_dtoList);
    
    	Response response = builder.put(entity);

    	
    	StatusType statusType = response.getStatusInfo();

    	
    	Status status = statusType.toEnum();
    	
    	if (status != Status.OK) {
    		throw new RuntimeException("Status is wrong, " + status.toString());
    	}
    }

    private static final String _USAGE_SERVER_URI = "http://localhost:8082/usage-server/commandhistory";

	private List<CommandHistoryDto> _dtoList = new ArrayList<>();
	private final Gson _gson = new Gson();
	private Path _outputPath;

}
