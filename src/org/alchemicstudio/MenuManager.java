package org.alchemicstudio;

import android.util.DisplayMetrics;

public class MenuManager extends BaseManager {

	/** max number of animated deco objects on the menu */
	private static final int MAX_NUM_ANIM_DECO = 10;
	
	/** max number of static deco objects on the menu */
	private static final int MAX_NUM_STATIC_DECO = 50;

	/** fixed array of static deco for menu background */
	private FixedSizeArray<DrawableObject> mStaticDeco = new FixedSizeArray<DrawableObject>(MAX_NUM_STATIC_DECO);
	
	/** fixed array of animated deco for menu background */
	private FixedSizeArray<DrawableObject> mAnimDeco = new FixedSizeArray<DrawableObject>(MAX_NUM_ANIM_DECO);

	/** the reference to the title which is a stored static texture rendered on game init */
	private HUDStaticTextElement mTitle;

	public MenuManager(DisplayMetrics metrics) {
        super(metrics);

		//Log.d("DEBUG", "pixels per inch y: " + mScreenMetrics.ydpi);
		//Log.d("DEBUG", "pixels per inch x: " + mScreenMetrics.xdpi);
	}

	/**
	 * populate the menu background with drawable objects positioned somewhat relatively
	 * though several items here are pixel hard coded and will break on new screen sizes
	 * TODO - fix pixel hardcoded values
	 * 
	 */
	public void init() {

		mTitle  = new HUDStaticTextElement(HUD.NOT_UNIQUE_ELEMENT, 205, 190, AssetLibrary.PRERENDERED_TEXT_INDEX_CIRCUIT);

        ImagePack borderTop = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("borderTop");
		mStaticDeco.add(new DrawableObject(borderTop, 0, mScreenWidth, -1));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_TOP_LEFT));

        ImagePack borderBottom = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("borderBottom");
		mStaticDeco.add(new DrawableObject(borderBottom, 0, mScreenWidth, -1));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 168.0f, ORIGIN_TOP_LEFT));

		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_head_left"), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_TOP_LEFT));
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_head_right"), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_TOP_RIGHT));

        ImagePack goldRobot = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("gold_robot");
		mStaticDeco.add(new DrawableObject(goldRobot, 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(),  0.2f*mSMetrics.xdpi, 0.33f*mSMetrics.ydpi, ORIGIN_TOP_RIGHT));


        ImagePack railImage = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_rail_segment");
		mStaticDeco.add(new DrawableObject(railImage, 0, mScreenWidth, -1));
		// 136dpi is 126dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(136.0f) + 37.0f, ORIGIN_TOP_LEFT));

		mStaticDeco.add(new DrawableObject(railImage, 0, mScreenWidth, -1));
		// 241dpi is 231dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(241.0f) + 37.0f, ORIGIN_TOP_LEFT));
		
		mStaticDeco.add(new DrawableObject(railImage, 0, mScreenWidth, -1));
		// 346dpi is 336dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(346.0f) + 37.0f, ORIGIN_TOP_LEFT));

        ImagePack pipeImage = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_pipe");
		mStaticDeco.add(new DrawableObject(pipeImage, 0, mScreenWidth, -1));
		// 136dpi is 126dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(136.0f) + 67.0f, ORIGIN_TOP_LEFT));

		mStaticDeco.add(new DrawableObject(pipeImage, 0, mScreenWidth, -1));
		// 241dpi is 231dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(241.0f) + 67.0f, ORIGIN_TOP_LEFT));
		
		mStaticDeco.add(new DrawableObject(pipeImage, 0, mScreenWidth, -1));
		// 346dpi is 336dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(346.0f) + 67.0f, ORIGIN_TOP_LEFT));
		

		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_crane"), 1));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.2f*mSMetrics.xdpi, 16.0f, ORIGIN_BOTTOM_RIGHT));
		
		mStaticDeco.add(new DrawableObject(borderBottom, 0, mScreenWidth, -1));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_BOTTOM_LEFT));
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_foot_left"), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 16.0f, ORIGIN_BOTTOM_LEFT));
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_foot_right"), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 16.0f, ORIGIN_BOTTOM_RIGHT));

		
		mAnimDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_pink_gear"), 0));
		mAnimDeco.getLast().setRelativePosition(getRelativePosition(mAnimDeco.getLast().mSprite.getPolyScale(), 0.0f*mSMetrics.xdpi, 0.11f*mSMetrics.ydpi, ORIGIN_BOTTOM_RIGHT));
		mAnimDeco.getLast().setRotationSpeed(1.0f);
		
		mAnimDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_red_gear"), 0));
		mAnimDeco.getLast().setRelativePosition(getRelativePosition(mAnimDeco.getLast().mSprite.getPolyScale(), 0.1f*mSMetrics.xdpi, 0.1f*mSMetrics.ydpi, ORIGIN_BOTTOM_LEFT));
		mAnimDeco.getLast().setRotationSpeed(2.0f);
		
		mAnimDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("bg_yellow_gear"), 0));
		mAnimDeco.getLast().setRelativePosition(getRelativePosition(mAnimDeco.getLast().mSprite.getPolyScale(), 0.01f*mSMetrics.xdpi, 0.15f*mSMetrics.ydpi, ORIGIN_BOTTOM_LEFT));
		mAnimDeco.getLast().setRotationSpeed(-4.0f);
		mAnimDeco.getLast().mSprite.setPolyScale(0.6f, 0.6f);
		
		super.init();
	}
	
	/**
	 * convert dip units (density independent pixels) to actual pixels
	 * 
	 * @param dip
	 * @return
	 */
	private float dipToPx(float dip) {
		float px = dip * (mSMetrics.densityDpi / mSMetrics.DENSITY_DEFAULT);
		return px;
	}

	/**
	 * update
	 * 
	 * @param timeDelta
	 */
	public void update(long timeDelta) {
		int staticLen = mStaticDeco.getCount();
		for(int i = 0; i < staticLen; i++) {
			mStaticDeco.get(i).update(timeDelta);
		}
		int animLen = mAnimDeco.getCount();
		for(int i = 0; i < animLen; i++) {
			mAnimDeco.get(i).update(timeDelta);
		}
		
		mTitle.update(timeDelta);
	}

}
