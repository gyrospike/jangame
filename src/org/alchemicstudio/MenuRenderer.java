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
	
	private Sprite currentSprite;
	private Sprite backgroundSprite;
	
	private float pixToDpiScale;

	public MenuRenderer(Context context) {
		mContext = context;
		
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		pixToDpiScale = metrics.densityDpi / 160.0f;
		
		TextureLibrary longTermTextureLibrary = new TextureLibrary();
		BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;
		
		currentSprite = new Sprite(0, 3, 300);
		currentSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.grey_gate_node), 32 / pixToDpiScale, 32 / pixToDpiScale);
		currentSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.yellow_gate_node), 32 / pixToDpiScale, 32 / pixToDpiScale);
		currentSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.green_gate_node), 32 / pixToDpiScale, 32 / pixToDpiScale);
		
		backgroundSprite = new Sprite(0, 1);
		backgroundSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.robo), 128 / pixToDpiScale, 128 / pixToDpiScale);
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

		Log.d("DEBUG", "screen dimsensions, dpi: " + mWidth + ", " + mHeight);
		int newWidth = (width * 240 / 160);
		int newHeight = (height * 240 / 160);
		Log.d("DEBUG", "screen dimsensions, pix: " + newWidth + ", " + newHeight);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		// GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f,
		// 100.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	private void viewOrtho(GL10 gl, int w, int h) {
		gl.glEnable(GL10.GL_BLEND);
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, w, -h, 0, -1, 1);
		// Log.d("DEBUG", "w, h: " + w + ", " + h);
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
	}

	private void viewPerspective(GL10 gl) {
		gl.glDisable(GL10.GL_BLEND);
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	public void onDrawFrame(GL10 gl) {

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		viewOrtho(gl, mWidth, mHeight);

		synchronized (this) {
			currentSprite.draw(gl, 0, 32, -32);
			backgroundSprite.draw(gl, 0, 64 * pixToDpiScale, -854 + (64 * pixToDpiScale));
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
