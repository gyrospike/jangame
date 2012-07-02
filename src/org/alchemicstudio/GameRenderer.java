package org.alchemicstudio;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GameRenderer implements Renderer {

	/** current context used for loading textures */
	private Context mContext;
	
	/** list of sprites to be drawn */
	private FixedSizeArray<Sprite> spriteList;
	
	/** list of the text boxes to be drawn */
	private FixedSizeArray<TextBox> textBoxList;
	
	/** lock object - TODO - study this more */
	private Object mDrawLock;
	
	/** has the draw queue been updated? */
	private boolean mDrawQueueChanged;
	
	/** height of the surface */
	private int mHeight;
	
	/** width of the surface */
	private int mWidth;

	/** handles open GL implementation of text boxes, polys with text textures */
	private LabelMaker mLabels;
	
	/** 
	 * the textures types that are being used by this game renderer, different
	 *	types means fewer textures in memory - TODO - might not need this concept
	 */
	private int mTextureTypes;

	public GameRenderer(Context context, int textureTypes) {
		mContext = context;
		mDrawLock = new Object();
		mDrawQueueChanged = false;
		mTextureTypes = textureTypes;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("DEBUG", "onSurfaceCreated was called");
		loadTextures(gl);

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
			mLabels = new LabelMaker(256, 256);
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
						Vector2 tempPos = currentSprite.getPosition();
						x = tempPos.x;
						y = tempPos.y;
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
						currentTextBox.setIndex(mLabels.add(gl, currentTextBox.getText(), currentTextBox.getPaint()));
					}
				}
				mLabels.endAdding(gl);
				mLabels.beginDrawing(gl, mWidth, mHeight);
				for (int g = 0; g < len; g++) {
					if (objectArray[g] != null) {
						TextBox currentTextBox = (TextBox) objectArray[g];
						mLabels.draw(gl, currentTextBox.getX(), currentTextBox.getY(), currentTextBox.getIndex());
					}
				}
				mLabels.endDrawing(gl);
			} else if (textBoxList == null) {
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			}
		}
	}

	public void loadTextures(GL10 gl) {
		if (gl != null) {
			BaseObject.sSystemRegistry.mAssetLibrary.loadAll(mTextureTypes, mContext, gl);
		}
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

	public synchronized void pause() {
		// Stop waiting to avoid deadlock.
		// TODO: this is a hack. Probably this renderer
		// should just use GLSurfaceView's non-continuous render
		// mode.
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
