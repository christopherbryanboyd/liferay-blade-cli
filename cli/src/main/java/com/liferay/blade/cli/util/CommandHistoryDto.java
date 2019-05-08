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

package com.liferay.blade.cli.util;

import java.util.Date;

/**
 * @author Christopher Bryan Boyd
 */
public class CommandHistoryDto {
	public String getArgs() {
		return args;
	}

	

	public String getException() {
		return exceptionString;
	}

	

	public int getExitCode() {
		return exitCode;
	}

	public String getName() {
		return name;
	}

	

	public String getProfile() {
		return profile;
	}

	

	public Date getTimeInvoked() {
		return new Date(timeInvoked);
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public void setException(String exception) {
		this.exceptionString = exception;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public void setTimeInvoked(Date timeInvoked) {
		this.timeInvoked = timeInvoked.getTime();
	}

	public void setTimeInvokedValue(long timeInvoked) {
		this.timeInvoked = timeInvoked;
	}

	private String args;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((exceptionString == null) ? 0 : exceptionString.hashCode());
		result = prime * result + exitCode;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + (int) (timeInvoked ^ (timeInvoked >>> 32));
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandHistoryDto other = (CommandHistoryDto) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (exceptionString == null) {
			if (other.exceptionString != null)
				return false;
		} else if (!exceptionString.equals(other.exceptionString))
			return false;
		if (exitCode != other.exitCode)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		if (timeInvoked != other.timeInvoked)
			return false;
		return true;
	}

	private String exceptionString;
	private int exitCode;
	private String name;
	private String profile;

	
	private long timeInvoked;

}
