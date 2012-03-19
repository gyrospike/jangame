package org.alchemicstudio;

import java.util.concurrent.ArrayBlockingQueue;

import android.util.Log;

public class GameManager {

	/** the game grid, huge fat class with too much stuff in it */
	private Grid mGrid;
	
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
	}
	
	/**
	 * create the primary logical entities for the game
	 * 
	 * @param dataSet		the game's grid data loaded from xml
	 * @param screenWidth
	 * @param screenHeight
	 */
	public void initGame(ParsedDataSet dataSet, float screenWidth, float screenHeight) {
		mGrid = new Grid(dataSet, screenWidth, screenHeight);
	}

	/**
	 * update
	 * 
	 * @param timeDelta
	 */
	public void update(float timeDelta) {
		processInput();
		mGrid.update(timeDelta);
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
							processTouchDownEvent(input);
						}
						if (input.action == InputObject.ACTION_TOUCH_MOVE) {
							processTouchMoveEvent(input);
						}
						if (input.action == InputObject.ACTION_TOUCH_UP) {
							processTouchUpEvent(input);
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
	 * handle touch move event
	 * 
	 * @param input
	 */
	private void processTouchMoveEvent(InputObject input) {
		mGrid.updateWire(input.x, input.y);
		//gameManager.checkNodePress(input.x, input.y);
		// objectManager.checkButtonPress(input.x, input.y);
		//Log.d("DEBUG", "Create Particle at (" + input.x + ", " + input.y + ")");
		//gameManager.createParticle(input.x, input.y);
	}

	/**
	 * handle touch down event
	 * 
	 * @param input
	 */
	private void processTouchDownEvent(InputObject input) {
		mGrid.checkNodePress(input.x, input.y);
		// objectManager.checkButtonPress(input.x, input.y);
		//Log.d("DEBUG", "Create Particle at (" + input.x + ", " + input.y + ")");
		//gameManager.createParticle(input.x, input.y);
	}

	/**
	 * handle touch up event
	 * 
	 * @param input
	 */
	private void processTouchUpEvent(InputObject input) {
		mGrid.checkNodeRelease(input.x, input.y);
	}
}
