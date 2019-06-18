package com.liferay.blade.extensions.maven.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ProcessResult {
	private StringBuffer output = new StringBuffer();
	private StringBuffer error = new StringBuffer();
	private boolean done = false;
	private int exitCode = -1;
	private Collection<Throwable> throwables = new ArrayList<>();
	private Future<?> future = null;
	public Collection<Throwable> getThrowables() {
		return throwables;
	}
	public int getExitCode() {
		return exitCode;
	}
	public Future<?> getFuture() {
		return future;
	}
	public String getOutput() {
		return output.toString();
	}
	public String getError() {
		return error.toString();
	}
	public static ProcessResult execute(String... args) {
		return execute(new File("."), true, args);
	}
	
	public static ProcessResult execute(File directory, boolean printOutput, String... args) {
		ProcessResult processResult = new ProcessResult();
		
		processResult.executeLocal(directory, printOutput, args);
		
		return processResult;
	}
	
	private void executeLocal(File directory, boolean printOutput, String... args) {
		
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		
		processBuilder.directory(directory);
		
		try {

			Process process = processBuilder.start();
	
			CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(
				() -> {
					String line = null;
	
					try 
						(BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
						while ((line = processOutput.readLine()) != null) {
							output.append(line);
							output.append(System.lineSeparator());
	
							if (printOutput) {
								System.out.println(line);
							}
						}
					}
					catch (Exception e) {
						System.err.println(e.getMessage());
						throwables.add(e);
					}
				});
	
			CompletableFuture<Void> errorFuture = CompletableFuture.runAsync(
				() -> {
					String line = null;
	
					try (BufferedReader processError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
						while ((line = processError.readLine()) != null) {
							error.append(line);
							error.append(System.lineSeparator());
	
							if (printOutput) {
								System.err.println(line);
							}
						}
					}
					catch (Exception e) {
						System.err.println(e.getMessage());
						throwables.add(e);
					}
				});
			CompletableFuture<Void> processFuture = CompletableFuture.runAsync(
				() -> {
					try {
						exitCode = process.waitFor();
					}
					catch (Throwable th) {
						throwables.add(th);
					}
			});
	
			CompletableFuture<?> futures = CompletableFuture.allOf(outputFuture, errorFuture, processFuture);
			future = futures;
			futures.thenRun(() -> done = true);
		}
		catch (Throwable th) {
			throwables.add(th);
		}
	}
	
	public boolean isDone() {
		return done;
	}
}
