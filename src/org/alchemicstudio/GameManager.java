package org.alchemicstudio;

/**
 * handles the initialization of the game's resource loading and the game's
 * logical entities
 * 
 */


import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameManager extends BaseManager {

	/** game mode constant for build */
	public final static int GAME_MODE_BUILD = 0;
	
	/** game mode constant for release */
	public final static int GAME_MODE_RELEASE = 1;
	
	/** debug text constant representing the release game mode */
	private final static String DEBUG_GAME_MODE_RELEASE = "Release";
	
	/** debug text constant representing the build game mode */
	private final static String DEBUG_GAME_MODE_BUILD = "Build";
	
	/** array of game modes */
	private GameMode[] mGameModeArray = new GameMode[2];
	
	/** queue of input objects ready to be processed */
	private ArrayBlockingQueue<InputObject> mInputQueue = new ArrayBlockingQueue<InputObject>(BaseActivity.INPUT_QUEUE_SIZE);
	
	/** locking object for accessing the input queue */
	private Object inputQueueMutex = new Object();
	
	/** current active game mode */
	private int mActiveGameMode;
	
	/** the context of the activity creating this manager */
	private Context mContext;
	
	/** the data set containing all the level data */
	private ParsedMapData mDataSet;
	
	/** width of the screen */
	private int mScreenWidth;
	
	/** height of the screen */
	private int mScreenHeight;
	
	/** the effects overlay */
	private DrawableOverlay mOverlay;

    /** handler for the on level complete ui event */
    private Handler mHandler;

    /**
     *
     * @param context   reference to the base activity
     */
    public GameManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

	/**
	 * create the primary logical entities for the game
	 *
	 * @param dataSet		the game's grid data loaded from xml
	 * @param screenWidth
	 * @param screenHeight
	 */
	public void loadData(ParsedMapData dataSet, int screenWidth, int screenHeight) {
		mDataSet = dataSet;
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
	}
	
	/**
	 * start the game manager now that we now the assets are loaded
	 */
	public void init() {
		mOverlay = new DrawableOverlay();
		Grid gameGrid = new Grid(mDataSet, mScreenWidth, mScreenHeight, mOverlay);
		ReleaseManager releaseManager = new ReleaseManager(mOverlay, mContext, mHandler, mDataSet);

		mGameModeArray[GAME_MODE_BUILD] = new GMGridBuild(gameGrid);
		mGameModeArray[GAME_MODE_RELEASE] = new GMRelease(gameGrid, releaseManager, mContext);
		mActiveGameMode = GAME_MODE_BUILD;

		Button gameModeToggleButton = (Button) ((Activity) mContext).findViewById(R.id.gameModeToggleButton);
		gameModeToggleButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleGameModes();
			}
		});

		super.init();
	}

	/**
	 * update
	 * 
	 * @param timeDelta
	 */
	public void update(long timeDelta) {
		processInput();
		mGameModeArray[mActiveGameMode].update(timeDelta);
		mOverlay.update(timeDelta);
		HUD.getInstance().update(timeDelta);
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
	private void toggleGameModes() {
		if(mActiveGameMode == GAME_MODE_BUILD) {
			mActiveGameMode = GAME_MODE_RELEASE;
		} else {
			mActiveGameMode = GAME_MODE_BUILD;
		}
		for(int i = 0; i < mGameModeArray.length; i++) {
			if(i == mActiveGameMode) {
				mGameModeArray[i].makeActive();
			} else {
				mGameModeArray[i].makeInactive();
			}
		}
	}
}
