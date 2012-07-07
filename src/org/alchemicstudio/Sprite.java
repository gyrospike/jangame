package org.alchemicstudio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Comparator;
import javax.microedition.khronos.opengles.GL10;

import org.alchemicstudio.Texture;

public class Sprite {

	private final float TEXTURE_FITS_POLY = 1.0f;
	
	/** the x offset of the sprite */
	private float xOffset;
	
	/** the y offset of the sprite */
	private float yOffset;

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
	private float mRotDegrees;
	
	private float mOpacity = 1.0f;
	private float mXScale = 1.0f;
	private float mYScale = 1.0f;

	public Sprite(Texture texture, int priority) {
		mWidthScale = texture.width;
		mHeightScale = texture.height;
		
		Texture[] textureArray = {texture};
		mTexture = textureArray;
		mPriority = priority;
		
		mTextureArrayLength = mTexture.length;
		init(TEXTURE_FITS_POLY, TEXTURE_FITS_POLY);
	}
	
	public Sprite(Texture texture, int priority, int polyWidth, int polyHeight) {
		mWidthScale = polyWidth;
		mHeightScale = polyHeight;
		
		Texture[] textureArray = {texture};
		mTexture = textureArray;
		mPriority = priority;
		
		mTextureArrayLength = mTexture.length;
		init(polyWidth/texture.width, polyHeight/texture.height);
	}
	
	public Sprite(Texture[] textureArray, int priority) {
		mWidthScale = textureArray[0].width;
		mHeightScale = textureArray[0].height;
		
		mTexture = textureArray;
		mPriority = priority;
		
		mTextureArrayLength = mTexture.length;
		init(TEXTURE_FITS_POLY, TEXTURE_FITS_POLY);
	}
	
	public Sprite(Texture[] textureArray, int priority, int polyWidth, int polyHeight) {
		mWidthScale = polyWidth;
		mHeightScale = polyHeight;
		
		mTexture = textureArray;
		mPriority = priority;
		
		mTextureArrayLength = mTexture.length;
		init(polyWidth/textureArray[0].width, polyHeight/textureArray[0].height);
	}

	private void init(float repeatS, float repeatT) {
		final byte[] indices = { 1, 0, 2, 3 };
		
		final float[] vertices = { 
				0.0f, 0.0f, // 0 bottom left
				0.0f, 1.0f, // 1 top left
				1.0f, 1.0f, // 2 top right
				1.0f, 0.0f, // 3 bottom right
		};
		
		final float[] texture = { 
				0.0f, repeatT, //
				0.0f, 0.0f, //
				repeatS, 0.0f, //
				repeatS, repeatT, //
		};

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
	
	public void modTex(float lengthScale, float widthScale) {
		float[] texture = { 
				0.0f, widthScale, //
				0.0f, 0.0f, //
				lengthScale, 0.0f, //
				lengthScale, widthScale, //
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
	 * 
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y) {
		xOffset = x;
		yOffset = y;
	}

	public void setOpacity(float value) {
		mOpacity = value;
	}
	
	public void setPolyScale(float x, float y) {
		mWidthScale *= x;
		mHeightScale *= y;
	}

	public void setScale(float x, float y) {
		mXScale = x;
		mYScale = y;
	}
	
	public float getRotation() {
		return mRotDegrees;
	}

	public void setRotationDegrees(float degree) {
		mRotDegrees = degree;
	}
	
	public void setRotation(double rad) {
		mRotDegrees = (float) (180 * (rad / Math.PI));
	}

	public Vector2 getPosition() {
		return new Vector2(xOffset, yOffset);
	}

	public int getPriority() {
		return mPriority;
	}
	
	public Vector2 getPolyScale() {
		return new Vector2(mWidthScale, mHeightScale);
	}

	public void draw(GL10 gl, float x, float y) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[mCurrentTextureIndex].name);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);
		gl.glColor4f(mOpacity, mOpacity, mOpacity, mOpacity);
		
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_S,GL10.GL_REPEAT);
		//gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_WRAP_T,GL10.GL_REPEAT);
		
		// NOTE: negative here allows all values for y to be positive in game managers, though 
		// coordinates in y axis are flipped
		gl.glTranslatef(x, -y, 0);
		
		//translate the gl object to the center of the image, rotate, then go back -> this helps in thinking about opengl 
		//as a sort of camera moving around looking at objects as opposed to objects moving around
		gl.glTranslatef(mWidthScale/2, mHeightScale/2, 0);
		gl.glRotatef(mRotDegrees, 0, 0, 1);
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
