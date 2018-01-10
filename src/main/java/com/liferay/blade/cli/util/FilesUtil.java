package com.liferay.blade.cli.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.liferay.blade.cli.CopyDirVisitor;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FilesUtil {
	private static ExecutorService executorService = Executors.newSingleThreadExecutor();
	public static void delete(Path path) throws Exception {
		Collection<Future<Void>> futures = new ArrayList<>();
		if (Files.exists(path))
		{
			if (Files.isDirectory(path))
			{
				/*Files.walk(path, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).parallel().forEach((p)-> {
					
					try {
						deleteFile(p);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				});*/
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					   @Override
					   public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
							futures.add(CompletableFuture.runAsync(() -> {
								try {
									deleteFile(file);
								} catch (IOException e) {
									e.printStackTrace();
								}
							},  executorService));
					       return FileVisitResult.CONTINUE;
					   }

					   @Override
					   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							futures.add(CompletableFuture.runAsync(() -> {
								try {
									deleteFile(dir);
								} catch (IOException e) {
									e.printStackTrace();
								}
							},  executorService));
					       return FileVisitResult.CONTINUE;
					   }
					});

				
			}
			else
			{
				futures.add(CompletableFuture.runAsync(() -> {
					try {
						deleteFile(path);
					} catch (IOException e) {
						e.printStackTrace();
					}
				},  executorService));
			}
			for (Future<Void> future : futures)
				future.get();
		}
	}
	private static void deleteFile(Path path) throws IOException
	{
		Optional<IOException> exception = Optional.empty();
		for (int i = 0; i < 5 && Files.exists(path); i++)
		{
			try
			{
				Files.delete(path);
				exception = Optional.empty();
			}
			catch (IOException e)
			{
				exception= Optional.of(e);
				try {
					Thread.sleep(200);
					System.gc();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		if (exception.isPresent())
			throw exception.get();

	}
	

	public static void copy(Path sourceDir, Path targetDir) throws IOException {
		Files.walkFileTree(sourceDir, new CopyDirVisitor(sourceDir, targetDir, StandardCopyOption.REPLACE_EXISTING));
	}
	
	public static String convertStreamToString(java.io.InputStream is) {
	   
		final String returnValue;
		try (java.util.Scanner s = new java.util.Scanner(is))
	    {
			s.useDelimiter("\\A");
			returnValue = s.hasNext() ? s.next() : "";
	    }
	    finally {
	    	try {
	    		is.close();
	    	}
	    	catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
		return returnValue;
	}

}
