package jan.game;

import android.content.Context;
import android.opengl.GLSurfaceView;

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