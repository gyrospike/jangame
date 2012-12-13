package org.star.game;

/**
 * Simple container class for textures.  Serves as a mapping between Android resource ids and
 * OpenGL texture names, and also as a placeholder object for textures that may or may not have
 * been loaded into vram.  Objects can cache Texture objects but should *never* cache the texture
 * name itself, as it may change at any time.
 */
public class Texture {
	
	/** resource id, as in R.drawable.whatever for this texture */
    public int resource;
    
    public int name;
    
    public int width;
    
    public int height;
    
    public boolean loaded;
    
    /** used for marking certain textures to be unloaded or loaded at certain times */
    public int type;
    
    public Texture() {
        super();
        reset();
    }
    
    public void reset() {
    	type = -1;
        resource = -1;
        name = -1;
        width = 0;
        height = 0;
        loaded = false;
    }
}