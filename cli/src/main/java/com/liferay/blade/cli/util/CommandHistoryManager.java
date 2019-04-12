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

public class CommandHistoryManager {
    private static final String _USAGE_SERVER_URI = "http://localhost:8082/usage-server/employees";

    public void upload() {
    	Client client = ClientBuilder.newClient();

    	WebTarget webTarget = client.target(_USAGE_SERVER_URI);
    	
    	Invocation.Builder builder = webTarget.request();
    	
    	Entity<List<CommandHistoryDto>> entity = Entity.json(dtoList);
    
    	Response response = builder.put(entity);
    	
    	StatusType statusType = response.getStatusInfo();
    	
    	Status status = statusType.toEnum();
    	
    	if (status != Status.OK) {
    		throw new RuntimeException("Status is wrong, " + status.toString());
    	}
    }
	public CommandHistoryManager(Path outputPath) {
		this.outputPath = outputPath;
	}

	public void add(CommandHistoryDto dto) {
		dtoList.add(dto);
	}

	public List<CommandHistoryDto> getDtoList() {
		return dtoList;
	}

	public void load() {
		StringBuilder stringBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(outputPath)) {
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
			
			dtoList.clear();
			
			if (stringBuilder.length() > 0) {
				String json = stringBuilder.toString();

				Type type = new TypeToken<ArrayList<CommandHistoryDto>>() {
				}.getType();
				
				if (json != null && !json.trim().isEmpty()) {
					Collection<CommandHistoryDto> commandHistory = new Gson().fromJson(json, type);
					dtoList.addAll(commandHistory);
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
			String json = gson.toJson(dtoList, type);
			try {
				Files.deleteIfExists(outputPath);
				Files.write(outputPath, json.getBytes(), StandardOpenOption.CREATE);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private List<CommandHistoryDto> dtoList = new ArrayList<>();

	private final Gson gson = new Gson();

	private Path outputPath;

}
