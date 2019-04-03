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

public class CommandHistoryManager {
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
			Collection<String> strings = stream.collect(Collectors.toList());

			for (String string : strings) {
				stringBuilder.append(string + System.lineSeparator());
			}
			
			String json = stringBuilder.toString();
			Type listType = new TypeToken<Type>(){}.getType();

			Collection<CommandHistoryDto> commandHistory =  new Gson().fromJson(json, listType);
			
			dtoList.clear();
			dtoList.addAll(commandHistory);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	

	public void save() {
		try {
			/*
			String gsonString = gson.toJson(dtoList);

			try (Writer writer = new FileWriter(outputPath.toFile())) {
			    Gson gson = new GsonBuilder().create();
			    gson.toJson(dtoList, writer);
			}

			System.out.println(gsonString);

			try (Writer writer = new FileWriter("Output.json")) {
			    Gson gson = new GsonBuilder().create();
			    gson.toJson(dtoList, writer);
			}

			*/
            Type type = new TypeToken<Type>() {}.getType();
            String json = gson.toJson(dtoList, type);
            try {
            	Files.deleteIfExists(outputPath);
                Files.write(outputPath, json.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	

	private List<CommandHistoryDto> dtoList = new ArrayList<>();

	
	private final Gson gson = new Gson();

	
	private Path outputPath;

	
}
