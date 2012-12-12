package org.alchemicstudio;

/**
 * handles the initialization of the game's resource loading and the game's
 * logical entities
 *
 */


import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
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
    private ArrayBlockingQueue<InputObject> mInputQueue = new ArrayBlockingQueue<InputObject>(GameActivity.INPUT_QUEUE_SIZE);

    /** locking object for accessing the input queue */
    private Object inputQueueMutex = new Object();

    /** current active game mode */
    private int mActiveGameMode;

    /** the context of the activity creating this manager */
    private Context mContext;

    /** the data set containing all the level data */
    private ParsedMapData mDataSet;

    /** the effects overlay */
    private DrawableOverlay mOverlay;

    /** the game grid */
    private Grid mGameGrid;

    /** handler for the on level complete ui event */
    private Handler mHandler;

    /** max number of static deco objects in the game */
    private static final int MAX_NUM_STATIC_DECO = 5;

    /** fixed array of static deco for game background */
    private FixedSizeArray<DrawableObject> mStaticDeco = new FixedSizeArray<DrawableObject>(MAX_NUM_STATIC_DECO);

    /** length of the static deco array */
    private int mStaticLen;

    /** user facing name of the level we are loading, ex: resource id for "easy 1" in strings.xml */
    private int mMapNameResource;

    /** maps this level to a save file on the HD */
    private int mSaveId;

    /**
     *
     * @param context   reference to the base activity
     */
    public GameManager(DisplayMetrics metrics, Context context, Handler handler) {
        super(metrics);
        mContext = context;
        mHandler = handler;
    }

    /**
     * create the primary logical entities for the game
     *
     * @param dataSet		the game's grid data loaded from xml
     * @param mapName       the name of the map, stored in menumap.xml
     * @param saveId        the id mapped to the save file for this level
     */
    public void loadData(ParsedMapData dataSet, int mapName, int saveId) {
        mDataSet = dataSet;
        mMapNameResource = mapName;
        mSaveId = saveId;
    }

    /**
     * start the game manager now that we now the assets are loaded
     */
    public void init() {
        mOverlay = new DrawableOverlay();
        mGameGrid = new Grid(mDataSet, mScreenWidth, mScreenHeight, mOverlay);
        ReleaseManager releaseManager = new ReleaseManager(mOverlay, mContext, mHandler, mMapNameResource, mSaveId, mDataSet);

        mGameModeArray[GAME_MODE_BUILD] = new GMGridBuild(mGameGrid);
        mGameModeArray[GAME_MODE_RELEASE] = new GMRelease(mGameGrid, releaseManager, mContext);
        mActiveGameMode = GAME_MODE_BUILD;

        Button gameModeToggleButton = (Button) ((Activity) mContext).findViewById(R.id.gameModeToggleButton);
        gameModeToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                toggleGameModes();
            }
        });

        Button resetTrackButton = (Button) ((Activity) mContext).findViewById(R.id.resetTrackButton);
        resetTrackButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                resetTrack();
            }
        });

        ImagePack gameBG = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("gameBackground");
        mStaticDeco.add(new DrawableObject(gameBG, 0, mScreenWidth, mScreenHeight));
        mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_BOTTOM_LEFT));

        mStaticLen = mStaticDeco.getCount();
        super.init();
    }

    /**
     * update
     *
     * @param timeDelta
     */
    public void update(long timeDelta) {
        for(int i = 0; i < mStaticLen; i++) {
            mStaticDeco.get(i).update(timeDelta);
        }
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

    /**
     * reset the track, remove all user created connections and path preferences
     * and toggle the power to off
     */
    private void resetTrack() {
        if(mActiveGameMode == GAME_MODE_RELEASE) {
            toggleGameModes();
        }
        mGameGrid.resetAllConnections();
    }
}
