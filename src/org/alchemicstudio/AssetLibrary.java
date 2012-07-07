package org.alchemicstudio;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;
//import javax.microedition.khronos.opengles.GL11;
//import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;


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

	private static BitmapFactory.Options sBitmapOptions  = new BitmapFactory.Options();

	private int[] mTextureNameWorkspace;

	//private int[] mCropWorkspace;

	/** 
	 * array of texture objects, original comment: "Textures are stored in a simple hash.  This class 
	 * implements its own array-based hash rather than using HashMap for performance" 
	 */
	public Texture[] mTextureHash;

	/** font name hash, fonts can be accessed from anywhere in the game through the object registry */
	private HashMap<String, Typeface> mFontHashMap = null;

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

	public void conditionallyLoadFonts(Context context) {
		if(mFontHashMap == null) {
			mFontHashMap = new HashMap<String, Typeface>();
			mFontHashMap.put("agency", Typeface.createFromAsset(context.getAssets(), "fonts/AGENCYR.TTF"));
		}
	}

	public Typeface getTypeFace(String tName) {
		return mFontHashMap.get(tName);
	}

	/**
	 * allocate all the textures used in the menu
	 * 
	 */
	public void loadMenuTextures() {
		int[] textureArray = {
				R.drawable.gold1,
				R.drawable.gold2,
				R.drawable.gold3,
				R.drawable.gold4,
				R.drawable.bg_head,
				R.drawable.bg_base,
				R.drawable.menu_pipe,
				R.drawable.bg_red_gear,
				R.drawable.bg_yellow_gear,
				R.drawable.bg_pink_gear,
				R.drawable.bg_crane,
				R.drawable.bg_head_border_top,
				R.drawable.bg_head_border_bottom,
				R.drawable.bg_head_left,
				R.drawable.bg_head_right,
				R.drawable.bg_rail_segment,
				R.drawable.bg_pipe,
				R.drawable.bg_foot_left,
				R.drawable.bg_foot_right
		};

		for(int i = 0; i < textureArray.length; i++) {
			allocateTexture(textureArray[i], TEXTURE_TYPE_MENU);
		}
	}

	/**
	 * allocate all the textures used in the game
	 * 
	 */
	public void loadGameTextures() {
		int[] textureArray = {
				R.drawable.grey_gate_node,
				R.drawable.yellow_gate_node,
				R.drawable.green_gate_node,
				R.drawable.grey_node,
				R.drawable.yellow_node,
				R.drawable.green_node,
				R.drawable.white_box,
				R.drawable.wire_segment,
				R.drawable.spark1,
				R.drawable.spark2,
				R.drawable.spark3,
				R.drawable.arrow,
				R.drawable.hud_gear_red,
				R.drawable.hud_gear_blue,
				R.drawable.hud_gear_green
		};

		for(int i = 0; i < textureArray.length; i++) {
			allocateTexture(textureArray[i], TEXTURE_TYPE_GAME);
		}
	}

	/** 
	 * Creates a Texture object that is mapped to the passed resource id.  If a texture has already
	 * been allocated for this id, the previously allocated Texture object is returned.
	 * @param resourceID
	 * @return
	 */
	public Texture allocateTexture(int resourceID, int type) {
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
			if (mTextureHash[x].resource != -1 && mTextureHash[x].loaded == false && mTextureHash[x].type == textureType) {
				loadBitmap(context, gl, mTextureHash[x]);
			}
		}
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
					Log.d("Texture Delete", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + mTextureHash[x].resource);
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

		if (texture.loaded == false && texture.resource != -1) {
			gl.glGenTextures(1, mTextureNameWorkspace, 0);

			int error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("Texture Load 1", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

			int textureName = mTextureNameWorkspace[0];

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);

			error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("Texture Load 2", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
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
			//gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_DECAL); 

			InputStream is = context.getResources().openRawResource(texture.resource);
			Bitmap bitmap;
			try {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inScaled = false;
				//Log.d("DEBUG", "Resource: " + texture.resource);
				bitmap = BitmapFactory.decodeStream(is, null, opts);
				//bitmap = BitmapFactory.decodeResource(context.getResources(), texture.resource, opts);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
					// Ignore.
				}
			}

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

			error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("Texture Load 3", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
			}

			assert error == GL10.GL_NO_ERROR;

			texture.name = textureName;
			texture.width = bitmap.getWidth();
			texture.height = bitmap.getHeight();

			bitmap.recycle();

			error = gl.glGetError();
			if (error != GL10.GL_NO_ERROR) {
				Log.d("Texture Load 4", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + texture.resource);
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
