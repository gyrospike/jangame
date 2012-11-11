package org.alchemicstudio;


public class TrackSegment extends BaseObject {

	/** sprite drawable for a track segment */
	public Sprite mSprite;
	
	/** is this track segment being used currently */
	private boolean mInUse = false;

	/** render system reference */
	private RenderSystem system = sSystemRegistry.mRenderSystem;

    /** the x scale of the sprite for this track segment */
    private float mSpriteScaleX;

    /** the y scale of the sprite for this track segment */
    private float mSpriteScaleY;
	
	/**
	 * Constructor
	 */
	public TrackSegment(){
		ImagePack imagePack = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("track_segment");
		mSprite = new Sprite(imagePack, 0);
        mSpriteScaleX = mSprite.getPolyScale().x;
        mSpriteScaleY = mSprite.getPolyScale().y;
	}

    /**
     * @return  the scale of the y dimension by the scale of teh x dimension of the sprite
     */
    public float getSegmentYScale() {
        return mSpriteScaleY/mSpriteScaleX;
    }

    /**
     * @return  the x scale of the sprite for this track segment
     */
    public float getScaleX() {
        return mSpriteScaleX;
    }

    /**
     * @return  the y scale of the sprite for this track segment
     */
    public float getScaleY() {
        return mSpriteScaleY;
    }
	
	/**
	 * setter
	 * @param val
	 */
	public void setInUse(boolean val) {
		mInUse = val;
	}
	
	/**
	 * getter
	 * @return	true if this track segment is being used already
	 */
	public boolean getInUse() {
		return mInUse;
	}
	
	@Override
	public void update(long timeDelta) {
		if(mInUse) {
			system.scheduleForDraw(mSprite);
		}
	}
}
