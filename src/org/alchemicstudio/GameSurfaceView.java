package org.alchemicstudio;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameSurfaceView extends GLSurfaceView {

	/** the game renderer object */
	private GameRenderer mOpenGL;
	
	/**
	 * Constructor for Open GL SurfaceView
	 * 
	 * @param context
	 */
	public GameSurfaceView(Context context) {
		super(context);
		init(context);
	}
	
	/**
	 * Constructor for Open GL SurfaceView
	 * 
	 * @param context
	 * @param attrs		this param is required if you are creating the view via a layout xml definition
	 */
	public GameSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * initialize the surface view by creating the game renderer
	 * 
	 * @param context
	 */
	private void init(Context context) {
		mOpenGL = new GameRenderer(context, AssetLibrary.TEXTURE_TYPE_GAME);
		setRenderer(mOpenGL);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mOpenGL.pause();
		BaseObject.sSystemRegistry.mAssetLibrary.prepForReload(AssetLibrary.TEXTURE_TYPE_GAME);
	}
	
	/**
	 * @return	game renderer
	 */
	public GameRenderer getGameRenderer() {
		return mOpenGL;
	}
}