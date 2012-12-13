package org.star.circuit;

import org.star.game.InputObject;

public abstract class GameMode {

	/**
	 * handle touch move event
	 * 
	 * @param input
	 */
	public abstract void processTouchMoveEvent(InputObject input);

	/**
	 * handle touch down event
	 * 
	 * @param input
	 */
	public abstract void processTouchDownEvent(InputObject input);

	/**
	 * handle touch up event
	 * 
	 * @param input
	 */
	public abstract void processTouchUpEvent(InputObject input);
	
	/**
	 * make this game mode active
	 */
	public abstract void makeActive();
	
	/**
	 * make this game mode inactive
	 */
	public abstract void makeInactive();
	
	/**
	 * update the game mode
	 * 
	 * @param timeDelta
	 */
	public abstract void update(long timeDelta);
}
