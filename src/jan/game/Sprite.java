package jan.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Comparator;
import javax.microedition.khronos.opengles.GL10;
import jan.game.Texture;

public class Sprite {
	
	public float xOffset;
	public float yOffset;
	public float rotation;
	public boolean cameraRelative;
	
	private Texture[] mTexture;
	private int mPriority;
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private ByteBuffer indexBuffer;

	public Sprite(int priority) {
		byte[] indices = { 1, 0, 2, 3 };

		float[] vertices = { -.1f, -.1f, // 0 bottom left
				-.1f, .1f, // 1 top left
				.1f, .1f, // 2 top right
				.1f, -.1f, // 3 bottom right
		};

		float[] texture = { 0.0f, 0.0f, //
				1.0f, 0.0f, //
				1.0f, 1.0f, //
				0.0f, 1.0f //
		};
		
		mTexture = new Texture[2];
		mPriority = priority;
		
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

	public int getPriority() {
		return mPriority;
	}

	public void setTexture(Texture texture, int width, int height) {
		mTexture[0] = texture;
	}

	public void draw(GL10 gl, float angle, float x, float y) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0].name);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		gl.glTranslatef(x, y, 0);
		gl.glRotatef(rotation, 0, 0, 1);
		gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, indexBuffer);

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
