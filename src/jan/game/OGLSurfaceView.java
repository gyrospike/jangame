package jan.game;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.TextView;

class OGLSurfaceView extends GLSurfaceView {

	private GameRenderer mOpenGL;

	public OGLSurfaceView(Context context) {
		super(context);
		
		mOpenGL = new GameRenderer(context);
		setRenderer(mOpenGL);
	}
	
	public GameRenderer getRenderer() {
		return mOpenGL;
	}
}