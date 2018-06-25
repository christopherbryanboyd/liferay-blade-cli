/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liferay.blade.cli;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Christopher Bryan Boyd
 */
public class PathChangeWatcher implements AutoCloseable, Supplier<Boolean> {
	private boolean changed = false;
	private boolean closed = false;
    private final WatchService watchService;
	private WatchKey watchKey;
	private Path pathFileName;
	public PathChangeWatcher(Path path) {
		try {
			watchService = FileSystems.getDefault().newWatchService();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Path pathToWatch;
		
		if (!Files.isDirectory(path)) {
			pathToWatch = path.getParent();
		}
		else {
			pathToWatch = path;
		}
		
		pathFileName = path.getFileName();
		try {
			watchKey = 
			        pathToWatch.register(
			                watchService, 
			                  StandardWatchEventKinds.ENTRY_CREATE, 
			                    StandardWatchEventKinds.ENTRY_DELETE, 
			                      StandardWatchEventKinds.ENTRY_MODIFY);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void close() throws Exception {
		if (!closed) {
			closed = true;
			if (watchKey.isValid()) {
				watchKey.cancel();
			}
			watchService.close();
		}
	}
	
	public boolean isClosed() {
		return closed;
	}

	@Override
	public Boolean get() {
		if (changed) {
			return changed;
		} else if (closed) {
			return false;
		} else {
			WatchKey key = null;
			try {
				while (!closed && !changed && (key = watchService.poll(100, TimeUnit.MILLISECONDS)) != null) {

					for (WatchEvent<?> event : key.pollEvents()) {

						final Path changedPath = (Path) event.context();
						if (pathFileName.equals(changedPath.getFileName())) {
							changed = true;
							close();
							break;
						}
					}
					if (!closed) {
						key.reset();
					}
				}
				return changed;
			} 
			catch (ClosedWatchServiceException e) {

				if (!closed) {
					try {
						close();
					} 
					catch (Exception ignored) {
					}
				}
			}
			catch (Exception e) {

				if (!closed) {
					try {
						close();
					} 
					catch (Exception ignored) {
					}
				}
				throw new RuntimeException(e);
			} 
			return false;
		}
	}
}
