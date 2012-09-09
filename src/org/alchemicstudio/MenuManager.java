package org.alchemicstudio;

import android.util.DisplayMetrics;

public class MenuManager extends BaseManager {

	/** const representing origin method of aligning an object */
	public static final int ORIGIN_CENTER = 0;

	/** const representing origin method of aligning an object */
	public static final int ORIGIN_TOP_LEFT = 1;

	/** const representing origin method of aligning an object */
	public static final int ORIGIN_TOP_RIGHT = 2;

	/** const representing origin method of aligning an object */
	public static final int ORIGIN_BOTTOM_LEFT = 3;

	/** const representing origin method of aligning an object */
	public static final int ORIGIN_BOTTOM_RIGHT = 4;

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

	/** contains information about the screen */
	private DisplayMetrics mSMetrics;

	public MenuManager(DisplayMetrics metrics) {
		mSMetrics = metrics;

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
		
		Texture borderTop = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_head_border_top);
		mStaticDeco.add(new DrawableObject(borderTop, 0, mSMetrics.widthPixels, borderTop.height));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_TOP_LEFT));
		
		Texture borderBottom = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_head_border_bottom);
		mStaticDeco.add(new DrawableObject(borderBottom, 0, mSMetrics.widthPixels, borderBottom.height));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 168.0f, ORIGIN_TOP_LEFT));

		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_head_left), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_TOP_LEFT));
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_head_right), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_TOP_RIGHT));

		int[] goldRobotTextureArray = {R.drawable.gold1, R.drawable.gold2, R.drawable.gold3};
		Texture[] goldRobotTextures = BaseObject.sSystemRegistry.mAssetLibrary.getTexturesByResources(goldRobotTextureArray);
		mStaticDeco.add(new DrawableObject(goldRobotTextures, 0, 300));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(),  0.2f*mSMetrics.xdpi, 0.33f*mSMetrics.ydpi, ORIGIN_TOP_RIGHT));
		
		
		Texture railTexture = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_rail_segment);
		
		mStaticDeco.add(new DrawableObject(railTexture, 0, mSMetrics.widthPixels, railTexture.height));
		// 136dpi is 126dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(136.0f) + 37.0f, ORIGIN_TOP_LEFT));

		mStaticDeco.add(new DrawableObject(railTexture, 0, mSMetrics.widthPixels, railTexture.height));
		// 241dpi is 231dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(241.0f) + 37.0f, ORIGIN_TOP_LEFT));
		
		mStaticDeco.add(new DrawableObject(railTexture, 0, mSMetrics.widthPixels, railTexture.height));
		// 346dpi is 336dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(346.0f) + 37.0f, ORIGIN_TOP_LEFT));

		Texture pipeTexture = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_pipe);
		
		mStaticDeco.add(new DrawableObject(pipeTexture, 0, mSMetrics.widthPixels, pipeTexture.height));
		// 136dpi is 126dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(136.0f) + 67.0f, ORIGIN_TOP_LEFT));

		mStaticDeco.add(new DrawableObject(pipeTexture, 0, mSMetrics.widthPixels, pipeTexture.height));
		// 241dpi is 231dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(241.0f) + 67.0f, ORIGIN_TOP_LEFT));
		
		mStaticDeco.add(new DrawableObject(pipeTexture, 0, mSMetrics.widthPixels, pipeTexture.height));
		// 346dpi is 336dpi + 10dpi, the xml dpi offsets, + 37px, the offset in the image to the point I want
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, dipToPx(346.0f) + 67.0f, ORIGIN_TOP_LEFT));
		
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_crane), 1));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.2f*mSMetrics.xdpi, 16.0f, ORIGIN_BOTTOM_RIGHT));
		
		mStaticDeco.add(new DrawableObject(borderBottom, 0, mSMetrics.widthPixels, borderBottom.height));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 0.0f, ORIGIN_BOTTOM_LEFT));
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_foot_left), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 16.0f, ORIGIN_BOTTOM_LEFT));
		
		mStaticDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_foot_right), 0));
		mStaticDeco.getLast().setRelativePosition(getRelativePosition(mStaticDeco.getLast().mSprite.getPolyScale(), 0.0f, 16.0f, ORIGIN_BOTTOM_RIGHT));

		
		mAnimDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_pink_gear), 0));
		mAnimDeco.getLast().setRelativePosition(getRelativePosition(mAnimDeco.getLast().mSprite.getPolyScale(), 0.0f*mSMetrics.xdpi, 0.11f*mSMetrics.ydpi, ORIGIN_BOTTOM_RIGHT));
		mAnimDeco.getLast().setRotationSpeed(1.0f);
		
		mAnimDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_red_gear), 0));
		mAnimDeco.getLast().setRelativePosition(getRelativePosition(mAnimDeco.getLast().mSprite.getPolyScale(), 0.1f*mSMetrics.xdpi, 0.1f*mSMetrics.ydpi, ORIGIN_BOTTOM_LEFT));
		mAnimDeco.getLast().setRotationSpeed(2.0f);
		
		mAnimDeco.add(new DrawableObject(BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.bg_yellow_gear), 0));
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
	 * Returns a vector location given the corner or center you want to make your reference point for aligning a sprite
	 * 
	 * @param spriteScale
	 * @param xOffset
	 * @param yOffset
	 * @param originCode
	 * @return
	 */
	public Vector2 getRelativePosition(Vector2 spriteScale, float xOffset, float yOffset, int originCode) {
		Vector2 originPoint = null;
		Vector2 result = new Vector2();
		switch(originCode) {
		case ORIGIN_CENTER:
			originPoint = new Vector2(mSMetrics.widthPixels/2, mSMetrics.heightPixels/2);
			result.x = originPoint.x + xOffset + spriteScale.x/2;
			result.y = originPoint.y + yOffset + spriteScale.y/2;
			break;
		case ORIGIN_TOP_LEFT:
			originPoint = new Vector2(0, 0);
			result.x = originPoint.x + xOffset;
			result.y = originPoint.y + yOffset + spriteScale.y;
			break;
		case ORIGIN_TOP_RIGHT:
			originPoint = new Vector2(mSMetrics.widthPixels, 0);
			result.x = originPoint.x - xOffset - spriteScale.x;
			result.y = originPoint.y + yOffset + spriteScale.y;
			break;
		case ORIGIN_BOTTOM_LEFT:
			originPoint = new Vector2(0, mSMetrics.heightPixels);
			result.x = originPoint.x + xOffset;
			result.y = originPoint.y - yOffset;
			break;
		case ORIGIN_BOTTOM_RIGHT:
			originPoint = new Vector2(mSMetrics.widthPixels, mSMetrics.heightPixels);
			result.x = originPoint.x - xOffset - spriteScale.x;
			result.y = originPoint.y - yOffset;
			break;
		}
		return result;
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
