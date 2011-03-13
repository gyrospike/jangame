package org.alchemicstudio;

import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity {

	private static final int INPUT_QUEUE_SIZE = 32;
	private ArrayBlockingQueue<InputObject> inputObjectPool;
	private GameThread gameThread;
	private OGLSurfaceView mGLView;
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
		
		int mapNumber = 0;
		Bundle extras = getIntent().getExtras(); 
		if(extras !=null && getIntent().hasExtra("mapNumber"))
		{
			mapNumber = extras.getInt("mapNumber");
		}

		mGLView = new OGLSurfaceView(this);
		setContentView(mGLView);

		mGame = new Game(metrics.widthPixels, metrics.heightPixels);
		mGame.setSurfaceView((OGLSurfaceView) mGLView);
		mGame.bootstrap(this, mapNumber);

		gameThread = mGame.getGameThread();
		createInputObjectPool();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
        mGame.pause();
        mGLView.onPause();
        //instructs renderer to invalidate all textures so that they can be reloaded onResume
        //some sort of bug exists if you switch between activities too fast, need to lock input down during loading
        gameThread.getRenderer().onPause();
		Log.d("DEBUG", "Game paused");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mGame.resume();
        //mGame.onResume(this, false);
        mGLView.onResume();
        Log.d("DEBUG", "Game resumed");
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
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
					InputObject input = inputObjectPool.take();
					input.useEventHistory(event, i);
					gameThread.feedInput(input);
				}
			}
			// current last
			InputObject input = inputObjectPool.take();
			input.useEvent(event);
			gameThread.feedInput(input);
		} catch (InterruptedException e) {
		}
		// don't allow more than 60 motion events per second
		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
		}
		return true;
	}

	private void createInputObjectPool() {
		inputObjectPool = new ArrayBlockingQueue<InputObject>(INPUT_QUEUE_SIZE);
		for (int i = 0; i < INPUT_QUEUE_SIZE; i++) {
			inputObjectPool.add(new InputObject(inputObjectPool));
		}
	}
}