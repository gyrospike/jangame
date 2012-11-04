package org.alchemicstudio;


public class TrackSegment extends BaseObject {

	/** sprite drawable for a track segment */
	public Sprite mSprite;
	
	/** is this track segment being used currently */
	private boolean mInUse = false;

	/** render system reference */
	private RenderSystem system = sSystemRegistry.mRenderSystem;
	
	/**
	 * Constructor
	 */
	public TrackSegment(){
		ImagePack imagePack = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("track_segment");
		mSprite = new Sprite(imagePack, 0);
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
