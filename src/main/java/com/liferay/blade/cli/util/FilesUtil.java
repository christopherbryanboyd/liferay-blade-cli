package com.liferay.blade.cli.util;

import java.io.IOException;
import com.liferay.blade.cli.CopyDirVisitor;

import java.nio.file.*;

public class FilesUtil {

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
