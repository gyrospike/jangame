package org.star.game;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.*;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;
import org.star.R;


/**
 * original comment referred to a class TextureLibrary which still exists in part below:
 * 
 * "The Texture Library manages all textures in the game.  Textures are pooled and handed out to
 * requesting parties via allocateTexture().  However, the texture data itself is not immediately
 * loaded at that time; it may have already been loaded or it may be loaded in the future via
 * a call to loadTexture() or loadAllTextures().  This allows Texture objects to be dispersed to
 * various game systems and while the texture data itself is streamed in or loaded as necessary."
 * 
 */
public class AssetLibrary extends BaseObject {

	/** default size of the texture hash array */
	public static final int DEFAULT_SIZE = 512;

	/** identifier for all textures, used when loading or unloading only a subset of the texture hash */
	public static final int TEXTURE_TYPE_ALL = -1;

	/** identifier for menu textures, used when loading or unloading only a subset of the texture hash */
	public static final int TEXTURE_TYPE_MENU = 0;

	/** identifier for game textures, used when loading or unloading only a subset of the texture hash */
	public static final int TEXTURE_TYPE_GAME = 1;
	
	public static final int PRERENDERED_TEXT_INDEX_CIRCUIT = 0;
	
	public static final int PRERENDERED_TEXT_INDEX_COMPLETE = 1;
	
	public static final int PRERENDERED_TEXT_INDEX_INCOMPLETE = 2;
	
	public static final int PRERENDERED_TEXT_INDEX_METER = 3;

	private static BitmapFactory.Options sBitmapOptions  = new BitmapFactory.Options();

	private int[] mTextureNameWorkspace;

    private double mTotalTextureMemoryBytes = 0;

    /** image packs contains all the available textures in game keyed off of id and then image id inside each pack */
    private HashMap<String, ImagePack> mImagePacks;

	//private int[] mCropWorkspace;

	/** 
	 * array of texture objects, original comment: "Textures are stored in a simple hash.  This class 
	 * implements its own array-based hash rather than using HashMap for performance" 
	 */
	public Texture[] mTextureHash;

	/** font name hash, fonts can be accessed from anywhere in the game through the object registry */
	private HashMap<String, Typeface> mFontHashMap = null;
	
	/** font name hash, fonts can be accessed from anywhere in the game through the object registry */
	private HashMap<Integer, String> mStringHashMap = null;

	public AssetLibrary() {
		super();
		mTextureHash = new Texture[DEFAULT_SIZE];
		for (int x = 0; x < mTextureHash.length; x++) {
			mTextureHash[x] = new Texture();
		}

		mTextureNameWorkspace = new int[1];
		//mCropWorkspace = new int[4];

		sBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
	}

    /**
     * Loading of ImagePacks into memory for both accessing the images by name and also
     * loading them into OpenGL memory
     * @param imagePacks
     */
    public void loadImagePacks(HashMap<String, ImagePack> imagePacks) {
        Iterator<Map.Entry<String, ImagePack>> packIterator = imagePacks.entrySet().iterator();
        while(packIterator.hasNext()) {
            Map.Entry<String, ImagePack> packs = (Map.Entry<String, ImagePack>)packIterator.next();
            HashMap<String, Image> imageMap = packs.getValue().getImageMap();
            Iterator<Map.Entry<String, Image>> imageIterator = imageMap.entrySet().iterator();
            while(imageIterator.hasNext()) {
                Map.Entry<String, Image> imagePairs = (Map.Entry<String, Image>)imageIterator.next();
                int imageUsageCode = imagePairs.getValue().getImageUsageCode();
                int[] resourceIds = imagePairs.getValue().getResourceIds();
                int resourceIdLen = resourceIds.length;
                Texture[] textureArray = new Texture[resourceIdLen];
                for(int i = 0; i < resourceIdLen; i++) {
                    textureArray[i] = allocateTexture(resourceIds[i], imageUsageCode);
                }
                imagePairs.getValue().setTextureArray(textureArray);
            }
        }
        mImagePacks = imagePacks;
    }

    /**
     * Retrieve an image pack by name
     *
     * @param id
     * @return
     */
    public ImagePack getImagePack(String id) {
        ImagePack result = mImagePacks.get(id);
        if(result == null) {
            Log.d("joelog", "Requested a non-existant image pack " + id);
        }
        return result;
    }

	public void conditionallyLoadFonts(Context context) {
		if(mFontHashMap == null) {
			mFontHashMap = new HashMap<String, Typeface>();
			mFontHashMap.put("agency", Typeface.createFromAsset(context.getAssets(), "fonts/AGENCYR.TTF"));
		}
		
		loadStrings(context);
	}
	
