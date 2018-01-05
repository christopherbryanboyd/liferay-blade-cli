package com.liferay.blade.cli.util;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;

import java.nio.file.*;
import java.nio.file.attribute.*;

public class FilesUtil {

	public static void deleteWithException(Path path) throws IOException {
		for (Path p : Files.walk(path, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder())
				.collect(Collectors.toList())) {
			Files.delete(p);
		}
	}

	private static class CopyDir extends SimpleFileVisitor<Path> {
		private Path sourceDir;
		private Path targetDir;

		public CopyDir(Path sourceDir, Path targetDir) {
			this.sourceDir = sourceDir;
			this.targetDir = targetDir;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

			try {
				Path targetFile = targetDir.resolve(sourceDir.relativize(file));
				Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				System.err.println(ex);
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) {
			try {
				Path newDir = targetDir.resolve(sourceDir.relativize(dir));
				Files.createDirectory(newDir);
			} catch (IOException ex) {
				System.err.println(ex);
			}

			return FileVisitResult.CONTINUE;
		}

	}

	public static void copy(Path sourceDir, Path targetDir) throws IOException {
		Files.walkFileTree(sourceDir, new FilesUtil.CopyDir(sourceDir, targetDir));
	}
}
