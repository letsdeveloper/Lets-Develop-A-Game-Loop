package com.letsdeveloper.games;

public interface Game<I> {

	public abstract void update(I input);

	public abstract boolean isRunning();

	public abstract void render();

}
