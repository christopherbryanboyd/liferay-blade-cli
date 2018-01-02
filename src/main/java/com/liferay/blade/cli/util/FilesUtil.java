package com.liferay.blade.cli.util;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

public class FilesUtil {

	public static void deleteWithException(Path path) throws IOException {
		for (Path p : Files.walk(path, FileVisitOption.FOLLOW_LINKS)
		.sorted(Comparator.reverseOrder())
		.collect(Collectors.toList())) {
			Files.delete(p);
		}
	}

}
