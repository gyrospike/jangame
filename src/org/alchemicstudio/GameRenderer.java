package org.alchemicstudio;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GameRenderer implements Renderer {

	private Context mContext;
	private FixedSizeArray<Sprite> spriteList;
	private FixedSizeArray<TextBox> textBoxList;
	private float originX, originY;
	private float xCamera, yCamera;
	private boolean setOrigin = false;
	private Object mDrawLock;
	private boolean mDrawQueueChanged;
	private int mHeight;
	private int mWidth;

	private LabelMaker mLabels;
	private Paint mLabelPaint;

	public GameRenderer(Context context) {
		mContext = context;
		mDrawLock = new Object();
		mDrawQueueChanged = false;

		Typeface myFont = Typeface.createFromAsset(context.getAssets(), "fonts/AGENCYR.TTF");

		mLabelPaint = new Paint();
		mLabelPaint.setTypeface(myFont);
		mLabelPaint.setTextSize(24);
		mLabelPaint.setAntiAlias(true);
		mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("DEBUG", "onSurfaceCreated was called");
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

		if (mLabels != null) {
			mLabels.shutdown(gl);
		} else {
			mLabels = new LabelMaker(256, 64);
		}
		mLabels.initialize(gl);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (height == 0) {
			height = 1;
		}

		mWidth = width;
		mHeight = height;

		Log.d("DEBUG", "game screen dimsensions, dpi: " + mWidth + ", " + mHeight);

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
		// Log.d("DEBUG", "w, h: " + w + ", " + h);
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
						currentSprite.draw(gl, 0, x, y);
					}
				}
			} else if (spriteList == null) {
				// If we have no draw queue, clear the screen. If we have a draw
				// queue that
				// is empty, we'll leave the frame buffer alone.
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			}

			viewPerspective(gl);
			
			//having the text drawing done outside of the sync caused flickering, possibly more problems too

			if (textBoxList != null) {
				Object[] objectArray = textBoxList.getArray();
				final int len = objectArray.length;
				mLabels.beginAdding(gl);
				for (int h = 0; h < len; h++) {
					if (objectArray[h] != null) {
						TextBox currentTextBox = (TextBox) objectArray[h];
						currentTextBox.index = mLabels.add(gl, currentTextBox.theText, mLabelPaint);
					}
				}
				mLabels.endAdding(gl);
				mLabels.beginDrawing(gl, mWidth, mHeight);
				for (int g = 0; g < len; g++) {
					if (objectArray[g] != null) {
						TextBox currentTextBox = (TextBox) objectArray[g];
						mLabels.draw(gl, currentTextBox.posX, currentTextBox.posY, currentTextBox.index);
					}
				}
				mLabels.endDrawing(gl);
			} else if (textBoxList == null) {
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			}
		}
	}

	public void loadTextures(GL10 gl, TextureLibrary library) {
		if (gl != null) {
			library.loadAll(mContext, gl);
		}
	}

	public void unloadTextures(TextureLibrary library) {
		library.invalidateAll();
	}

	public synchronized void setDrawQuadQueue(FixedSizeArray<Sprite> sList) {
		spriteList = sList;
		synchronized (mDrawLock) {
			mDrawQueueChanged = true;
			mDrawLock.notify();
		}
	}

	public synchronized void setTextBoxQueue(FixedSizeArray<TextBox> tList) {
		textBoxList = tList;
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

	public void setContext(Context newContext) {
		mContext = newContext;
	}
}
