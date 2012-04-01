package org.alchemicstudio;

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
	 * update the game mode
	 * 
	 * @param timeDelta
	 */
	public abstract void update(float timeDelta);
}
