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
	private int mPriority;
	public float xOffset;
	public float yOffset;
	public boolean cameraRelative;
	
	private Texture[] mTexture;
	
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private FloatBuffer textureBuffer;
	/** The buffer holding the indices */
	private ByteBuffer indexBuffer;
	
	private byte[] indices = { 1, 0, 2, 3 };

	private float[] vertices = { -.1f, -.1f, // 0 bottom left
			-.1f, .1f, // 1 top left
			.1f, .1f, // 2 top right
			.1f, -.1f, // 3 bottom right
	};

	private float[] texture = { 0.0f, 0.0f, //
			1.0f, 0.0f, //
			1.0f, 1.0f, //
			0.0f, 1.0f //
	};

	public Sprite(int priority) {

		mTexture = new Texture[2];
		mPriority = priority;
		
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		//
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	public int getPriority() {
		return mPriority;
	}

	public void setTexture(Texture texture, int width, int height) {
		mTexture[0] = texture;
		// mWidth = width;
		// mHeight = height;
		// setCrop(0, height, width, height);
		//textureIndex++;
	}

	public void draw(GL10 gl, float angle, float x, float y) {
		// Bind the texture according to the set texture filter
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0].name);

		// Enable the vertex, texture and normal state
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);

		gl.glTranslatef(x, y, 0);
		
		// Point to our buffers
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		// Draw the vertices as triangles, based on the Index Buffer information
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, indices.length, GL10.GL_UNSIGNED_BYTE,
				indexBuffer);

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
