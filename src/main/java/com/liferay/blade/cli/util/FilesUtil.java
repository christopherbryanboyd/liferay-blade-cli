package com.liferay.blade.cli.util;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.liferay.blade.cli.CopyDirVisitor;

import java.nio.file.*;

public class FilesUtil {

	public static void delete(Path path) throws IOException {
		if (Files.exists(path))
		{
			if (Files.isDirectory(path))
			{
				FileUtils.deleteDirectory(path.toFile());
				/*for (Path p : Files.walk(path, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder())
						.collect(Collectors.toList())) {
					Files.delete(p);
				}*/
				
				
			}
			else
			{
				Files.delete(path);
			}
		}
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
