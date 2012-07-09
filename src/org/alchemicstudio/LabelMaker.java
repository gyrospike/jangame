package org.alchemicstudio;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLUtils;
import android.util.Log;

public class LabelMaker {

	/**
	 * 0000 0000 0000 0000 
	 * 0000 0000 0000 0000
	 * 0000 0000 0000 0000
	 * 0000 0000 0000 0000
	 * 
	 * 1221 1221 1221 1221
	 * 2112 2112 2112 2112
	 * 
	 * 1221 1221 1221 1221
	 * 2112 2112 2112 2112
	 */
	
	public static final int LABEL_LARGE_ROW_NUM = 1;
	public static final int LABEL_SMALL_ROW_NUM = 2;
	
	public static final int LABEL_LARGE_COL_NUM = 4;
	public static final int LABEL_SMALL_COL_NUM = 8;
	
	public static final int LABEL_SMALL_WIDTH = 64;
	public static final int LABEL_SMALL_HEIGHT = 64;
	public static final int LABEL_LARGE_WIDTH = 128;
	public static final int LABEL_LARGE_HEIGHT = 128;
	
	private static final int STATE_NEW = 0;
	private static final int STATE_INITIALIZED = 1;
	private static final int STATE_ADDING = 2;
	private static final int STATE_DRAWING = 3;
	
	private int mNextLargeLabelLeft;
	private int mNextLargeLabelBottom;
	
	private int mNextSmallLabelLeft;
	private int mNextSmallLabelBottom;
	
	private int mStrikeWidth;
	private int mStrikeHeight;
	private int mTextureID;
	private int mState;

	private Bitmap mBitmap;
	private Canvas mCanvas;

	private FixedSizeArray<Label> mLabels = new FixedSizeArray<Label>((LABEL_LARGE_ROW_NUM*LABEL_LARGE_COL_NUM)+(LABEL_SMALL_ROW_NUM*LABEL_SMALL_COL_NUM));

	public LabelMaker() {
		mStrikeWidth = LabelMaker.LABEL_LARGE_WIDTH * LabelMaker.LABEL_LARGE_COL_NUM;
		mStrikeHeight = (LabelMaker.LABEL_LARGE_HEIGHT * LabelMaker.LABEL_LARGE_ROW_NUM) + (LabelMaker.LABEL_SMALL_HEIGHT * LabelMaker.LABEL_SMALL_ROW_NUM);
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
		
		// we don't want these textures to tile so we set to clamp to edge
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		// using GL_REPLACE instead of GL_MODULATE makes it so only PNGs with no alpha channel could be transparent
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
	}

	public void beginAdding(GL10 gl) {
		checkState(STATE_INITIALIZED, STATE_ADDING);
		mLabels.clear();
		Bitmap.Config config = Bitmap.Config.ARGB_4444;
		mBitmap = Bitmap.createBitmap(mStrikeWidth, mStrikeHeight, config);
		mCanvas = new Canvas(mBitmap);
		mBitmap.eraseColor(0);
		
		mNextLargeLabelLeft = 0;
		mNextLargeLabelBottom = LABEL_SMALL_HEIGHT * LABEL_SMALL_ROW_NUM;
		
		mNextSmallLabelLeft = 0;
		mNextSmallLabelBottom = LABEL_SMALL_HEIGHT * (LABEL_SMALL_ROW_NUM-1);
		
		//Paint myPaint = new Paint();
		//myPaint.setColor(Color.MAGENTA);
		//mCanvas.drawCircle(20.0f, 20.0f, 20.0f, myPaint);
		//mLabels.add(new Label(0, 0, 40, 40));
		
		//Paint myPaint2 = new Paint();
		//myPaint2.setColor(Color.YELLOW);
		//mCanvas.drawCircle(60.0f, 20.0f, 20.0f, myPaint2);
		//mLabels.add(new Label(40, 0, 40, 40));
		
		//int[] wholeView = {0, 0, 512, 512};
		//mLabels.add(new Label(wholeView));
	}

	public int add(GL10 gl, String text, Paint textPaint) {
		int result = 0;
		checkState(STATE_ADDING, STATE_ADDING);
		
		boolean drawText = (text != null) && (textPaint != null);
		if (drawText) {
			// Paint.ascent is negative, so negate it.
			int ascent = (int) Math.ceil(-textPaint.ascent());
			int descent = (int) Math.ceil(textPaint.descent());
			int textHeight = ascent + descent;

			String[] textSegments = text.split("\n");
			int numberOfLines = textSegments.length;
			int maxHeight = textHeight*numberOfLines;
			int maxWidth = 0;
			int newWidth = 0;
			for(int j = 0; j < numberOfLines; j++) {
				newWidth = (int) Math.ceil(textPaint.measureText(textSegments[j]));
				if(newWidth > maxWidth) {
					maxWidth = newWidth;
				}
			}
			
			int[] assignedCrop = getCrop(maxWidth, maxHeight);

			for(int i = 0; i < numberOfLines; i++) {
				mCanvas.drawText(textSegments[i], assignedCrop[0], assignedCrop[1] + ascent + (i*textHeight), textPaint);
			}

			mLabels.add(new Label(assignedCrop));
			result = mLabels.getCount() - 1;
		}
		return result;
	}
	
