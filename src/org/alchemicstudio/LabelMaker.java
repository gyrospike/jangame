package org.alchemicstudio;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
	
	
	public static final int LABEL_LARGE_ROW_NUM = 4;
	
	public static final int LABEL_MEDIUM_ROW_NUM = 2;
	public static final int LABEL_SMALL_ROW_NUM = 2;
	
	public static final int LABEL_LARGE_COL_NUM = 1;
	
	public static final int LABEL_MEDIUM_COL_NUM = 2;
	public static final int LABEL_SMALL_COL_NUM = 8;
	
	public static final int LABEL_LARGE_WIDTH = 512;
	public static final int LABEL_LARGE_HEIGHT = 128;
	public static final int LABEL_MEDIUM_WIDTH = 256;
	public static final int LABEL_MEDIUM_HEIGHT = 96;
	public static final int LABEL_SMALL_WIDTH = 64;
	public static final int LABEL_SMALL_HEIGHT = 32;
	
	//private static final int STATE_NEW = 0;
	//private static final int STATE_INITIALIZED = 1;
	//private static final int STATE_ADDING = 2;
	//private static final int STATE_DRAWING = 3;
	
	private int mNextLargeLabelLeft;
	private int mNextLargeLabelBottom;
	
	private int mNextMediumLabelLeft;
	private int mNextMediumLabelBottom;
	
	private int mNextSmallLabelLeft;
	private int mNextSmallLabelBottom;
	
	private int mStrikeWidth;
	private int mStrikeHeight;
	private int mState;
	
	private int mTextureDynamic;
	private int mTextureStatic;

	private Bitmap mStaticBitmap;
	private Bitmap mDynamicBitmap;
	
	private Canvas mCanvas;
	private Canvas mStaticCanvas;
	
	private FixedSizeArray<Label> mDynamicLabels = new FixedSizeArray<Label>((LABEL_MEDIUM_ROW_NUM*LABEL_MEDIUM_COL_NUM)+(LABEL_SMALL_ROW_NUM*LABEL_SMALL_COL_NUM));
	private FixedSizeArray<Label> mStaticLabels = new FixedSizeArray<Label>((LABEL_MEDIUM_ROW_NUM*LABEL_MEDIUM_COL_NUM)+(LABEL_SMALL_ROW_NUM*LABEL_SMALL_COL_NUM));
	
	public LabelMaker() {
		mStrikeWidth = LabelMaker.LABEL_SMALL_WIDTH * LabelMaker.LABEL_SMALL_COL_NUM;
		mStrikeHeight = (LabelMaker.LABEL_MEDIUM_HEIGHT * LabelMaker.LABEL_MEDIUM_ROW_NUM)+(LabelMaker.LABEL_SMALL_HEIGHT * LabelMaker.LABEL_SMALL_ROW_NUM);
	}
	
	public void prepTexture(GL10 gl) {
		// Use Nearest for performance.
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);

		// we don't want these textures to tile so we set to clamp to edge
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		// using GL_REPLACE instead of GL_MODULATE makes it so only PNGs with no alpha channel could be transparent
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
	}

	public void initialize(GL10 gl) {
		//mState = STATE_INITIALIZED;
		int[] textures = new int[2];
		gl.glGenTextures(2, textures, 0);
		
		mTextureStatic = textures[1];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureStatic);
		prepTexture(gl);
		
		mTextureDynamic = textures[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureDynamic);
		prepTexture(gl);
		
		Bitmap.Config config = Bitmap.Config.ARGB_4444;
		mStaticBitmap = Bitmap.createBitmap(LABEL_LARGE_COL_NUM*LABEL_LARGE_WIDTH, LABEL_LARGE_ROW_NUM*LABEL_LARGE_HEIGHT, config);
		mStaticCanvas = new Canvas(mStaticBitmap);
		
		initCropFraming();
		
		//Paint myPaint = new Paint();
		//myPaint.setColor(Color.MAGENTA);
		//mStaticCanvas.drawCircle(100.0f, 100.0f, 20.0f, myPaint);
		
		// draw all the "static" text here on a bitmap
		TextBoxBase[] textBoxes = BaseObject.sSystemRegistry.mAssetLibrary.getPrerenderedText();
		for(int i = 0; i < textBoxes.length; i++) {
			addStatic(gl, textBoxes[i].getText(), textBoxes[i].getPaint());
		}
	}

	private void initCropFraming() {
		// initialize the first positions in each size to be far left, bottom of the first top-left rectangle
		mNextSmallLabelLeft = 0;
		mNextSmallLabelBottom = LABEL_SMALL_HEIGHT * (LABEL_SMALL_ROW_NUM-1);

		mNextMediumLabelLeft = 0;
		mNextMediumLabelBottom = mNextSmallLabelBottom + LABEL_SMALL_HEIGHT + (LABEL_MEDIUM_HEIGHT*(LABEL_MEDIUM_ROW_NUM-1));

		mNextLargeLabelLeft = 0;
		mNextLargeLabelBottom = (LABEL_LARGE_HEIGHT*(LABEL_LARGE_ROW_NUM-1));
	}

	public void beginAdding(GL10 gl) {
		//checkState(STATE_INITIALIZED, STATE_ADDING);
		mDynamicLabels.clear();
		Bitmap.Config config = Bitmap.Config.ARGB_4444;
		mDynamicBitmap = Bitmap.createBitmap(mStrikeWidth, mStrikeHeight, config);
		mCanvas = new Canvas(mDynamicBitmap);
		mDynamicBitmap.eraseColor(0);
		
		initCropFraming();
	}
	
	public int addStatic(GL10 gl, String text, Paint textPaint) {
		int result = 0;
		//checkState(STATE_ADDING, STATE_ADDING);
		
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
			
			int[] assignedCrop = getStaticCrop(maxWidth, maxHeight);

			for(int i = 0; i < numberOfLines; i++) {
				mStaticCanvas.drawText(textSegments[i], assignedCrop[0], assignedCrop[1] + ascent + (i*textHeight), textPaint);
			}

			mStaticLabels.add(new Label(assignedCrop));
			result = mStaticLabels.getCount() - 1;
		}
		return result;
	}

	public int addDynamic(GL10 gl, String text, Paint textPaint) {
		int result = 0;
		//checkState(STATE_ADDING, STATE_ADDING);
		
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

			mDynamicLabels.add(new Label(assignedCrop));
			result = mDynamicLabels.getCount() - 1;
		}
		return result;
	}
	
	public void endAdding(GL10 gl) {
		//checkState(STATE_ADDING, STATE_INITIALIZED);
		// Reclaim storage used by bitmap and canvas.
		
	}

	public void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
		//checkState(STATE_INITIALIZED, STATE_DRAWING);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureDynamic);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mDynamicBitmap, 0);
		
		mDynamicBitmap.recycle();
		mDynamicBitmap = null;
		mCanvas = null;
	}

	public void draw(GL10 gl, float x, float y, int labelID) {
		//checkState(STATE_DRAWING, STATE_DRAWING);
		
		Label label = mDynamicLabels.get(labelID);
		// glDrawTexfOES does not require the game matrix stack pushing and poping as glDrawElements for example
		// NOTE - glDrawTexfOES draws from the bottom left of the view, so need to flip y axis to match
		// glDrawElements which draws from the top left
		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, label.mCrop, 0);
		((GL11Ext)gl).glDrawTexfOES(x, y, 0, label.mWidth, label.mHeight);
	}
	
	public void switchToStaticLabels(GL10 gl) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureStatic);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mStaticBitmap, 0);
	}
	
	public void drawStatic(GL10 gl, float x, float y, int labelID) {		
		Label label = mStaticLabels.get(labelID);
		((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, label.mCrop, 0);
		((GL11Ext)gl).glDrawTexfOES(x, y, 0, label.mWidth, label.mHeight);
	}

	public void endDrawing(GL10 gl) {
		gl.glDisable(GL10.GL_BLEND);
	}
	
	public void shutdown(GL10 gl) {
		if (gl != null) {
			//if (mState > STATE_NEW) {
				int[] textures = new int[2];
				textures[0] = mTextureDynamic;
				textures[1] = mTextureStatic;
				gl.glDeleteTextures(2, textures, 0);
				//mState = STATE_NEW;
			//}
		}
		// Reclaim storage used by bitmap and canvas.
		mStaticBitmap.recycle();
		mStaticBitmap = null;
		mStaticCanvas = null;
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
		if(width < LABEL_SMALL_WIDTH && height < LABEL_SMALL_HEIGHT) {
			result[0] = mNextSmallLabelLeft;
			result[1] = mNextSmallLabelBottom;
			//result[2] = 2*LABEL_SMALL_WIDTH;
			//result[3] = LABEL_SMALL_HEIGHT;
			result[2] = width;
			result[3] = height;
			mNextSmallLabelLeft += LABEL_SMALL_WIDTH;
			if(mNextSmallLabelLeft >= LABEL_SMALL_WIDTH*LABEL_SMALL_COL_NUM) {
				mNextSmallLabelLeft = 0;
				mNextSmallLabelBottom -= LABEL_SMALL_HEIGHT;
				if(mNextSmallLabelBottom < 0) {
					Log.d("ERROR", "ran out of bitmap space for small text block");
				}
			}
		} else if(width < LABEL_MEDIUM_WIDTH && height < LABEL_MEDIUM_HEIGHT) {
			result[0] = mNextMediumLabelLeft;
			result[1] = mNextMediumLabelBottom;
			//result[2] = 2*LABEL_MEDIUM_WIDTH;
			//result[3] = LABEL_MEDIUM_HEIGHT;
			result[2] = width;
			result[3] = height;
			mNextMediumLabelLeft += LABEL_MEDIUM_WIDTH;
			if(mNextMediumLabelLeft >= LABEL_MEDIUM_WIDTH*LABEL_MEDIUM_COL_NUM) {
				mNextMediumLabelLeft = 0;
				mNextMediumLabelBottom -= LABEL_MEDIUM_HEIGHT;
				if(mNextMediumLabelBottom < LABEL_SMALL_HEIGHT*LABEL_SMALL_ROW_NUM) {
					Log.d("ERROR", "ran out of bitmap space for medium text block");
				}
			}
		} 
		else {
			Log.d("ERROR", "attempting to print text larger than largest text block allocation: width, height: " + width + ", " + height);
		}
		return result;
	}

	private int[] getStaticCrop(int width, int height) {
		int[] result = new int[4];
		result[0] = mNextLargeLabelLeft;
		result[1] = mNextLargeLabelBottom;
		result[2] = width;
		result[3] = height;
		mNextLargeLabelLeft += LABEL_LARGE_WIDTH;
		if(mNextLargeLabelLeft >= LABEL_LARGE_WIDTH*LABEL_LARGE_COL_NUM) {
			mNextLargeLabelLeft = 0;
			mNextLargeLabelBottom -= LABEL_LARGE_HEIGHT;
			if(mNextLargeLabelBottom < 0) {
				Log.d("ERROR", "ran out of bitmap space for large text block");
			}
		}
		return result;
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
