package jan.game;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Comparator;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import jan.game.Texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

public class Sprite {

	private Context mContext;
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mTexBuffer;
	private ShortBuffer mIndexBuffer;

	private int[] textures = new int[1];
	private int mResourceId;

	private Texture[] mTexture;

	private int mPriority;

	public float xOffset;
	public float yOffset;
	public boolean cameraRelative;
	public int textureIndex;

	public Sprite(int priority) {

		xOffset = 0.0f;
		yOffset = 0.0f;

		mTexture = new Texture[2];
		mPriority = priority;
		// mResourceId = resourceId;
		// mContext = context;

		short[] mIndicesArray = { 1, 0, 2, 3 };

		float[] coords = { -.1f, -.1f, // 0 bottom left
				.1f, -.1f, // 3 bottom right
				-.1f, .1f, // 1 top left
				.1f, .1f, // 2 top right
		};

		float[] texCoords = { 0.0f, 0.0f, //
	    		0.0f, 1.0f, //
	    		1.0f, 0.0f, //
	    		1.0f, 1.0f //
		};

		ByteBuffer vbb = ByteBuffer.allocateDirect(coords.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();

		ByteBuffer ibb = ByteBuffer.allocateDirect(mIndicesArray.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		mIndexBuffer = ibb.asShortBuffer();

		ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTexBuffer = tbb.asFloatBuffer();

		mVertexBuffer.put(coords);
		mIndexBuffer.put(mIndicesArray);
		mTexBuffer.put(texCoords);

		mVertexBuffer.position(0);
		mIndexBuffer.position(0);
		mTexBuffer.position(0);

	}

	public int getPriority() {
		return mPriority;
	}

	public void setTexture(Texture texture, int width, int height) {
		mTexture[textureIndex] = texture;
		// mWidth = width;
		// mHeight = height;
		// setCrop(0, height, width, height);
		textureIndex++;
	}

	public void draw(GL10 gl, float angle, float x, float y) {
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		final Texture texture = mTexture[textureIndex];

		// Bind our only previously generated texture in this case
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.name);

		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);

		// Enable the vertex and texture state
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);

		// Draw the vertices as triangles, based on the Index Buffer information
		gl.glDrawElements(GL10.GL_TRIANGLES, 4, GL10.GL_UNSIGNED_BYTE,
				mIndexBuffer);

		// Disable the client state before leaving
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
