package jan.game;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

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