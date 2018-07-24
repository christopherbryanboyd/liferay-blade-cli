package com.liferay.extensions.maven.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.liferay.blade.cli.BladeCLI;
import com.liferay.blade.cli.BladeProfile;
import com.liferay.blade.cli.command.BaseCommand;
import com.liferay.blade.cli.gradle.LiferayBundleDeployerImpl;
import com.liferay.blade.cli.util.BladeUtil;

/**
 * @author Christopher Boyd
 */
@BladeProfile("maven")
public class MavenDeployCommand extends BaseCommand<MavenDeployArgs> {

	@Override
	public void execute() throws Exception {
		BladeCLI bladeCLI = getBladeCLI();
		if (!BladeUtil.canConnect(host, port)) {
			StringBuilder sb = new StringBuilder();
			
			sb.append("Unable to connect to gogo shell on " + host + ":" + port);
			sb.append(System.lineSeparator());
			sb.append("Liferay may not be running, or the gogo shell may need to be enabled. ");
			sb.append("Please see this link for more details: ");
			sb.append("https://dev.liferay.com/en/develop/reference/-/knowledge_base/7-1/using-the-felix-gogo-shell");
			sb.append(System.lineSeparator());

			getBladeCLI().addErrors("deploy", Collections.singleton(sb.toString()));

			PrintStream err = bladeCLI.err();

			new ConnectException(sb.toString()).printStackTrace(err);

			return;
		}

		Path currentPath = Paths.get("./");
		if (BladeUtil.isProjectMaven(currentPath)) {
			if (getArgs().isWatch()) {
				_deployMavenWatch(currentPath);
			}
			else
			{
				
				_deployMaven(currentPath);
			}
		}
	}

	@Override
	public Class<MavenDeployArgs> getArgsClass() {
		return MavenDeployArgs.class;
	}

	private void _deployMaven(Path currentPath) throws Exception {
		MavenExec mavenExec = new MavenExec(getBladeCLI());
		
		if (mavenExec.executeMavenCommand("clean") == 0) {
		
			if (mavenExec.executeMavenCommand("package") == 0) { 
				
				installFromMavenCurrentPath(currentPath);

			}
		}
	}
	
	private void _deployMavenWatch(Path currentPath) throws Exception {

		BladeCLI blade = getBladeCLI();
		
		if (PomUtil.addPluginToPom(currentPath.resolve("pom.xml"))) {

			MavenExec mavenExec = new MavenExec(blade);

			if (mavenExec.executeMavenCommand("clean") == 0) {
				
				if (mavenExec.executeMavenCommand("package") == 0) {

					installFromMavenCurrentPath(currentPath);
	
					Process pr = mavenExec.executeMavenCommandAsync("fizzed-watcher:run");
		
					try (BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()))) {
					
						CompletableFuture.runAsync(() -> {
							String line;
							try {
								while ((line = in.readLine()) != null) {
									blade.out(line);
									if (line.contains("BUILD SUCCESS")) {
										installFromMavenCurrentPath(currentPath);
									}
								}
							} catch (IOException e) {
								blade.err(e.getMessage());
								e.printStackTrace(blade.err());
							}
						});
						pr.waitFor();
					}
					
				} 
				else {
					blade.err("Unable to build project.");
				}
			}
			else {
				blade.err("Unable to clean project.");
			}
		} 
		else {
			blade.err("Unable to add Maven Watcher Plugin to pom.xml");
		}
	}

	private void installFromMavenCurrentPath(Path currentPath) {
		File outputFile = BladeUtil.getMavenOutputFile(currentPath).toFile();

		try {
			LiferayBundleDeployerImpl.installOrUpdate(outputFile, getBladeCLI(), host, port);
		} catch (Exception e) {
			PrintStream error = getBladeCLI().err();

			error.println(e.getMessage());

			e.printStackTrace(error);
		}
	}

	private String host = "localhost";
	private int port = 11311;

}
