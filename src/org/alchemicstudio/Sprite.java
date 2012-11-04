package org.alchemicstudio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Comparator;
import javax.microedition.khronos.opengles.GL10;

import org.alchemicstudio.Texture;

public class Sprite {

    /** signifies we don't need to scale the texture to fit multiple times inside the poly */
    private final float TEXTURE_FITS_POLY = 1.0f;

    /** the x offset of the sprite */
    private float xOffset;

    /** the y offset of the sprite */
    private float yOffset;

    /** the image pack this sprite references */
    private ImagePack mImagePack;

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

    /** the current texture array being pulled from the image pack */
    private Texture[] mTextures;

    /** the current texture array index that is being displayed */
    private int mTextureIndex;

    /** the length of the current texture array */
    private int mTextureArrayLength;

    /** the current miliseconds per frame of the current texture array */
    private int mTextureFrameMS;

    /** how much time has passed since we last updated the texture frame */
    private int mElapsedTime;

    /** the last key we requested the image pack to provide */
    private String mLastRequestedKey;

    public Sprite(ImagePack imagePack, int priority) {
        mImagePack = imagePack;
        mPriority = priority;
        setImageId("idle");

        mWidthScale = mTextures[mTextureIndex].width;
        mHeightScale = mTextures[mTextureIndex].height;
        initTextureBuffers(TEXTURE_FITS_POLY, TEXTURE_FITS_POLY);
    }

    public Sprite(ImagePack imagePack, int priority, int polyWidth, int polyHeight) {
        mImagePack = imagePack;
        mPriority = priority;
        setImageId("idle");

        int imageWidth = mTextures[mTextureIndex].width;
        int imageHeight = mTextures[mTextureIndex].height;

        // if -1 is specified then just set the poly dimensions to the image dimensions
        if(polyWidth == -1) {
            polyWidth = imageWidth;
        }

        if(polyHeight == -1) {
            polyHeight = imageHeight;
        }

        mWidthScale = polyWidth;
        mHeightScale = polyHeight;
        initTextureBuffers(polyWidth/imageWidth, polyHeight/imageHeight);
    }

    /**
     * Specify which image to use for this sprite by id
     * @param key
     */
    public void setImageId(String key) {
        if(!key.equals(mLastRequestedKey)) {
            Image image = mImagePack.getImage(key);
            mTextures = image.getTextures();
            mTextureFrameMS = image.getFrameMS();
            mTextureIndex = 0;
            mTextureArrayLength = mTextures.length;
            mLastRequestedKey = key;
        }
    }

    /**
     * Update the frame to display based on how much time has elapsed since the last update
     * and the rate at which we should change frames
     * @param timeDelta
     */
    public void updateFrame(long timeDelta) {
        if (mTextureFrameMS != 0) {
            mElapsedTime += timeDelta;
            if (mElapsedTime > mTextureFrameMS) {
                mElapsedTime = mElapsedTime - mTextureFrameMS;
                mTextureIndex++;
                if(mTextureIndex == mTextureArrayLength) {
                    mTextureIndex = 0;
                }
            }
        } else {
            mTextureIndex = 0;
        }
    }

    /**
     * Initilize the texture byte buffers used to indicate the size and draw order of the
     * sprite poly
     *
     * @param repeatS
     * @param repeatT
     */
    private void initTextureBuffers(float repeatS, float repeatT) {
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

    // ???
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

    /**
     * Sprites are drawn from the bottom left corner of the poly
     *
     * @param gl
     * @param x
     * @param y
     */
    public void draw(GL10 gl, float x, float y) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures[mTextureIndex].name);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer);

        // tints can be applied by altering these RGBA values
        gl.glColor4f(1.0f, 1.0f, 1.0f, mOpacity);

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

        // draws from top left as origin
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, 4, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        gl.glLoadIdentity();

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

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
