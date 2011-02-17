package org.alchemicstudio;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLUtils;

public class LabelMaker {

	private static final int STATE_NEW = 0;
	private static final int STATE_INITIALIZED = 1;
	private static final int STATE_ADDING = 2;
	private static final int STATE_DRAWING = 3;

	private float mTexelWidth; // Convert texel to U
	private float mTexelHeight; // Convert texel to V
	
	private int mStrikeWidth;
	private int mStrikeHeight;
	private int mTextureID;
	private int mU;
	private int mV;
	private int mLineHeight;
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
		mU = 0;
		mV = 0;
		mLineHeight = 0;
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
		return add(gl, null, text, textPaint);
	}

	public int add(GL10 gl, Drawable background, String text, Paint textPaint) {
		return add(gl, background, text, textPaint, 0, 0);
	}

	public int add(GL10 gl, Drawable drawable, int minWidth, int minHeight) {
		return add(gl, drawable, null, null, minWidth, minHeight);
	}

	public int add(GL10 gl, Drawable background, String text, Paint textPaint,
			int minWidth, int minHeight) {
		checkState(STATE_ADDING, STATE_ADDING);
		boolean drawBackground = background != null;
		boolean drawText = (text != null) && (textPaint != null);

		Rect padding = new Rect();
		if (drawBackground) {
			background.getPadding(padding);
			minWidth = Math.max(minWidth, background.getMinimumWidth());
			minHeight = Math.max(minHeight, background.getMinimumHeight());
		}

		int ascent = 0;
		int descent = 0;
		int measuredTextWidth = 0;
		if (drawText) {
			// Paint.ascent is negative, so negate it.
			ascent = (int) Math.ceil(-textPaint.ascent());
			descent = (int) Math.ceil(textPaint.descent());
			measuredTextWidth = (int) Math.ceil(textPaint.measureText(text));
		}
		int textHeight = ascent + descent;
		int textWidth = Math.min(mStrikeWidth, measuredTextWidth);

		int padHeight = padding.top + padding.bottom;
		int padWidth = padding.left + padding.right;
		int height = Math.max(minHeight, textHeight + padHeight);
		int width = Math.max(minWidth, textWidth + padWidth);
		int effectiveTextHeight = height - padHeight;
		int effectiveTextWidth = width - padWidth;

		int centerOffsetHeight = (effectiveTextHeight - textHeight) / 2;
		int centerOffsetWidth = (effectiveTextWidth - textWidth) / 2;

		// Make changes to the local variables, only commit them
		// to the member variables after we've decided not to throw
		// any exceptions.

		int u = mU;
		int v = mV;
		int lineHeight = mLineHeight;

		if (width > mStrikeWidth) {
			width = mStrikeWidth;
		}

		// Is there room for this string on the current line?
		if (u + width > mStrikeWidth) {
			// No room, go to the next line:
			u = 0;
			v += lineHeight;
			lineHeight = 0;
		}
		lineHeight = Math.max(lineHeight, height);
		if (v + lineHeight > mStrikeHeight) {
			throw new IllegalArgumentException("Out of texture space.");
		}

		int u2 = u + width;
		int vBase = v + ascent;
		int v2 = v + height;

		if (drawBackground) {
			background.setBounds(u, v, u + width, v + height);
			background.draw(mCanvas);
		}

		if (drawText) {
			mCanvas.drawText(text, u + padding.left + centerOffsetWidth, vBase
					+ padding.top + centerOffsetHeight, textPaint);
		}

		TextGrid grid = new TextGrid(2, 2);
		// Grid.set arguments: i, j, x, y, z, u, v

		float texU = u * mTexelWidth;
		float texU2 = u2 * mTexelWidth;
		float texV = 1.0f - v * mTexelHeight;
		float texV2 = 1.0f - v2 * mTexelHeight;

		grid.set(0, 0, 0.0f, 0.0f, 0.0f, texU, texV2);
		grid.set(1, 0, width, 0.0f, 0.0f, texU2, texV2);
		grid.set(0, 1, 0.0f, height, 0.0f, texU, texV);
		grid.set(1, 1, width, height, 0.0f, texU2, texV);

		// We know there's enough space, so update the member variables
		mU = u + width;
		mV = v;
		mLineHeight = lineHeight;
		mLabels.add(new Label(grid, width, height, ascent, u, v + height,
				width, -height));
		return mLabels.getCount() - 1;
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
		
		public TextGrid grid;
        public float width;
        public float height;
        public float baseline;
        public int[] mCrop;
        
        public Label(TextGrid grid, float width, float height, float baseLine,
                int cropU, int cropV, int cropW, int cropH) {
            this.grid = grid;
            this.width = width;
            this.height = height;
            this.baseline = baseLine;
            int[] crop = new int[4];
            crop[0] = cropU;
            crop[1] = cropV;
            crop[2] = cropW;
            crop[3] = cropH;
            mCrop = crop;
        }
    }
}
