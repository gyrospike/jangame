package org.alchemicstudio;

/**
 * Created with IntelliJ IDEA.
 * User: joe
 * Date: 11/3/12
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class Image {

    /** array of textures that makes up this image */
    private Texture[] mTextureArray;

    /** array of resouce ids, used to load up the textures */
    private int[] mResourceIDArray;

    /** how many milliseconds should pass before the next frame of this animation is shown */
    private int mFrameTimeMS;

    /** usage code refers to what context the image should loaded up for, ie: menu, game */
    private int mImageUsageCode;

    public Image(int imageUsageCode, int[] resourceIDArray, int frameTimeMS) {
        mImageUsageCode = imageUsageCode;
        mResourceIDArray = resourceIDArray;
        mFrameTimeMS = frameTimeMS;
    }

    /**
     * @return  the array of resource ids
     */
    public int[] getResourceIds() {
        return mResourceIDArray;
    }

    /**
     * @return  the array of textures
     */
    public Texture[] getTextures() {
        return mTextureArray;
    }

    /**
     * @return  the miliseconds for each frame to be shown
     */
    public int getFrameMS() {
        return mFrameTimeMS;
    }

    /**
     * @return  the image usage code
     */
    public int getImageUsageCode() {
        return mImageUsageCode;
    }

    /**
     * @param textureArray  assigns a pointer to the texture array in AssetLibrary that this image should use
     */
    public void setTextureArray(Texture[] textureArray) {
        mTextureArray = textureArray;
    }

}