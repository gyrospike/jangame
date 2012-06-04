package org.alchemicstudio;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLUtils;

public class LabelMaker {

	private static final int STATE_NEW = 0;
	private static final int STATE_INITIALIZED = 1;
	private static final int STATE_ADDING = 2;
	private static final int STATE_DRAWING = 3;

	private int mStrikeWidth;
	private int mStrikeHeight;
	private int mTextureID;
	private int mState;

	private Bitmap mBitmap;
	private Canvas mCanvas;

	private FixedSizeArray<Label> mLabels = new FixedSizeArray<Label>(2);

	public LabelMaker(int strikeWidth, int strikeHeight) {
		mStrikeWidth = strikeWidth;
		mStrikeHeight = strikeHeight;
	}

	public void initialize(GL10 gl) {
		mState = STATE_INITIALIZED;
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);
		mTextureID = textures[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

		// Use Nearest for performance.
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
	}

	public void beginAdding(GL10 gl) {
		checkState(STATE_INITIALIZED, STATE_ADDING);
		mLabels.clear();
		Bitmap.Config config = Bitmap.Config.ALPHA_8;
		mBitmap = Bitmap.createBitmap(mStrikeWidth, mStrikeHeight, config);
		mCanvas = new Canvas(mBitmap);
		mBitmap.eraseColor(0);
	}

	public void shutdown(GL10 gl) {
		if (gl != null) {
			if (mState > STATE_NEW) {
				int[] textures = new int[1];
				textures[0] = mTextureID;
				gl.glDeleteTextures(1, textures, 0);
				mState = STATE_NEW;
			}
		}
	}

	public float getWidth(int labelID) {
		return mLabels.get(labelID).width;
	}

	public float getHeight(int labelID) {
		return mLabels.get(labelID).height;
	}

	public int add(GL10 gl, String text, Paint textPaint) {
		int result = 0;
		checkState(STATE_ADDING, STATE_ADDING);
		boolean drawText = (text != null) && (textPaint != null);
		if (drawText) {
			// Paint.ascent is negative, so negate it.
			int ascent = (int) Math.ceil(-textPaint.ascent());
			int descent = (int) Math.ceil(textPaint.descent());
			int measuredTextWidth = (int) Math.ceil(textPaint.measureText(text));

			int textHeight = ascent + descent;
			int textWidth = Math.min(mStrikeWidth, measuredTextWidth);

			String[] textSegments = text.split("\n");
			int numberOfLines = textSegments.length;

			for(int i = 0; i < numberOfLines; i++) {
				mCanvas.drawText(textSegments[i], 0, ascent + (i*textHeight), textPaint);
			}

			int totalHeight = numberOfLines*textHeight;

			// have to pass in V as height and height as negative, else the text is written upside down :)
			mLabels.add(new Label(textWidth, totalHeight, 0, totalHeight, textWidth, -totalHeight));
			result = mLabels.getCount() - 1;
		}
		return result;
	}

	public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
		checkState(STATE_INITIALIZED, STATE_DRAWING);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, viewWidth, 0.0f, viewHeight, 0.0f, 1.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		// Magic offsets to promote consistent rasterization.
		//gl.glTranslatef(0.375f, 0.375f, 0.0f);
	}

	public void draw(GL10 gl, float x, float y, int labelID) {
		checkState(STATE_DRAWING, STATE_DRAWING);
		gl.glPushMatrix();

		Label label = mLabels.get(labelID);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, label.mCrop, 0);
		((GL11Ext)gl).glDrawTexfOES(x, y, 0, label.width, label.height);
		gl.glPopMatrix();
	}

	public void endDrawing(GL10 gl) {
		checkState(STATE_DRAWING, STATE_INITIALIZED);
		gl.glDisable(GL10.GL_BLEND);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
	}

	public void endAdding(GL10 gl) {
		checkState(STATE_ADDING, STATE_INITIALIZED);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
		// Reclaim storage used by bitmap and canvas.
		mBitmap.recycle();
		mBitmap = null;
		mCanvas = null;
	}

	private void checkState(int oldState, int newState) {
		if (mState != oldState) {
			throw new IllegalArgumentException("Can't call this method now.");
		}
		mState = newState;
	}

	private static class Label {
		
		/** width of label */
		public float width;
		
		/** height of label */
		public float height;
		
		/** [0] - U, [1] - V, [2] - width, [3] - height - the cropping array used in glTexParameteriv */
		public int[] mCrop;

		public Label(float width, float height, int cropU, int cropV, int cropW, int cropH) {
			this.width = width;
			this.height = height;
			
			int[] crop = new int[4];
			crop[0] = cropU;
			crop[1] = cropV;
			crop[2] = cropW;
			crop[3] = cropH;
			mCrop = crop;
		}
	}
}
