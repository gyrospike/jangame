package org.alchemicstudio;


public class TrackSegment extends BaseObject {

	/** sprite drawable for a track segment */
	public Sprite mSprite;
	
	public boolean permanent;
	
	/** is this track segment being used currently */
	private boolean mInUse = false;

	/** render system reference */
	private RenderSystem system = sSystemRegistry.mRenderSystem;
	
	/**
	 * Constructor
	 */
	public TrackSegment(){
		int[] textureArray = {R.drawable.wire_segment};
		mSprite = new Sprite(textureArray, 0, 16.0f, 4.0f);
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
	public void update(float timeDelta) {
		if(mInUse) {
			system.scheduleForDraw(mSprite);
		}
	}
}
