package org.alchemicstudio;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class MenuBackgroundView extends GLSurfaceView {

	private MenuRenderer mOpenGL;

	public MenuBackgroundView(Context context) {
		super(context);
		init(context);
	}

	public MenuBackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mOpenGL = new MenuRenderer(context);
		setRenderer(mOpenGL);
	}
}
