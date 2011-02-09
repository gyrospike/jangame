package org.alchemicstudio;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GameRenderer implements Renderer {

	private Context mContext;
	private FixedSizeArray<Sprite> spriteList;
	private float originX, originY;
	private float xCamera, yCamera;
	private boolean setOrigin = false;
	private Object mDrawLock;
	private boolean mDrawQueueChanged;
	private int mHeight;
	private int mWidth;

	public GameRenderer(Context context) {
		mContext = context;
		mDrawLock = new Object();
		mDrawQueueChanged = false;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("DEBUG", "onSurfaceCreated was called");
		loadTextures(gl, BaseObject.sSystemRegistry.longTermTextureLibrary);

		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc (GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
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
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	public void onDrawFrame(GL10 gl) {

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		// gl.glTranslatef(0.0f, 0.0f, -5.0f);

		synchronized (mDrawLock) {
			if (!mDrawQueueChanged) {
				while (!mDrawQueueChanged) {
					try {
						mDrawLock.wait();
						// Log.d("DEBUG", "waiting for mDrawLock");
					} catch (InterruptedException e) {
						// No big deal if this wait is interrupted.
					}
				}
			}
			mDrawQueueChanged = false;
		}

		viewOrtho(gl, mWidth, mHeight);

		synchronized (this) {
			if (spriteList != null) {
				Object[] objectArray = spriteList.getArray();
				float x;
				float y;
				final int count = objectArray.length;
				for (int i = 0; i < count; i++) {
					if (objectArray[i] != null) {
						Sprite currentSprite = (Sprite) objectArray[i];
						x = currentSprite.xOffset;
						y = currentSprite.yOffset;
						/*
						 * if (spriteArray[i].cameraRelative) { x = (x +
						 * xCamera) + (mWidth/2); y = (y + yCamera) +
						 * (mHeight/2); }
						 */
						currentSprite.draw(gl, 0, x, y);
					}
				}
			} else if (spriteList == null) {
				// If we have no draw queue, clear the screen. If we have a draw
				// queue that
				// is empty, we'll leave the frame buffer alone.
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			}
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

	public synchronized void setDrawQueue(FixedSizeArray<Sprite> sList) {
		spriteList = sList;
		synchronized (mDrawLock) {
			mDrawQueueChanged = true;
			mDrawLock.notify();
		}
	}

	public void resetPosition() {
		setOrigin = false;
	}

	public void setPosition(float x, float y) {
		if (!setOrigin) {
			originX = x;
			originY = y;
			setOrigin = true;
		}

		xCamera += -(originX - x);
		yCamera += (originY - y);

		originX = x;
		originY = y;
	}

	public synchronized void onPause() {
		// Stop waiting to avoid deadlock.
		// TODO: this is a hack. Probably this renderer
		// should just use GLSurfaceView's non-continuous render
		// mode.
		unloadTextures(BaseObject.sSystemRegistry.longTermTextureLibrary);
		synchronized (mDrawLock) {
			mDrawQueueChanged = true;
			mDrawLock.notify();
		}
	}

	public synchronized void waitDrawingComplete() {
	}
}
