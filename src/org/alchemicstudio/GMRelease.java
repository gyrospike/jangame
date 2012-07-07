package org.alchemicstudio;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GMRelease extends GameMode {
	
	/** the game grid, huge fat class with too much stuff in it */
	private Grid mGrid = null;
	
	/** the track manages the spark navigation of the grid */
	private Track mTrack = null;
	
	/** the spark release button */
	private Button sparkReleaseButton;

	/**
	 * 
	 * 
	 * @param grid
	 * @param context
	 */
	public GMRelease(Grid grid, Context context) {
		mGrid = grid;
		mTrack = new Track();
		
		Texture red = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.hud_gear_red);
		HUD.getInstance().addElement(GameManager.GAME_MODE_RELEASE, red, 30, 60, 0, 300, false, HUD.NOT_UNIQUE_ELEMENT);
		
		Texture green = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.hud_gear_green);
		HUD.getInstance().addElement(GameManager.GAME_MODE_RELEASE, green, 150, 60, 0, 300, false, HUD.NOT_UNIQUE_ELEMENT);
		
		Texture blue = BaseObject.sSystemRegistry.mAssetLibrary.getTextureByResource(R.drawable.hud_gear_blue);
		HUD.getInstance().addElement(GameManager.GAME_MODE_RELEASE, blue, 270, 60, 0, 300, false, HUD.NOT_UNIQUE_ELEMENT);

		sparkReleaseButton = (Button) ((Activity) context).findViewById(R.id.sparkReleaseButton);
		sparkReleaseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d("DEBUG", "released the spark");
				releaseSpark();
			}
		});
	}

	/**
	 * update the release game mode
	 */
	public void update(long timeDelta) {
		mGrid.update(timeDelta);
		mTrack.update(timeDelta);
	}

	/**
	 * handle touch move event
	 * 
	 * @param input
	 */
	public void processTouchMoveEvent(InputObject input) {
		mGrid.growTrackSwitchChain(input.x, input.y);
	}

	/**
	 * handle touch down event
	 * 
	 * @param input
	 */
	public void processTouchDownEvent(InputObject input) {
		mGrid.startTrackSwitchChain(input.x, input.y);
	}

	/**
	 * handle touch up event
	 * 
	 * @param input
	 */
	public void processTouchUpEvent(InputObject input) {
		mGrid.stopTrackSwitchChain();
		mTrack.loadNodes(mGrid.getNodes(), mGrid.getBorderNodes());
	}

	@Override
	public void makeActive() {
		sparkReleaseButton.setVisibility(View.VISIBLE);
		HUD.getInstance().setElementsVisibility(GameManager.GAME_MODE_RELEASE, true);
	}

	@Override
	public void makeInactive() {
		mTrack.resetSpark();
		sparkReleaseButton.setVisibility(View.INVISIBLE);
		HUD.getInstance().setElementsVisibility(GameManager.GAME_MODE_RELEASE, false);
	}
	
	/**
	 * releases the spark into the track
	 */
	private void releaseSpark() {
		mTrack.loadNodes(mGrid.getNodes(), mGrid.getBorderNodes());
		mTrack.releaseSpark();
	}
}
