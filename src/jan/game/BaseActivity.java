package jan.game;

import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends Activity {

	private static final int INPUT_QUEUE_SIZE = 16;
	private ArrayBlockingQueue<InputObject> inputObjectPool;
	private GameThread gameThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		GLSurfaceView mGLView = new OGLSurfaceView(this);
		setContentView(mGLView);

		Game mGame = new Game();
		mGame.setSurfaceView((OGLSurfaceView) mGLView);
		mGame.bootstrap(this);
		
		gameThread = mGame.getGameThread();
		createInputObjectPool();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//Log.d("DEBUG", "toucha toucha touch me!");
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