	private void loadStrings(Context context) {
		int[] stringArray = {
				R.string.app_title,
				R.string.circuit_complete,
				R.string.circuit_incomplete,
				R.string.elapsed_time,
                R.string.par_time
		};
		mStringHashMap = new HashMap<Integer, String>();
		for(int i = 0; i < stringArray.length; i++) {
			mStringHashMap.put(stringArray[i], context.getString(stringArray[i]));
		}
	}
	
	public TextBoxBase[] getPrerenderedText() {
		TextBoxBase[] mBoxes = new TextBoxBase[3];
		mBoxes[PRERENDERED_TEXT_INDEX_CIRCUIT] = new TextBoxBase();
		mBoxes[PRERENDERED_TEXT_INDEX_CIRCUIT].setText(getStringById(R.string.app_title));
		mBoxes[PRERENDERED_TEXT_INDEX_CIRCUIT].setARGB(255, 255, 255, 255);
		mBoxes[PRERENDERED_TEXT_INDEX_CIRCUIT].setTextSize(128);
		
		mBoxes[PRERENDERED_TEXT_INDEX_COMPLETE] = new TextBoxBase();
		mBoxes[PRERENDERED_TEXT_INDEX_COMPLETE].setText(getStringById(R.string.circuit_complete));
		mBoxes[PRERENDERED_TEXT_INDEX_COMPLETE].setARGB(255, 0, 255, 0);
		mBoxes[PRERENDERED_TEXT_INDEX_COMPLETE].setTextSize(48);
		
		mBoxes[PRERENDERED_TEXT_INDEX_INCOMPLETE] = new TextBoxBase();
		mBoxes[PRERENDERED_TEXT_INDEX_INCOMPLETE].setText(getStringById(R.string.circuit_incomplete));
		mBoxes[PRERENDERED_TEXT_INDEX_INCOMPLETE].setARGB(255, 255, 0, 0);
		mBoxes[PRERENDERED_TEXT_INDEX_INCOMPLETE].setTextSize(48);
		
		return mBoxes;
	}
	
	public String getStringById(int id) {
		return mStringHashMap.get(id);
	}

	public Typeface getTypeFace(String tName) {
		return mFontHashMap.get(tName);
	}

	/** 
	 * Creates a Texture object that is mapped to the passed resource id.  If a texture has already
	 * been allocated for this id, the previously allocated Texture object is returned.
	 * @param resourceID
	 * @return
	 */
	private Texture allocateTexture(int resourceID, int type) {
		Texture texture = getTextureByResource(resourceID);
		if (texture == null) {
			texture = addTexture(resourceID, -1, 0, 0, type);
		}

		return texture;
	}

	/** Loads a single texture into memory.  Does nothing if the texture is already loaded. */
	/*
    public Texture loadTexture(Context context, GL10 gl, int resourceID) {
        Texture texture = allocateTexture(resourceID);
        texture = loadBitmap(context, gl, texture);
        return texture;
    }
	 */

	/** Loads all unloaded textures into OpenGL memory.  Already-loaded textures are ignored. */
	public void loadAll(int textureType, Context context, GL10 gl) {
		for (int x = 0; x < mTextureHash.length; x++) {
			if (mTextureHash[x].resource != -1 && !mTextureHash[x].loaded && mTextureHash[x].type == textureType) {
				loadBitmap(context, gl, mTextureHash[x]);
			}
		}
        Log.d("joelog", "total texture memory usage: " + (mTotalTextureMemoryBytes/1000) + " kB");
        mTotalTextureMemoryBytes = 0;
	}

	/** Flushes all textures from OpenGL memory */
	public void deleteAll(GL10 gl) {
		for (int x = 0; x < mTextureHash.length; x++) {
			if (mTextureHash[x].resource != -1 && mTextureHash[x].loaded) {
				assert mTextureHash[x].name != -1;
				mTextureNameWorkspace[0] = mTextureHash[x].name;
				mTextureHash[x].name = -1;
				mTextureHash[x].loaded = false;
				gl.glDeleteTextures(1, mTextureNameWorkspace, 0);
				int error = gl.glGetError();
				if (error != GL10.GL_NO_ERROR) {
					Log.d("joelog:Graphics", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + mTextureHash[x].resource);
				}

				assert error == GL10.GL_NO_ERROR;
			}
		}
	}

	/**
	 * Marks all textures as unloaded so that the loader will reload them in loadAll
	 * 
	 * @param type	type of texture to prep for reloading
	 */
	public void prepForReload(int type) {
		for (int x = 0; x < mTextureHash.length; x++) {
			if (mTextureHash[x].resource != -1 && mTextureHash[x].loaded && (mTextureHash[x].type == type || type == TEXTURE_TYPE_ALL)) {
				mTextureHash[x].name = -1;
				mTextureHash[x].loaded = false;
			}
		}
	}

