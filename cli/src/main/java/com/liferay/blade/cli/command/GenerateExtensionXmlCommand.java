package com.liferay.blade.cli.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import com.liferay.blade.cli.ProfileEntry;
import com.liferay.blade.cli.ProfileRepository;
import com.liferay.blade.cli.ProfileRepositoryManager;

public class GenerateExtensionXmlCommand extends BaseCommand<GenerateExtensionXmlArgs> {

	@Override
	public void execute() throws Exception {
		GenerateExtensionXmlArgs args = getArgs();
		String homeFolder = System.getProperty("user.home");
		Path homeBladePath = Paths.get(homeFolder, ".blade");
		Files.createDirectories(homeBladePath);
		Path extensionsXmlPath = homeBladePath.resolve("extensions.xml");
		
		ProfileRepository repo = ProfileRepositoryManager.loadProfileRepository(extensionsXmlPath);
	
		for (ProfileEntry entry : repo.getProfileEntries()) {
			
		}
		
		ProfileEntry entry = new ProfileEntry();
		entry.setAuthor(args.getAuthor());
		entry.setGithubProjectUrl(args.getGithubProjectUrl());
		entry.setProfileName(args.getProfileName());
		entry.setVersion(args.getVersion());
		entry.setUpdatedDate(new Date());
		
		
	}

	@Override
	public Class<GenerateExtensionXmlArgs> getArgsClass() {
		return GenerateExtensionXmlArgs.class;
	}

}
