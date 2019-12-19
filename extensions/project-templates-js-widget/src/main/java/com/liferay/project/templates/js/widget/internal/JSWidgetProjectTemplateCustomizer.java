package com.liferay.project.templates.js.widget.internal;

import com.liferay.project.templates.extensions.ProjectTemplateCustomizer;
import com.liferay.project.templates.extensions.ProjectTemplatesArgs;
import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.liferay.project.templates.js.widget.internal.JSWidgetProjectTemplatesArgsExt;

public class JSWidgetProjectTemplateCustomizer
    implements ProjectTemplateCustomizer {
	
	@Override
	public String getTemplateName() {
		return "js-widget";
	}
	
    @Override
	public void onAfterGenerateProject(
			ProjectTemplatesArgs projectTemplatesArgs, File destinationDir,
			ArchetypeGenerationResult archetypeGenerationResult)
		throws Exception {

        File userHome =  new File(System.getProperty("user.home"));

        Path userHomePath = userHome.toPath();

        Path bladeCachePath = userHomePath.resolve(".blade/cache");

        Path configPath =
            Paths.get(
                destinationDir.getAbsolutePath(),
                projectTemplatesArgs.getName() + "/config.json");

        String config = read(configPath);

        config = _replace(config, "[$TARGET$]", "react-widget");
        
        
        config = _replace(
            config, "[$OUTPUT_PATH$]",
            projectTemplatesArgs.getName());
        config = _replace(config, "[$DESCRIPTION$]", projectTemplatesArgs.getName());

        JSWidgetProjectTemplatesArgsExt ext = (JSWidgetProjectTemplatesArgsExt)projectTemplatesArgs.getProjectTemplatesArgsExt();
        
        String workspaceLocation = ext.getWorkspaceLocation();

        if (workspaceLocation != null) {
        	String liferayLocation = Paths.get(workspaceLocation + "/bundles/").normalize().toString();
            if (File.pathSeparator.equals(";")) {
                liferayLocation = liferayLocation.replace("\\", "\\\\");
        	}
        	config = _replace(config, "[$LIFERAY_DIR$]", liferayLocation);
        	config = _replace(config, "[$LIFERAY_PRESENT$]", "true");

        	String modulesLocation =  ext.getModulesLocation();
        	
        	modulesLocation = new File(ext.getModulesLocation(), projectTemplatesArgs.getName()).getAbsolutePath();
        	
            if (File.pathSeparator.equals(";")) {
            	modulesLocation = modulesLocation.replace("\\", "\\\\");
        	}
            
        	Path modulesPath = Paths.get(modulesLocation);
  
        	Path workspacePath = Paths.get(workspaceLocation).normalize();
        	
        	Path relativePath = workspacePath.relativize(modulesPath);
        	
        	String relativePathString = relativePath.toString();
        	
            if (File.pathSeparator.equals(";")) {
            	relativePathString = relativePathString.replace("\\", "\\\\");
        	}
            
        	config = _replace(config, "[$FOLDER$]", relativePathString);
        }
        else
        {
        	config = _replace(config, "[$LIFERAY_DIR$]", "/liferay");
        	config = _replace(config, "[$LIFERAY_PRESENT$]", "false");
        }
        
        
        write(configPath, config);

        ProcessBuilder processBuilder = new ProcessBuilder();

        File cwd = new File(System.getProperty("user.dir"));

        processBuilder.directory(cwd);

        Map<String, String> env = processBuilder.environment();

        List<String> commands = new ArrayList<>();

        if (isWindows()) {
            commands.add("cmd.exe");
            commands.add("/c");

            Path nodePath = bladeCachePath.resolve("node").resolve("node.exe");
            Path yoPath =
                bladeCachePath.resolve("yo/node_modules/yo/lib/cli.js");

            //commands.add("sh");
            //commands.add("-c");
            commands.add(
                nodePath.toString());
            commands.add(yoPath.toString());
            commands.add("liferay-js");
            commands.add("--config");
            commands.add(configPath.toString());
            
            for (String command : commands) {
            System.out.println(command);
            }
        }
        else {
            env.put("PATH", env.get("PATH") + ":/bin:/usr/local/bin");

            Path nodePath = bladeCachePath.resolve("node/bin/node");
            Path yoPath = bladeCachePath.resolve("yo/node_modules/.bin/yo");

            commands.add("sh");
            commands.add("-c");
            commands.add(
                nodePath.toString() + " " + yoPath.toString() + " liferay-js --config " + configPath.toString());
        }


        processBuilder.command(commands);
        processBuilder.inheritIO();

        Process process = processBuilder.start();

        OutputStream outputStream = process.getOutputStream();

        outputStream.close();

        process.waitFor();

        //Files.delete(configPath);
    }

    public boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }

    public String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

    public void write(Path destination, String content) throws Exception {
       Files.write(destination, content.getBytes());
    }

    private String _replace(String s, String key, Object value) {
        if (value == null) {
            value = "";
        }

        return s.replace(key, value.toString());
    }

    @Override
    public void onBeforeGenerateProject(
            ProjectTemplatesArgs projectTemplatesArgs,
            ArchetypeGenerationRequest archetypeGenerationRequest)
    throws Exception {

//            Properties properties = archetypeGenerationRequest.getProperties();
//
//            properties.put("packageJsonVersion", "1.0.0");

    }

}