	/**
	 * NOTE - in the following function texture space is allocated in 64x64 or 128x128 or 128x256 blocks, though
	 * 		  not all of that is used, notice that commented out result[2] and result[3] lines
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	private int[] getCrop(int width, int height) {
		int[] result = new int[4];
		if(width > LABEL_LARGE_WIDTH || height > LABEL_LARGE_HEIGHT) {
			//Log.d("ERROR", "attempting to print extra long label, w: " + width + ", h: " + height);
			if(width > LABEL_LARGE_WIDTH) {
				result[0] = mNextLargeLabelLeft;
				result[1] = mNextLargeLabelBottom;
				//result[2] = 2*LABEL_LARGE_WIDTH;
				//result[3] = LABEL_LARGE_HEIGHT;
				result[2] = width;
				result[3] = height;
				mNextLargeLabelLeft += 2*LABEL_LARGE_WIDTH;
				if(mNextLargeLabelLeft >= LABEL_LARGE_WIDTH*LABEL_LARGE_COL_NUM) {
					mNextLargeLabelLeft = 0;
					mNextLargeLabelBottom += LABEL_LARGE_HEIGHT;
				}
			} else {
				Log.d("ERROR", "attempting to print text larger than large label");
			}
		} else if(width > LABEL_SMALL_WIDTH || height > LABEL_SMALL_HEIGHT) {
			result[0] = mNextLargeLabelLeft;
			result[1] = mNextLargeLabelBottom;
			//result[2] = LABEL_LARGE_WIDTH;
			//result[3] = LABEL_LARGE_HEIGHT;
			result[2] = width;
			result[3] = height;
			mNextLargeLabelLeft += LABEL_LARGE_WIDTH;
			if(mNextLargeLabelLeft >= LABEL_LARGE_WIDTH*LABEL_LARGE_COL_NUM) {
				mNextLargeLabelLeft = 0;
				mNextLargeLabelBottom += LABEL_LARGE_HEIGHT;
			}
			if(mNextLargeLabelBottom >= LABEL_LARGE_HEIGHT*LABEL_LARGE_ROW_NUM) {
				Log.d("ERROR", "ran out of bitmap space for large text block");
			}
		} else {
			result[0] = mNextSmallLabelLeft;
			result[1] = mNextSmallLabelBottom;
			//result[2] = LABEL_SMALL_WIDTH;
			//result[3] = LABEL_SMALL_HEIGHT;
			result[2] = width;
			result[3] = height;
			mNextSmallLabelLeft += LABEL_SMALL_WIDTH;
			if(mNextSmallLabelLeft >= LABEL_SMALL_WIDTH*LABEL_SMALL_COL_NUM) {
				mNextSmallLabelLeft = 0;
				mNextSmallLabelBottom += LABEL_SMALL_HEIGHT;
			}
			if(mNextSmallLabelBottom >= LABEL_SMALL_HEIGHT*LABEL_SMALL_ROW_NUM) {
				Log.d("ERROR", "ran out of bitmap space for small text block");
			}
		}
		return result;
	}

	public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
		checkState(STATE_INITIALIZED, STATE_DRAWING);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		// this was setting everything to be fully visible, (so everything I was drawing
		// which made it hard to debug, the gl object is used by EVERYONE)
		//gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
		//gl.glMatrixMode(GL10.GL_PROJECTION);
		//gl.glPushMatrix();
		//gl.glLoadIdentity();
		//gl.glOrthof(0.0f, viewWidth, 0.0f, viewHeight, 0.0f, 1.0f);
		//gl.glMatrixMode(GL10.GL_MODELVIEW);
		//gl.glPushMatrix();
		//gl.glLoadIdentity();
		// Magic offsets to promote consistent rasterization.
		//gl.glTranslatef(0.375f, 0.375f, 0.0f);
		
		//draw(gl, 0, 0, 0);
	}

	public void draw(GL10 gl, float x, float y, int labelID) {
		checkState(STATE_DRAWING, STATE_DRAWING);
		//gl.glPushMatrix();

		Label label = mLabels.get(labelID);
		gl.glEnable(GL10.GL_TEXTURE_2D);

		// glDrawTexfOES does not require the game matrix stack pushing and poping as glDrawElements for example
		// NOTE - glDrawTexfOES draws from the bottom left of the view, so need to flip y axis to match
		// glDrawElements which draws from the top left
		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, label.mCrop, 0);
		((GL11Ext)gl).glDrawTexfOES(x, y, 0, label.mWidth, label.mHeight);
		//gl.glPopMatrix();
	}

	public void endDrawing(GL10 gl) {
		checkState(STATE_DRAWING, STATE_INITIALIZED);
		gl.glDisable(GL10.GL_BLEND);
		//gl.glMatrixMode(GL10.GL_PROJECTION);
		//gl.glPopMatrix();
		//gl.glMatrixMode(GL10.GL_MODELVIEW);
		//gl.glPopMatrix();
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
		return mLabels.get(labelID).mWidth;
	}

	public float getHeight(int labelID) {
		return mLabels.get(labelID).mHeight;
	}

	private void checkState(int oldState, int newState) {
		if (mState != oldState) {
			throw new IllegalArgumentException("Can't call this method now.");
		}
		mState = newState;
	}

	private static class Label {
		
		/** width of label */
		public float mWidth;
		
		/** height of label */
		public float mHeight;
		
		/** [0] - left (u), [1] - bottom(v), [2] - width, [3] - height - the cropping array used in GL_TEXTURE_CROP_RECT_OES */
		public int[] mCrop;

		/**
		 * 
		 * @param left
		 * @param bottom
		 * @param width
		 * @param height
		 */
		public Label(int[] crop) {
			mWidth = crop[2];
			mHeight = crop[3];
			mCrop = crop;
			mCrop[3] *= -1;
			mCrop[1] += mHeight;
		}
	}
}