	/** Loads a bitmap into OpenGL and sets up the common parameters for 2D texture maps. */
	protected Texture loadBitmap(Context context, GL10 gl, Texture texture) {
		assert gl != null;
		assert context != null;
		assert texture != null;

		if (!texture.loaded && texture.resource != -1) {
			gl.glGenTextures(1, mTextureNameWorkspace, 0);

            int[] maxTextureSize = new int[1];
            gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
            Log.i("glinfo", "Max texture size = " + maxTextureSize[0]);

			int error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("joelog", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

			int textureName = mTextureNameWorkspace[0];
		
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);

			error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("joelog", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

			// original texture -> [-0-]
			// repeat            -> [-0--0--0--0--0-]
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);

			// original texture -> [-0-]
			// clamp            -> [-0----------]
			//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			//gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

			// I think this is only used when dealing with lighting
			// using GL_REPLACE instead of GL_MODULATE makes it so only PNGs with no alpha channel could be transparent
			gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inScaled = false;
            // R - 5 bits, G - 6 bits, B - 5 bits, so 16 bits total, or 2 bytes
            // to find total memory used do width * height * 2 = number of bytes the bitmap uses
            opts.inPreferredConfig  =  Bitmap.Config.RGB_565;

            // galaxy nexus tablet resolution is 800 x 1205, the fat texture shows up but is clamp distorted, the big texture does not show up
            // the galaxy nexus phone is 720 x 1184, the fat texture does not show up at all, nor does the big texture
            /*
            Bitmap.Config config = Bitmap.Config.RGB_565;
            Bitmap bitmap = Bitmap.createBitmap(1024, 512, config);
            Canvas myCanvas = new Canvas(bitmap);
            Paint myPaint = new Paint();
            myPaint.setColor(Color.MAGENTA);
            myCanvas.drawCircle(100.0f, 100.0f, 20.0f, myPaint);
            */

			Log.d("joelog", "resource name: " + context.getResources().getString(texture.resource));
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texture.resource, opts);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

			error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("joelog", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

            mTotalTextureMemoryBytes += (bitmap.getWidth() * bitmap.getHeight() * 2);

			texture.name = textureName;
			texture.width = bitmap.getWidth();
			texture.height = bitmap.getHeight();

            if(bitmap.getWidth() > 1024 || bitmap.getHeight() > 1024) {
                Log.d("joelog", "GLWarning: your bitmap is larger in at least one dimension that most screens, if a texture is not showing or is just black, you may need to scale it down");
            }

			bitmap.recycle();

			error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("joelog", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

			texture.loaded = true;
		}
		return texture;
	}

	public boolean isTextureLoaded(int resourceID) {
		return getTextureByResource(resourceID) != null;
	}

	public Texture[] getTexturesByResources(int[] resourceIDs) {
		int len = resourceIDs.length;
		Texture[] result = new Texture[len];
		for(int i = 0; i < len; i++) {
			result[i] = getTextureByResource(resourceIDs[i]);
		}
		return result;
	}
	
	/**
	 * Returns the texture associated with the passed Android resource ID.
	 * @param resourceID The resource ID of a bitmap defined in R.java.
	 * @return An associated Texture object, or null if there is no associated
	 *  texture in the library.
	 */
	public Texture getTextureByResource(int resourceID) {
		int index = getHashIndex(resourceID);
		int realIndex = findFirstKey(index, resourceID);
		Texture texture = null;
		if (realIndex != -1) {
			texture = mTextureHash[realIndex];
		}        
		return texture;
	}

	private int getHashIndex(int id) {
		return id % mTextureHash.length;
	}

	/**
	 * Locates the texture in the hash.  This hash uses a simple linear probe chaining mechanism:
	 * if the hash slot is occupied by some other entry, the next empty array index is used.  
	 * This is O(n) for the worst case (every slot is a cache miss) but the average case is
	 * constant time. 
	 * @param startIndex
	 * @param key
	 * @return
	 */
	private int findFirstKey(int startIndex, int key) {
		int index = -1;
		for (int x = 0; x < mTextureHash.length; x++) {
			final int actualIndex = (startIndex + x) % mTextureHash.length;
			if (mTextureHash[actualIndex].resource == key) {
				index = actualIndex;
				break;
			} else if (mTextureHash[actualIndex].resource == -1) {
				break;
			}
		}
		return index;
	}

	/** Inserts a texture into the hash */
	protected Texture addTexture(int id, int name, int width, int height, int type) {
		int index = findFirstKey(getHashIndex(id), -1);
		Texture texture = null;
		assert index != -1;

		if (index != -1) {
			mTextureHash[index].resource = id;
			mTextureHash[index].name = name;
			mTextureHash[index].width = width;
			mTextureHash[index].height = height;
			mTextureHash[index].type = type;
			texture = mTextureHash[index];
		}

		return texture;
	}

	public void removeAll() {
		for (int x = 0; x < mTextureHash.length; x++) {
			mTextureHash[x].reset();
		}
	}

}

