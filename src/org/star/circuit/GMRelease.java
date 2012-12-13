package org.star.circuit;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import org.star.R;
import org.star.common.game.HUD;
import org.star.common.game.InputObject;

public class GMRelease extends GameMode {
	
	/** the game grid, huge fat class with too much stuff in it */
	private Grid mGrid = null;

    /** the nodes from the game grid */
    private Node[][] mNodes;
	
	/** the track manages the spark navigation of the grid */
	private ReleaseManager mReleaseManager = null;
	
	/** the spark release button */
	private Button sparkReleaseButton;

	/**
	 * 
	 * 
	 * @param grid
	 * @param context
	 */
	public GMRelease(Grid grid, ReleaseManager releaseManager, Context context) {
		mGrid = grid;
		mReleaseManager = releaseManager;

        /*
		ImagePack imagePackRed = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("gear_red");
		HUD.getInstance().addElement(GameManager.GAME_MODE_RELEASE, imagePackRed, 30, 60, 0, 0, false, HUD.NOT_UNIQUE_ELEMENT);

        ImagePack imagePackGreen = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("gear_green");
		HUD.getInstance().addElement(GameManager.GAME_MODE_RELEASE, imagePackGreen, 150, 60, 0, 0, false, HUD.NOT_UNIQUE_ELEMENT);

        ImagePack imagePackBlue = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("gear_blue");
		HUD.getInstance().addElement(GameManager.GAME_MODE_RELEASE, imagePackBlue, 270, 60, 0, 0, false, HUD.NOT_UNIQUE_ELEMENT);
        */

        sparkReleaseButton = (Button) ((Activity) context).findViewById(R.id.sparkReleaseButton);
		sparkReleaseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d("DEBUG", "released the spark");
				releaseSpark();
			}
		});
	}

    /**
     * Toggle the power settings (image to id to use) for each node
     */
    private void toggleNodesPower() {
        for(int i = 0; i < mNodes.length; i++) {
            for(int j = 0; j < mNodes[i].length; j++) {
                if(mNodes[i][j] != null) {
                    mNodes[i][j].togglePower();
                }
            }
        }
    }

	/**
	 * update the release game mode
	 */
	public void update(long timeDelta) {
		mGrid.update(timeDelta);
		mReleaseManager.update(timeDelta);
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
		mReleaseManager.loadNodes(mGrid.getNodes(), mGrid.getBorderNodes());
	}

	@Override
	public void makeActive() {
		//sparkReleaseButton.setVisibility(View.VISIBLE);
        sparkReleaseButton.setEnabled(true);
		HUD.getInstance().setElementsVisibility(GameManager.GAME_MODE_RELEASE, true);
        mNodes = mGrid.getNodes();
        toggleNodesPower();
		mReleaseManager.loadNodes(mNodes, mGrid.getBorderNodes());
	}

	@Override
	public void makeInactive() {
		mReleaseManager.resetSpark();
		//sparkReleaseButton.setVisibility(View.INVISIBLE);
        sparkReleaseButton.setEnabled(false);
        toggleNodesPower();
		HUD.getInstance().setElementsVisibility(GameManager.GAME_MODE_RELEASE, false);
		//HUD.getInstance().removeStaticTextElement(HUD.UNIQUE_ELEMENT_COMPLETE);
	}
	
	/**
	 * releases the spark into the track
	 */
	private void releaseSpark() {
		//HUD.getInstance().removeStaticTextElement(HUD.UNIQUE_ELEMENT_COMPLETE);
		mReleaseManager.releaseSpark();
	}
}
