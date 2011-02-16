package org.alchemicstudio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
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

	private LabelMaker mLabels;
	private int mLabelA;
	private Paint mLabelPaint;
	private Triangle mTriangle;
	private float[] mScratch = new float[8];

	public GameRenderer(Context context) {
		mContext = context;
		mDrawLock = new Object();
		mDrawQueueChanged = false;

		mTriangle = new Triangle();
		mLabelPaint = new Paint();
		mLabelPaint.setTextSize(32);
		mLabelPaint.setAntiAlias(true);
		mLabelPaint.setARGB(0xff, 0x00, 0x00, 0x00);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("DEBUG", "onSurfaceCreated was called");
		loadTextures(gl, BaseObject.sSystemRegistry.longTermTextureLibrary);

		InputStream is = mContext.getResources().openRawResource(
				R.drawable.number_grid);
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Ignore.
			}
		}
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		if (mLabels != null) {
			mLabels.shutdown(gl);
		} else {
			mLabels = new LabelMaker(256, 64);
		}
		mLabels.initialize(gl);
		mLabels.beginAdding(gl);
		mLabelA = mLabels.add(gl, "A", mLabelPaint);
		mLabels.endAdding(gl);

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
		Log.d("DEBUG", "screen dimsensions, pix: " + newWidth + ", "
				+ newHeight);

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

	private void drawLabel(GL10 gl, int triangleVertex, int labelId) {
		float x = mTriangle.getX(triangleVertex);
		float y = mTriangle.getY(triangleVertex);
		mScratch[0] = x;
		mScratch[1] = y;
		mScratch[2] = 0.0f;
		mScratch[3] = 1.0f;
		float sx = mScratch[4];
		float sy = mScratch[5];
		float height = mLabels.getHeight(labelId);
		float width = mLabels.getWidth(labelId);
		float tx = sx - width * 0.5f;
		float ty = sy - height * 0.5f;
		mLabels.draw(gl, tx, ty, labelId);
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
		
		mTriangle.draw(gl);
		mLabels.beginDrawing(gl, mWidth, mHeight);
		drawLabel(gl, 0, mLabelA);
		mLabels.endDrawing(gl);
		
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

class Triangle {
	public Triangle() {

		// Buffers to be passed to gl*Pointer() functions
		// must be direct, i.e., they must be placed on the
		// native heap where the garbage collector cannot
		// move them.
		//
		// Buffers with multi-byte datatypes (e.g., short, int, float)
		// must have their byte order set to native order

		ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();

		ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTexBuffer = tbb.asFloatBuffer();

		ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
		ibb.order(ByteOrder.nativeOrder());
		mIndexBuffer = ibb.asShortBuffer();

		for (int i = 0; i < VERTS; i++) {
			for (int j = 0; j < 3; j++) {
				mFVertexBuffer.put(sCoords[i * 3 + j]);
			}
		}

		for (int i = 0; i < VERTS; i++) {
			for (int j = 0; j < 2; j++) {
				mTexBuffer.put(sCoords[i * 3 + j] * 2.0f + 0.5f);
			}
		}

		for (int i = 0; i < VERTS; i++) {
			mIndexBuffer.put((short) i);
		}

		mFVertexBuffer.position(0);
		mTexBuffer.position(0);
		mIndexBuffer.position(0);
	}

	public void draw(GL10 gl) {
		gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS,
				GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
	}

	public float getX(int vertex) {
		return sCoords[3 * vertex];
	}

	public float getY(int vertex) {
		return sCoords[3 * vertex + 1];
	}

	private final static int VERTS = 3;

	private FloatBuffer mFVertexBuffer;
	private FloatBuffer mTexBuffer;
	private ShortBuffer mIndexBuffer;
	// A unit-sided equalateral triangle centered on the origin.
	private final static float[] sCoords = {
			// X, Y, Z
			-0.5f, -0.25f, 0, 0.5f, -0.25f, 0, 0.0f, 0.559016994f, 0 };
}
