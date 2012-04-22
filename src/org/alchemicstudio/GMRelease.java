package org.alchemicstudio;

public class GMRelease extends GameMode {
	
	/** 2d array of the nodes that were constructed in the build game mode */
	private Node[][] mNodes = null;

	public GMRelease() {
		//create the cart
		
	}

	/**
	 * update the release game mode
	 */
	public void update(float timeDelta) {

	}
	
	/**
	 * load the track data from the grid build game mode
	 * 
	 * @param	nodes	the nodes that were created during the build game mode
	 */
	public void loadTrack(Node[][] nodes) {
		mNodes = nodes;
	}

	/**
	 * handle touch move event
	 * 
	 * @param input
	 */
	public void processTouchMoveEvent(InputObject input) {

	}

	/**
	 * handle touch down event
	 * 
	 * @param input
	 */
	public void processTouchDownEvent(InputObject input) {

	}

	/**
	 * handle touch up event
	 * 
	 * @param input
	 */
	public void processTouchUpEvent(InputObject input) {

	}
}
