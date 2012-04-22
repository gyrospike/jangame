package org.alchemicstudio;

import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class BaseActivity extends Activity {

	/** maximum number of input objects that we'll store*/
	public static final int INPUT_QUEUE_SIZE = 32;
	
	/** TODO */
	private ArrayBlockingQueue<InputObject> mInputObjectPool;
	
	/** The surface view that creates the game renderer */
	private OGLSurfaceView mGLView;
	
	/** the game, holds the logic and update loop, not the drawing */
	private Game mGame;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Log.d("DEBUG", "Real dpi: " + metrics.densityDpi);
		Log.d("DEBUG", "screen dimensions in dpi: " + metrics.widthPixels + " x " + metrics.heightPixels);
		
		setContentView(R.layout.game);
		mGLView = (OGLSurfaceView)findViewById(R.id.OGLSurfaceView01);
		
		Button button1 = (Button) findViewById(R.id.button1);

		mGame = new Game(metrics.widthPixels, metrics.heightPixels);
		mGame.setSurfaceView((OGLSurfaceView) mGLView);
		mGame.bootstrap(this, getIntent().getExtras(), button1);

		createInputObjectPool();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGame.pause();
		mGLView.onPause();
		/**
		 * TODO - some sort of bug exits here where if you switch between activities too fast
		 * the game crashes
		 */
		mGLView.getGameRenderer().onPause();
		Log.d("DEBUG", "Game paused");
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGame.resume();
		mGLView.onResume();
		Log.d("DEBUG", "Game resumed");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			mGame.stop();
			return true;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// we only care about down actions in this game.
		try {
			// history first
			int hist = event.getHistorySize();
			if (hist > 0) {
				// add from oldest to newest
				for (int i = 0; i < hist; i++) {
					InputObject input = mInputObjectPool.take();
					input.useEventHistory(event, i);
					mGame.getGameManager().feedInput(input);
				}
			}
			// current last
			InputObject input = mInputObjectPool.take();
			input.useEvent(event);
			mGame.getGameManager().feedInput(input);
		} catch (InterruptedException e) {
		}
		// don't allow more than 60 motion events per second
		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
		}
		return true;
	}

	/**
	 * initializes the input pool
	 */
	private void createInputObjectPool() {
		mInputObjectPool = new ArrayBlockingQueue<InputObject>(INPUT_QUEUE_SIZE);
		for (int i = 0; i < INPUT_QUEUE_SIZE; i++) {
			mInputObjectPool.add(new InputObject(mInputObjectPool));
		}
	}
}