package org.alchemicstudio;

import android.os.SystemClock;
import android.util.Log;

class GameRunnable implements Runnable {

	/** updates per second debug counter */
	public static int mDebugUPSCounter = 0;
	
	/** number of milliseconds in a second */
	public static final int MILLISECONDS_PER_SECOND = 1000;
	
	/** minimum time that must elapse before the game can be updated again */
	private static final int MIN_TIME_DELTA_MS = 16;
	
	/** maximum time that can elapse and be used to update the game */
	private static final int MAX_TIME_DELTA_MS = 48;
	
	/** game time at last update */
	private long mLastTime;
	
	/** true when the game loop is over */
	private boolean mFinished;
	
	/** reference to the game manager, the game logic admin */
	private GameManager mGameManager;
	
	/** reference to the game renderer, the game drawer */
	private GameRenderer mGameRenderer;

	/** true when the game thread is paused */
	private boolean mPaused = false;
	
	/** pause lock object, I don't know too much about this */
	private Object mPauseLock;
	
	/** debugging window shows debug text */
	private DebugWindow mDWindow;
	
	/**
	 * controls the main update loop, has hooks into the game manager, which updates game logic, and
	 * the game renderer, which draws the game
	 */
	public GameRunnable(DebugWindow dWindow) {
		mPauseLock = new Object();
		mFinished = false;
		mDWindow = dWindow;
	}
	
	public void stopGame() {
		synchronized (mPauseLock) {
			mPaused = false;
			mFinished = true;
			mPauseLock.notifyAll();
		}
	}

	public void pauseGame() {
		synchronized (mPauseLock) {
			mPaused = true;
		}
	}

	public void resumeGame() {
		synchronized (mPauseLock) {
			mPaused = false;
			mPauseLock.notifyAll();
		}
	}

	@Override
	public void run() {
		mLastTime = SystemClock.uptimeMillis();
		long fpsTime = SystemClock.uptimeMillis();
		while (!mFinished) {
			if(mGameManager.getInitialized() == false) {
				if(mGameRenderer.isLoadAllComplete()) {
					mGameManager.init();
				} else {
					try {
						// TODO - be careful here, is it OK to have the thread only check if loading
						//		  is done once every second?
						Thread.sleep(MILLISECONDS_PER_SECOND);
					} catch (InterruptedException e) {}
				}
			} else {
				final long time = SystemClock.uptimeMillis();
				final long timeDelta = time - mLastTime;
				long finalDelta = timeDelta;

				if(time - fpsTime >= MILLISECONDS_PER_SECOND) {
					fpsTime = time;
					mDWindow.updateTextBlock("FPS", Integer.toString(GameRenderer.mDebugFPSCounter));
					mDWindow.updateTextBlock("UPS", Integer.toString(mDebugUPSCounter));
					GameRenderer.mDebugFPSCounter = 0;
					mDebugUPSCounter = 0;
				}

				//Log.d("DEBUG", "time delta: " + finalDelta);
				if(finalDelta > MAX_TIME_DELTA_MS) {
					finalDelta = MAX_TIME_DELTA_MS;
				}
				if (finalDelta > MIN_TIME_DELTA_MS) {
					mLastTime = time;
					mGameManager.update(finalDelta);
					mDWindow.update(finalDelta);
					mGameRenderer.waitDrawingComplete();
					BaseObject.sSystemRegistry.mRenderSystem.sendUpdates(mGameRenderer);
					mDebugUPSCounter++;
				}

				if (finalDelta < MIN_TIME_DELTA_MS) {
					try {
						Thread.sleep(MIN_TIME_DELTA_MS - finalDelta);
					} catch (InterruptedException e) {
						// Interruptions here are no big deal.
					}
				}

				// Log.d("DEBUG", "right before pause block");
				synchronized (mPauseLock) {
					if (mPaused) {
						while (mPaused) {
							try {
								Log.d("DEBUG", "paused...");
								mPauseLock.wait();
							} catch (InterruptedException e) {
								// No big deal if this wait is interrupted.
							}
							Log.d("DEBUG", "pause lock awoken!");
						}
					}
				}
			}
		}
		BaseObject.sSystemRegistry.mRenderSystem.emptyDrawQueues(mGameRenderer);
		BaseObject.sSystemRegistry.mRenderSystem.emptyWriteQueues(mGameRenderer);
	}
	
	public boolean getPaused() {
		return mPaused;
	}
	
	public GameRenderer getGameRenderer() {
		return mGameRenderer;
	}
	
	public void setGameRenderer(GameRenderer gRenderer) {
		mGameRenderer = gRenderer;
	}

	public void setGameManager(GameManager gManager) {
		mGameManager = gManager;
	}
}