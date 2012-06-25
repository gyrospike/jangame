package org.alchemicstudio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Comparator;
import javax.microedition.khronos.opengles.GL10;

import org.alchemicstudio.Texture;

public class Sprite {

	public float xOffset;
	public float yOffset;

	/** array of textures this sprite uses */
	private Texture[] mTexture;
	
	/** length of texture array cached in a member variable as it is needed on draw, which is called often */
	private int mTextureArrayLength;
	
	/** the current texture index of the texture array to be used as the display texture */
	private int mCurrentTextureIndex = 0;
	
	/** the draw priority for this sprite, as in, should this sprite appear in front of this one? */
	private int mPriority;
	
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mTextureBuffer;
	private ByteBuffer mIndexBuffer;

	private float mWidthScale;
	private float mHeightScale;
	private float mRotation;
	
	private float mOpacity = 1.0f;
	private float mXScale = 1.0f;
	private float mYScale = 1.0f;

	public Sprite(int[] textureIDArray, int priority, float width, float height) {
		mWidthScale = width;
		mHeightScale = height;
		
		mTextureArrayLength = textureIDArray.length;
		init(priority, mTextureArrayLength);
		
		for(int i = 0; i < mTextureArrayLength; i++) {
			Texture temp = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(textureIDArray[i]);
			mTexture[i] = temp;
		}
	}

	private void init(int priority, int frames) {
		final byte[] indices = { 1, 0, 2, 3 };
		
		final float[] vertices = { 
				0.0f, 0.0f, // 0 bottom left
				0.0f, 1.0f, // 1 top left
				1.0f, 1.0f, // 2 top right
				1.0f, 0.0f, // 3 bottom right
		};
		
		final float[] texture = { 
				0.0f, 1.0f, //
				0.0f, 0.0f, //
				1.0f, 0.0f, //
				1.0f, 1.0f, //
		};

		mTexture = new Texture[frames];
		mPriority = priority;

		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuf.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuf.asFloatBuffer();
		mTextureBuffer.put(texture);
		mTextureBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
	
	/**
	 * setter for the current texture index
	 * 
	 * @param newIndex
	 */
	protected void setTextureIndex(int newIndex) {
		mCurrentTextureIndex = newIndex;
	}
	
	public void modTex(float scale) {
		float[] texture = { 0.0f, 1.0f, //
				0.0f, 0.0f, //
				scale, 0.0f, //
				scale, 1.0f, //
		};
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuf.asFloatBuffer();
		mTextureBuffer.put(texture);
		mTextureBuffer.position(0);
		
	}

	/**
	 * increment the texture frame by one, resetting to zero if out of range
	 */
	public void incrementFrame() {
		mCurrentTextureIndex++;
		if(mCurrentTextureIndex == mTextureArrayLength) {
			mCurrentTextureIndex = 0;
		}
	}
	
	/**
	 * Why do I need this -y here? not sure, seem likes the 0 point is at the top of the screen, so negative brings it down into
	 * the screen, positive is up above for some crazy reason
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y) {
		xOffset = x;
		yOffset = -y;
	}

	public void setOpacity(float value) {
		mOpacity = value;
	}

	public void setScale(float x, float y) {
		mXScale = x;
		mYScale = y;
	}

	public void setRotation(float angle) {
		mRotation = (float) (180 * (angle / Math.PI));
	}

	// TODO - what is the deal with this negative offset?
	public Vector2 getPosition() {
		return new Vector2(xOffset, -yOffset);
	}

	public int getPriority() {
		return mPriority;
	}

	public void draw(GL10 gl, float angle, float x, float y) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[mCurrentTextureIndex].name);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);

		gl.glColor4f(mOpacity, mOpacity, mOpacity, mOpacity);
		
		// 392, -168
		// x = 392;
		// y = -168;
		//
		
		gl.glTranslatef(x, y, 0);
		
		//translate the gl object to the center of the image, rotate, then go back -> this helps in thinking about opengl 
		//as a sort of camera moving around looking at objects as opposed to objects moving around
		gl.glTranslatef(mWidthScale/2, mHeightScale/2, 0);
		gl.glRotatef(mRotation + angle, 0, 0, 1);
		gl.glTranslatef(-mWidthScale/2, -mHeightScale/2, 0);
		
		gl.glScalef(mWidthScale * mXScale, mHeightScale * mYScale, 0);

		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
		gl.glLoadIdentity();

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public final static class PriorityComparator implements Comparator<Sprite> {
		// removing this seems to have no effect - not sure why we are overriding, aren't we implementing?
		@Override
		public int compare(Sprite s1, Sprite s2) {
			int s1P = s1.getPriority();
			int s2P = s2.getPriority();

			if (s1P > s2P)
				return 1;
			else if (s1P < s2P)
				return -1;
			else
				return 0;
		}
	}
}
