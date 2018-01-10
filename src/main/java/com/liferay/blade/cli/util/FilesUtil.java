package com.liferay.blade.cli.util;

import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import com.liferay.blade.cli.CopyDirVisitor;

import java.nio.file.*;

public class FilesUtil {

	public static void delete(Path path) throws Exception {
		if (Files.exists(path))
		{
			if (Files.isDirectory(path))
			{

				for (Path p : Files.list(path).collect(Collectors.toList())) {
				/*for (Path p : Files.walk(path, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder())
						.collect(Collectors.toList())) {*/
					
					try {
						delete(p);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				deleteFile(path);
				
			}
			else
			{
				deleteFile(path);
			}
		}
	}
	private static void deleteFile(Path path) throws Exception
	{
		Optional<Exception> exception = Optional.empty();
		for (int i = 0; i < 5 && Files.exists(path); i++)
		{
			try
			{
				Files.delete(path);
				exception = Optional.empty();
			}
			catch (Exception e)
			{
				exception= Optional.of(e);
				try {
					Thread.sleep(100);
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
