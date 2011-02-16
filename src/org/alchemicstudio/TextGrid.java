package org.alchemicstudio;

import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * A 2D rectangular mesh. Can be drawn textured or untextured.
 *
 */
class TextGrid {

        public TextGrid(int w, int h) {
                if (w < 0 || w >= 65536) {
                        throw new IllegalArgumentException("w");
                }
                if (h < 0 || h >= 65536) {
                        throw new IllegalArgumentException("h");
                }
                if (w * h >= 65536) {
                        throw new IllegalArgumentException("w * h >= 65536");
                }

                mW = w;
                mH = h;
                int size = w * h;
                mVertexArray = new float[size * 3];
                mVertexBuffer = FloatBuffer.wrap(mVertexArray);

                mTexCoordArray = new float[size * 2];
                mTexCoordBuffer = FloatBuffer.wrap(mTexCoordArray);

                int quadW = mW - 1;
                int quadH = mH - 1;
                int quadCount = quadW * quadH;
                int indexCount = quadCount * 6;
                mIndexCount = indexCount;
                char[] indexArray = new char[indexCount];

                /*
                 * Initialize triangle list mesh.
                 *
                 *     [0]-----[  1] ...
                 *      |    /   |
                 *      |   /    |
                 *      |  /     |
                 *     [w]-----[w+1] ...
                 *      |       |
                 *
                 */

                {
                        int i = 0;
                        for (int y = 0; y < quadH; y++) {
                                for (int x = 0; x < quadW; x++) {
                                        char a = (char) (y * mW + x);
                                        char b = (char) (y * mW + x + 1);
                                        char c = (char) ((y + 1) * mW + x);
                                        char d = (char) ((y + 1) * mW + x + 1);

                                        indexArray[i++] = a;
                                        indexArray[i++] = b;
                                        indexArray[i++] = c;

                                        indexArray[i++] = b;
                                        indexArray[i++] = c;
                                        indexArray[i++] = d;
                                }
                        }
                }

                mIndexBuffer = CharBuffer.wrap(indexArray);
        }

        void set(int i, int j, float x, float y, float z, float u, float v) {
                if (i < 0 || i >= mW) {
                        throw new IllegalArgumentException("i");
                }
                if (j < 0 || j >= mH) {
                        throw new IllegalArgumentException("j");
                }

                int index = mW * j + i;

                int posIndex = index * 3;
                mVertexArray[posIndex] = x;
                mVertexArray[posIndex + 1] = y;
                mVertexArray[posIndex + 2] = z;

                int texIndex = index * 2;
                mTexCoordArray[texIndex] = u;
                mTexCoordArray[texIndex + 1] = v;
        }

        public void draw(GL10 gl, boolean useTexture) {
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

                if (useTexture) {
                        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordBuffer);
                        gl.glEnable(GL10.GL_TEXTURE_2D);
                } else {
                        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
                        gl.glDisable(GL10.GL_TEXTURE_2D);
                }

                gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount,
                                GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }

        private FloatBuffer mVertexBuffer;
        private float[] mVertexArray;

        private FloatBuffer mTexCoordBuffer;
        private float[] mTexCoordArray;

        private CharBuffer mIndexBuffer;

        private int mW;
        private int mH;
        private int mIndexCount;
}