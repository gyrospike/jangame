package org.alchemicstudio;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;
import android.util.Log;

public class MenuRenderer implements Renderer {

	private Context mContext;
	private int mHeight;
	private int mWidth;
	
	private Marker myMarker;
	private Sprite roboSprite;
	private Sprite backgroundHeadSprite;
	private Sprite backgroundBaseSprite;
	private Sprite redGearSprite;
	private Sprite yellowGearSprite;
	private Sprite pinkGearSprite;
	private Sprite craneSprite;
	private Sprite menuPipeSprite1;
	private Sprite menuPipeSprite2;
	private Sprite menuPipeSprite3;
	
	private float angle;
	
	private float pixToDpiScale;

	public MenuRenderer(Context context) {
		mContext = context;
		
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		pixToDpiScale = metrics.densityDpi / 160.0f;
		Log.d("DEBUG", "density: " + metrics.densityDpi);
		Log.d("DEBUG", "metrics: " + pixToDpiScale);
		
		BaseObject.sSystemRegistry.mAssetLibrary.loadMenuTextures();
		
		int[] spriteArray = {R.drawable.gold1, R.drawable.gold2, R.drawable.gold3, R.drawable.gold4};
		roboSprite = new Sprite(spriteArray, 0, 50.0f, 64.0f, 4, 300);
		
		int[] spriteArray2 = {R.drawable.bg_head};
		backgroundHeadSprite = new Sprite(spriteArray2, 0, 480.0f, 480.0f, 1, 0);
		
		int[] spriteArray3 = {R.drawable.bg_base};
		backgroundBaseSprite = new Sprite(spriteArray3, 0, 480.0f, 480.0f, 1, 0);
		
		int[] spriteArray4 = {R.drawable.menu_pipe};
		menuPipeSprite1 = new Sprite(spriteArray4, 0, 32.0f, 32.0f, 1, 0);
		menuPipeSprite1.setScale(15, 1);
		menuPipeSprite1.modTex(6.0f);
		
		menuPipeSprite2 = new Sprite(spriteArray4, 0, 32.0f, 32.0f, 1, 0);
		menuPipeSprite2.setScale(15, 1);
		menuPipeSprite2.modTex(6.0f);
		
		menuPipeSprite3 = new Sprite(spriteArray4, 0, 32.0f, 32.0f, 1, 0);
		menuPipeSprite3.setScale(15, 1);
		menuPipeSprite3.modTex(6.0f);
		
		int[] spriteArray5 = {R.drawable.red_gear};
		redGearSprite = new Sprite(spriteArray5, 0, 52.0f, 52.0f, 1, 0);
		
		int[] spriteArray6 = {R.drawable.yellow_gear};
		yellowGearSprite = new Sprite(spriteArray6, 0, 32.0f, 32.0f, 1, 0);
		
		int[] spriteArray7 = {R.drawable.pink_gear};
		pinkGearSprite = new Sprite(spriteArray7, 0, 58.0f, 58.0f, 1, 0);
		
		int[] spriteArray8 = {R.drawable.crane};
		craneSprite = new Sprite(spriteArray8, 0, 100.0f, 120.0f, 1, 0);
		
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		loadTextures(gl, BaseObject.sSystemRegistry.mAssetLibrary);
		
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (height == 0) {
			height = 1;
		}

		mWidth = width;
		mHeight = height;

		Log.d("DEBUG", "menu screen dimsensions, dpi: " + mWidth + ", " + mHeight);
		//int newWidth = (width * 240 / 160);
		//int newHeight = (height * 240 / 160);
		//Log.d("DEBUG", "menu screen dimsensions, pix: " + newWidth + ", " + newHeight);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	private void viewOrtho(GL10 gl, int w, int h) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, w, -h, 0, -1, 1);
		//Log.d("DEBUG", "w, h: " + w + ", " + h);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
	}

	private void viewPerspective(GL10 gl) {
		gl.glDisable(GL10.GL_BLEND);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	public void onDrawFrame(GL10 gl) {

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		viewOrtho(gl, mWidth, mHeight);

		synchronized (this) {
			
			//draws from bottom left corner
			menuPipeSprite1.draw(gl, 0, 0, -264);
			menuPipeSprite2.draw(gl, 0, 0, -422);
			menuPipeSprite3.draw(gl, 0, 0, -578);
			backgroundHeadSprite.draw(gl, 0, 0, -480);
			backgroundBaseSprite.draw(gl, 0, 0, -854);
			roboSprite.draw(gl, 0, 392, -168);
			redGearSprite.draw(gl, -angle/3, 25, -830);
			yellowGearSprite.draw(gl, angle, 8, -792);
			pinkGearSprite.draw(gl, angle/2, 410, -830);
			craneSprite.draw(gl, 0, 340, -843);
			
			angle ++;
			//myMarker.mSprite.draw(gl, 0, 359, -130);
		}

		viewPerspective(gl);
	}
	
	public void loadTextures(GL10 gl, AssetLibrary library) {
		if (gl != null) {
			library.loadAll(mContext, gl);
		}
	}

	public void unloadTextures(AssetLibrary library) {
		library.invalidateTextures(AssetLibrary.TEXTURE_TYPE_MENU);
	}
	
	public synchronized void onPause() {
		Log.d("DEBUG", "Menu is now paused");
		unloadTextures(BaseObject.sSystemRegistry.mAssetLibrary);
	}
}
