package com.liferay.blade.cli;

public abstract class BaseCommand<T extends BaseArgs> {

	public BaseCommand() {
	}

	public BaseCommand(BladeCLI blade, T args) {
	_args = args; _blade = blade;
	}

	public abstract void execute() throws Exception;

	public T getArgs() {
		return _args;
	}

	public abstract Class<T> getArgsClass();

	public BladeCLI getBlade() {
		return _blade;
	}

	public void setArgs(BaseArgs _commandArgs) {
		this._args = getArgsClass().cast(_commandArgs);
	}

	public void setBlade(BladeCLI _blade) {
		this._blade = _blade;
	}

	protected T _args;
	protected BladeCLI _blade;

}