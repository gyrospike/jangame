package org.alchemicstudio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Comparator;
import javax.microedition.khronos.opengles.GL10;

import org.alchemicstudio.Texture;

import android.content.res.AssetManager;
import android.os.SystemClock;
import android.util.Log;

public class Sprite {

	public float xOffset;
	public float yOffset;
	public float rotation;
	public boolean cameraRelative;
	public int currentTextureIndex;
	public float xScale, yScale;

	private Texture[] mTexture;
	private int textureIndex;
	private int mPriority;
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private ByteBuffer indexBuffer;

	private float mWidthScale;
	private float mHeightScale;
	private float opacity;

	private long mLastTime;
	private long mTime;
	private int framesPerMillisecond;
	
	TextureLibrary mTextureLibrary;

	public Sprite(int[] textureIDArray, int priority, float width, float height, int frames, int fpms) {
		mWidthScale = width;
		mHeightScale = height;

		init(priority, frames);
		
		for(int i = 0; i < textureIDArray.length; i++) {
			Texture temp = mTextureLibrary.getTextureByResource(textureIDArray[i]);
			setTextureFrame(temp);
		}
		
		if(fpms != 0) {
			framesPerMillisecond = fpms;
		}
	}

	private void init(int priority, int frames) {
		byte[] indices = { 1, 0, 2, 3 };
		
		float[] vertices = { 0.0f, 0.0f, // 0 bottom left
				0.0f, 1.0f, // 1 top left
				1.0f, 1.0f, // 2 top right
				1.0f, 0.0f, // 3 bottom right
		};
		
		float[] texture = { 0.0f, 1.0f, //
				0.0f, 0.0f, //
				1.0f, 0.0f, //
				1.0f, 1.0f, //
		};

		mTexture = new Texture[frames];
		currentTextureIndex = 0;
		mTextureLibrary = BaseObject.sSystemRegistry.mTextureLibrary;
		
		opacity = 1.0f;
		mPriority = priority;
		xScale = 1.0f;
		yScale = 1.0f;

		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}
	
	public void modTex(float scale) {
		float[] texture = { 0.0f, 1.0f, //
				0.0f, 0.0f, //
				scale, 0.0f, //
				scale, 1.0f, //
		};
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
		
	}

	public void setPosition(float x, float y) {
		xOffset = x;
		yOffset = -y;
	}

	public void setOpacity(float value) {
		opacity = value;
	}

	public void setScale(float x, float y) {
		xScale = x;
		yScale = y;
	}

	public void setRotation(float angle) {
		rotation = (float) (180 * (angle / Math.PI));
	}

	public Vector2 getPosition() {
		return new Vector2(xOffset, yOffset);
	}

	public int getPriority() {
		return mPriority;
	}

	public void setTextureFrame(Texture texture) {
		mTexture[textureIndex] = texture;
		textureIndex++;
	}

	public void draw(GL10 gl, float angle, float x, float y) {
		if (framesPerMillisecond != 0) {
			mTime = SystemClock.uptimeMillis();
			final long timeDelta = mTime - mLastTime;

			if (timeDelta > framesPerMillisecond) {
				mLastTime = mTime;
				currentTextureIndex++;
				if (currentTextureIndex >= textureIndex)
					currentTextureIndex = 0;
			}
		}
		
		if(mTexture==null || mTexture[currentTextureIndex]==null) {
			String helllo = "hello";
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[currentTextureIndex].name);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		gl.glColor4f(opacity, opacity, opacity, opacity);
		gl.glTranslatef(x, y, 0);
		
		//translate the gl object to the center of the image, rotate, then go back -> this helps in thinking about opengl 
		//as a sort of camera moving around looking at objects as opposed to objects moving around
		gl.glTranslatef(mWidthScale/2, mHeightScale/2, 0);
		gl.glRotatef(rotation + angle, 0, 0, 1);
		gl.glTranslatef(-mWidthScale/2, -mHeightScale/2, 0);
		
		gl.glScalef(mWidthScale * xScale, mHeightScale * yScale, 0);

		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, indexBuffer);
		gl.glLoadIdentity();

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public final static class PriorityComparator implements Comparator<Sprite> {

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
