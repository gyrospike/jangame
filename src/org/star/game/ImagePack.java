package org.star.game;

import android.util.Log;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: joe
 * Date: 10/27/12
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ImagePack {

    /** mapping from an image id to the image, ie: "node" => [textures for node, animation ids for node, etc] */
    private HashMap<String, Image> mImageMap = new HashMap<String, Image>(8);

    public HashMap<String, Image> getImageMap() {
        return mImageMap;
    }

    /**
     * Add another image id and image information to the mapping
     *
     * @param name              id by which to reference this specific image being added (ex: "idle")
     * @param imageUsageCode    the code representing what context this texture should be loaded into opengl memory
     * @param resourceIDArray    the array of resource ids this image makes use of
     * @param frameTimeMS       the time in miliseconds each frame of the animation should play
     */
    public void addEntry(String name, int imageUsageCode, int[] resourceIDArray, int frameTimeMS) {
        Image newImage = new Image(imageUsageCode, resourceIDArray, frameTimeMS);
        mImageMap.put(name, newImage);
    }

    /**
     * Get an image by name from the hashmap
     * @param key
     * @return
     */
    public Image getImage(String key) {
        Image result = mImageMap.get(key);
        if(result == null) {
            Log.d("joelog", "Attempted to get image key " + key + " which does not exist");
        }
        return result;
    }

}
