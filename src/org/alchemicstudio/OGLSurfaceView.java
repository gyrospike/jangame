package org.alchemicstudio;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class OGLSurfaceView extends GLSurfaceView {

	/** the gaem renderer object */
	private GameRenderer mOpenGL;
	
	/**
	 * Constructor for Open GL SurfaceView
	 * 
	 * @param context
	 */
	public OGLSurfaceView(Context context) {
		super(context);
		init(context);
	}
	
	/**
	 * Constructor for Open GL SurfaceView
	 * 
	 * @param context
	 * @param attrs		this param is required if you are creating the view via a layout xml definition
	 */
	public OGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * initialize the surface view by creating the game renderer
	 * 
	 * @param context
	 */
	private void init(Context context) {
		mOpenGL = new GameRenderer(context);
		setRenderer(mOpenGL);
	}
	
	/**
	 * @return	game renderer
	 */
	public GameRenderer getGameRenderer() {
		return mOpenGL;
	}
}