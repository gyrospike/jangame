package org.star.circuit;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import org.star.common.game.AssetLibrary;
import org.star.common.game.BaseObject;
import org.star.common.game.GameRenderer;

public class MenuSurfaceView extends GLSurfaceView {

	/** the game renderer that draws the menu */
	private GameRenderer mOpenGL;

	public MenuSurfaceView(Context context) {
		super(context);
		init(context);
	}

	public MenuSurfaceView(Context context, AttributeSet attributes) {
		super(context, attributes);
		init(context);
	}

	/**
	 * create the asset library and create the menu renderer
	 * 
	 * @param context
	 */
	private void init(Context context) {
		mOpenGL = new GameRenderer(context, AssetLibrary.TEXTURE_TYPE_MENU);
		setRenderer(mOpenGL);
	}

	@Override
	public void onPause() {
		super.onPause();
		mOpenGL.pause();
		BaseObject.sSystemRegistry.mAssetLibrary
				.prepForReload(AssetLibrary.TEXTURE_TYPE_MENU);
	}

	/**
	 * @return game renderer
	 */
	public GameRenderer getRenderer() {
		return mOpenGL;
	}

}
