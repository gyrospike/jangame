package org.alchemicstudio;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class MenuBackgroundView extends GLSurfaceView {

	private GameRenderer mOpenGL;

	public MenuBackgroundView(Context context) {
		super(context);
		init(context);
	}

	public MenuBackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * create the asset library and create the menu renderer
	 * 
	 * @param context
	 */
	private void init(Context context) {
		BaseObject.sSystemRegistry.mAssetLibrary.loadMenuTextures();
		mOpenGL = new GameRenderer(context, AssetLibrary.TEXTURE_TYPE_MENU);
		setRenderer(mOpenGL);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mOpenGL.pause();
		BaseObject.sSystemRegistry.mAssetLibrary.prepForReload(AssetLibrary.TEXTURE_TYPE_MENU);
	}
	
	/**
	 * @return	game renderer
	 */
	public GameRenderer getRenderer() {
		return mOpenGL;
	}
	
}
