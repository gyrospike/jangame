package org.alchemicstudio;

public class GMGridBuild extends GameMode {

	/** the game grid, huge fat class with too much stuff in it */
	private Grid mGrid;
	
	public GMGridBuild() {
		
	}
	
	/**
	 * initialize the grid object within the passed in parameters
	 * 
	 * @param dataSet		contains preset obstacles built into the grid
	 * @param screenWidth	need this value for spacing
	 * @param screenHeight	need this value for spacing
	 */
	public void loadGrid(ParsedDataSet dataSet, float screenWidth, float screenHeight) {
		mGrid = new Grid(dataSet, screenWidth, screenHeight);
	}
	
	/**
	 * return the track the user has created on this grid
	 * 
	 * @return
	 */
	public int[][] getTrack() {
		return null;
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
