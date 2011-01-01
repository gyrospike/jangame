package jan.game;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

public class GameRenderer implements Renderer {

	private Context mContext;

	private Sprite[] spriteArray;

	private float originX, originY;
	private float xCamera, yCamera;
	private boolean setOrigin = false;
	private Object mDrawLock;
	private boolean mDrawQueueChanged;

	public GameRenderer(Context context) {
		mContext = context;
		mDrawLock = new Object();
		mDrawQueueChanged = false;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		loadTextures(gl, BaseObject.sSystemRegistry.longTermTextureLibrary);

		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
		
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
	}

	private void viewOrtho(GL10 gl, int w, int h) { // Set Up An Ortho View
		gl.glMatrixMode(gl.GL_PROJECTION); // Select Projection
		gl.glPushMatrix(); // Push The Matrix
		gl.glLoadIdentity(); // Reset The Matrix
		w = 2;
		h = 2;
		gl.glOrthof(-w / 2, w / 2, -h / 2, h / 2, -1, 1);
		gl.glMatrixMode(gl.GL_MODELVIEW); // Select Modelview Matrix
		gl.glPushMatrix(); // Push The Matrix
		gl.glLoadIdentity(); // Reset The Matrix
	}

	private void viewPerspective(GL10 gl) // Set Up A Perspective View
	{
		gl.glMatrixMode(gl.GL_PROJECTION); // Select Projection
		gl.glPopMatrix(); // Pop The Matrix
		gl.glMatrixMode(gl.GL_MODELVIEW); // Select Modelview
		gl.glPopMatrix(); // Pop The Matrix
	}


	public void onDrawFrame(GL10 gl) {

		synchronized (mDrawLock) {
			if (!mDrawQueueChanged) {
				while (!mDrawQueueChanged) {
					try {
						mDrawLock.wait();
					} catch (InterruptedException e) {
						// No big deal if this wait is interrupted.
					}
				}
			}
			mDrawQueueChanged = false;
		}

		viewOrtho(gl, 480, 854);
		
		synchronized (this) {
			if (spriteArray != null) {
				float x;
				float y;
				final int count = spriteArray.length;
				for (int i = 0; i < count; i++) {
					if (spriteArray[i] != null) {
						// Log.d("DEBUG", "drawArray index: " + i + " out of " +
						// count);
						x = spriteArray[i].xOffset;
						y = spriteArray[i].yOffset;
						if (spriteArray[i].cameraRelative) {
							x = (x + xCamera) + 160;
							y = (y + yCamera) + 240;
						}
						spriteArray[i].draw(gl, 0, 0, 0);
					}
				}
			}
		}

		viewPerspective(gl);
	}

	public void loadTextures(GL10 gl, TextureLibrary library) {
		if (gl != null) {
			library.loadAll(mContext, gl);
		}
	}
	
	public synchronized void setDrawQueue(Sprite[] dArray) {
		// Log.d("DEBUG", "resetting spriteArray");
		spriteArray = dArray;
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
		// should just use GLSurfaceView's non-continuious render
		// mode.
		synchronized (mDrawLock) {
			mDrawQueueChanged = true;
			mDrawLock.notify();
		}
	}

	public synchronized void waitDrawingComplete() {
	}
}
