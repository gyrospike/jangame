package org.alchemicstudio;

public class GMGridBuild extends GameMode {

	/** the game grid, huge fat class with too much stuff in it */
	private Grid mGrid = null;
	
	public GMGridBuild(Grid grid) {
		mGrid = grid;
	}
	
	/**
	 * return the track the user has created on this grid
	 * 
	 * @return
	 */
	public Node[][] getTrack() {
		return mGrid.getNodes();
	}
	
	/**
	 * updates the build game mode
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
		mGrid.updateTrackDrag(input.x, input.y);
	}

	/**
	 * handle touch down event
	 * 
	 * @param input
	 */
	public void processTouchDownEvent(InputObject input) {
		mGrid.checkNodePress(input.x, input.y);
	}

	/**
	 * handle touch up event
	 * 
	 * @param input
	 */
	public void processTouchUpEvent(InputObject input) {
		mGrid.checkNodeRelease(input.x, input.y);
	}
}
