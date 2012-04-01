package org.alchemicstudio;

import java.util.concurrent.ArrayBlockingQueue;

import android.util.Log;

public class GameManager {

	/** game mode constant for build */
	private final static int GAME_MODE_BUILD = 0;
	
	/** game mode constant for release */
	private final static int GAME_MODE_RELEASE = 1;
	
	/** current active game mode */
	private int mActiveGameMode = GAME_MODE_BUILD;
	
	/** array of game modes */
	private GameMode[] mGameModeArray = new GameMode[2];
	
	/** queue of input objects ready to be processed */
	private ArrayBlockingQueue<InputObject> mInputQueue = new ArrayBlockingQueue<InputObject>(BaseActivity.INPUT_QUEUE_SIZE);
	
	/** locking object for accessing the input queue */
	private Object inputQueueMutex = new Object();

	/**
	 * handles the initialization of the game's resource loading and the game's
	 * logical entities
	 * 
	 */
	public GameManager() {
		super();
		mGameModeArray[GAME_MODE_BUILD] = new GMGridBuild();
		mGameModeArray[GAME_MODE_RELEASE] = new GMRelease();
	}
	
	/**
	 * create the primary logical entities for the game
	 * 
	 * @param dataSet		the game's grid data loaded from xml
	 * @param screenWidth
	 * @param screenHeight
	 */
	public void initGame(ParsedDataSet dataSet, float screenWidth, float screenHeight) {
		((GMGridBuild) mGameModeArray[GAME_MODE_BUILD]).loadGrid(dataSet, screenWidth, screenHeight);
	}

	/**
	 * update
	 * 
	 * @param timeDelta
	 */
	public void update(float timeDelta) {
		processInput();
		mGameModeArray[mActiveGameMode].update(timeDelta);
	}
	
	/**
	 * move input objects form the base activity to the game manager
	 * 
	 * @param input
	 */
	public void feedInput(InputObject input) {
		// Log.d("DEBUG", "Got to feeding");
		synchronized (inputQueueMutex) {
			try {
				mInputQueue.put(input);
			} catch (InterruptedException e) {
				Log.d(e.getMessage(), e.toString());
			}
		}
	}

	/**
	 * hand out the input to the right logical entities
	 * 
	 */
	private void processInput() {
		synchronized (inputQueueMutex) {
			ArrayBlockingQueue<InputObject> inputQueue = mInputQueue;
			while (!inputQueue.isEmpty()) {
				try {
					InputObject input = inputQueue.take();
					if (input.eventType == InputObject.EVENT_TYPE_KEY) {
						// processKeyEvent(input);
						// Log.d("DEBUG", "Key Event yeah!");
					} else if (input.eventType == InputObject.EVENT_TYPE_TOUCH) {
						if (input.action == InputObject.ACTION_TOUCH_DOWN) {
							mGameModeArray[mActiveGameMode].processTouchDownEvent(input);
						}
						if (input.action == InputObject.ACTION_TOUCH_MOVE) {
							mGameModeArray[mActiveGameMode].processTouchMoveEvent(input);
						}
						if (input.action == InputObject.ACTION_TOUCH_UP) {
							mGameModeArray[mActiveGameMode].processTouchUpEvent(input);
						}
					}
					input.returnToPool();
				} catch (InterruptedException e) {
					Log.d(e.getMessage(), e.toString());
				}
			}
		}
	}
	
	/**
	 * switch between game modes
	 */
	/*
	private void toggleGameModes() {
		if(mActiveGameMode == GAME_MODE_BUILD) {
			mActiveGameMode = GAME_MODE_RELEASE;
			((GMRelease) mGameModeArray[GAME_MODE_RELEASE]).loadTrack(((GMGridBuild) mGameModeArray[GAME_MODE_BUILD]).getTrack());
		} else {
			mActiveGameMode = GAME_MODE_BUILD;
		}
	}
	*/
}
