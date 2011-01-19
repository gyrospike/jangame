package jan.game;

import java.util.concurrent.ArrayBlockingQueue;

import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

class GameThread implements Runnable {

	private long mLastTime;
	private GameManager gameManager;
	private SurfaceHolder mSurfaceHolder;
	private boolean mFinished;
	private GameRenderer mRenderer;
	private static final int INPUT_QUEUE_SIZE = 32; // equal to INPUT_QUEUE_SIZE
													// in BaseActivity,
													// otherwise will allow for
													// ANR (Application Not
													// Responding) error
	private ArrayBlockingQueue<InputObject> inputQueue = new ArrayBlockingQueue<InputObject>(INPUT_QUEUE_SIZE);
	private Object inputQueueMutex = new Object();
	private boolean mPaused = false;
	private Object mPauseLock;

	public GameThread(GameRenderer renderer) {
		mRenderer = renderer;
		mPauseLock = new Object();
	}

	public GameRenderer getRenderer() {
		return mRenderer;
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

	public boolean getPaused() {
		return mPaused;
	}

	public void setGameRoot(GameManager manager) {
		gameManager = manager;
	}

	@Override
	public void run() {
		mLastTime = SystemClock.uptimeMillis();
		while (!mFinished) {
			if (gameManager != null) {
				mRenderer.waitDrawingComplete();

				final long time = SystemClock.uptimeMillis();
				final long timeDelta = time - mLastTime;
				long finalDelta = timeDelta;

				if (timeDelta > 16) {
					mLastTime = time;
					processInput();
					gameManager.update(timeDelta, null);
					BaseObject.sSystemRegistry.renderSystem.sendUpdates(mRenderer);
				}

				if (finalDelta < 16) {
					try {
						Thread.sleep(16 - finalDelta);
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
		BaseObject.sSystemRegistry.renderSystem.emptyQueues(mRenderer);
	}

	public void feedInput(InputObject input) {
		// Log.d("DEBUG", "Got to feeding");
		synchronized (inputQueueMutex) {
			try {
				inputQueue.put(input);
			} catch (InterruptedException e) {
				Log.d(e.getMessage(), e.toString());
			}
		}
	}

	private void processInput() {
		synchronized (inputQueueMutex) {
			ArrayBlockingQueue<InputObject> inputQueue = this.inputQueue;
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

	private void processTouchMoveEvent(InputObject input) {
		// objectManager.checkNodePress(input.x, input.y);
		// objectManager.checkButtonPress(input.x, input.y);
		Log.d("DEBUG", "Create Particle at (" + input.x + ", " + input.y + ")");
		gameManager.createParticle(input.x, input.y);
	}

	private void processTouchDownEvent(InputObject input) {
		// objectManager.checkNodePress(input.x, input.y);
		// objectManager.checkButtonPress(input.x, input.y);
		Log.d("DEBUG", "Create Particle at (" + input.x + ", " + input.y + ")");
		gameManager.createParticle(input.x, input.y);
	}

	private void processTouchUpEvent(InputObject input) {
		// objectManager.checkNodeRelease(input.x, input.y);
	}
}