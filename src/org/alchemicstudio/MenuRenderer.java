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
		
		TextureLibrary longTermTextureLibrary = new TextureLibrary();
		BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;
		
		roboSprite = new Sprite(0, 4, 300);
		roboSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.gold1), 50.0f, 64.0f);
		roboSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.gold2), 50.0f, 64.0f);
		roboSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.gold3), 50.0f, 64.0f);
		roboSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.gold4), 50.0f, 64.0f);
		
		backgroundHeadSprite = new Sprite(0, 1);
		backgroundHeadSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.bg_head), 480.0f, 480.0f);
		
		backgroundBaseSprite = new Sprite(0, 1);
		backgroundBaseSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.bg_base), 480.0f, 480.0f);
		
		menuPipeSprite1 = new Sprite(0, 1);
		menuPipeSprite1.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.menu_pipe), 32.0f, 32.0f);
		menuPipeSprite1.setScale(15, 1);
		menuPipeSprite1.modTex(6.0f);
		
		menuPipeSprite2 = new Sprite(0, 1);
		menuPipeSprite2.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.menu_pipe), 32.0f, 32.0f);
		menuPipeSprite2.setScale(15, 1);
		menuPipeSprite2.modTex(6.0f);
		
		menuPipeSprite3 = new Sprite(0, 1);
		menuPipeSprite3.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.menu_pipe), 32.0f, 32.0f);
		menuPipeSprite3.setScale(15, 1);
		menuPipeSprite3.modTex(6.0f);
		
		redGearSprite = new Sprite(0, 1);
		redGearSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.red_gear), 52.0f, 52.0f);
		
		yellowGearSprite = new Sprite(0, 1);
		yellowGearSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.yellow_gear), 32.0f, 32.0f);
		
		pinkGearSprite = new Sprite(0, 1);
		pinkGearSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.pink_gear), 58.0f, 58.0f);
		
		craneSprite = new Sprite(0, 1);
		craneSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.crane), 100.0f, 120.0f);
		
		// marker is useful for showing where a certain dpi location is
		//myMarker = new Marker(10, 10);
		//myMarker.mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.red_box),(4.0f / pixToDpiScale), (4.0f / pixToDpiScale));
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		loadTextures(gl, BaseObject.sSystemRegistry.longTermTextureLibrary);
		
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
			menuPipeSprite1.draw(gl, 0, 0, -422);
			menuPipeSprite1.draw(gl, 0, 0, -578);
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
	
	public void loadTextures(GL10 gl, TextureLibrary library) {
		if (gl != null) {
			library.loadAll(mContext, gl);
		}
	}

	public void unloadTextures(TextureLibrary library) {
		library.invalidateAll();
	}
	
	public synchronized void onPause() {
		Log.d("DEBUG", "Menu is now paused");
		unloadTextures(BaseObject.sSystemRegistry.longTermTextureLibrary);
	}
}
