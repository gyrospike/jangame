
package org.alchemicstudio;

import android.os.SystemClock;
import android.util.Log;

class MenuRunnable implements Runnable {

	/** minimum time that must elapse before the game can be updated again */
	private static final int MIN_TIME_DELTA_MS = 16;
	
	/** game time at last update */
	private long mLastTime;
	
	/** true when the game loop is over */
	private boolean mFinished;
	
	/** reference to the game manager, the game logic admin */
	private MenuManager mManager;
	
	/** reference to the game renderer, the game drawer */
	private GameRenderer mGameRenderer;

	/** true when the game thread is paused */
	private boolean mPaused = false;
	
	/** pause lock object, I don't know too much about this */
	private Object mPauseLock;

	/**
	 * controls the main update loop, has hooks into the game manager, which updates game logic, and
	 * the game renderer, which draws the game
	 */
	public MenuRunnable() {
		mPauseLock = new Object();
		
		mFinished = false;
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
		while (!mFinished) {
			if(mManager.getInitialized() == false) {
				if(mGameRenderer.isLoadAllComplete()) {
					mManager.init();
				} else {
					try {
						// TODO - be careful here, is it OK to have the thread only check if loading
						//		  is done once every second?
						Thread.sleep(GameRunnable.MILLISECONDS_PER_SECOND);
					} catch (InterruptedException e) {}
				}
			} else {
				mGameRenderer.waitDrawingComplete();

				final long time = SystemClock.uptimeMillis();
				final long timeDelta = time - mLastTime;
				long finalDelta = timeDelta;

				if (timeDelta > MIN_TIME_DELTA_MS) {
					mLastTime = time;
					mManager.update(timeDelta);
					BaseObject.sSystemRegistry.mRenderSystem.sendUpdates(mGameRenderer);
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
						Log.d("DEBUG", "first run: paused");
						while (mPaused) {
							try {
								Log.d("DEBUG", "paused...");
								mPauseLock.wait();
							} catch (InterruptedException e) {
								// No big deal if this wait is interrupted.
							}
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

	public void setGameManager(MenuManager gManager) {
		mManager = gManager;
	}
}