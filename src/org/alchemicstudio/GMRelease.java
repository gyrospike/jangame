package org.alchemicstudio;

public class GMRelease extends GameMode {
	
	/** the game grid, huge fat class with too much stuff in it */
	private Grid mGrid = null;

	public GMRelease(Grid grid) {
		mGrid = grid;
	}

	/**
	 * update the release game mode
	 */
	public void update(float timeDelta) {
		mGrid.update(timeDelta);
	}

	/**
	 * handle touch move event
	 * 
	 * @param input
	 */
	public void processTouchMoveEvent(InputObject input) {
		mGrid.growTrackSwitchChain(input.x, input.y);
	}

	/**
	 * handle touch down event
	 * 
	 * @param input
	 */
	public void processTouchDownEvent(InputObject input) {
		mGrid.startTrackSwitchChain(input.x, input.y);
	}

	/**
	 * handle touch up event
	 * 
	 * @param input
	 */
	public void processTouchUpEvent(InputObject input) {
		mGrid.stopTrackSwitchChain();
	}
}
