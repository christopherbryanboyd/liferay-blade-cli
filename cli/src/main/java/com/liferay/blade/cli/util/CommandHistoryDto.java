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
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		CommandHistoryDto other = (CommandHistoryDto)obj;

		if (_args == null) {
			if (other._args != null) {
				return false;
			}
		} 
		else if (!_args.equals(other._args)) {
			return false;
		}

		if (_exceptionString == null) {
			if (other._exceptionString != null) {
				return false;
			}
		} 
		else if (!_exceptionString.equals(other._exceptionString)) {
			return false;
		}

		if (_exitCode != other._exitCode) {
			return false;
		}

		if (_name == null) {
			if (other._name != null) {
				return false;
			}
		} 
		else if (!_name.equals(other._name)) {
			return false;
		}

		if (_profile == null) {
			if (other._profile != null) {
				return false;
			}
		} 
		else if (!_profile.equals(other._profile)) {
			return false;
		}

		if (_timeInvoked != other._timeInvoked) {
			return false;
		}

		return true;
	}

	

	public String getArgs() {
		return _args;
	}

	

	public String getException() {
		return _exceptionString;
	}

	public int getExitCode() {
		return _exitCode;
	}

	

	public String getName() {
		return _name;
	}

	

	public String getProfile() {
		return _profile;
	}

	public Date getTimeInvoked() {
		return new Date(_timeInvoked);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_args == null) ? 0 : _args.hashCode());
		result = prime * result + ((_exceptionString == null) ? 0 : _exceptionString.hashCode());
		result = prime * result + _exitCode;
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_profile == null) ? 0 : _profile.hashCode());
		result = prime * result + (int)(_timeInvoked ^ (_timeInvoked >>> 32));

		return result;
	}

	public void setArgs(String args) {
		_args = args;
	}

	public void setException(String exception) {
		_exceptionString = exception;
	}

	public void setExitCode(int exitCode) {
		_exitCode = exitCode;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setProfile(String profile) {
		_profile = profile;
	}

	public void setTimeInvoked(Date timeInvoked) {
		_timeInvoked = timeInvoked.getTime();
	}

	public void setTimeInvokedValue(long timeInvoked) {
		_timeInvoked = timeInvoked;
	}



	private String _args;
	private String _exceptionString;
	private int _exitCode;
	private String _name;
	private String _profile;

	
	private long _timeInvoked;

}
