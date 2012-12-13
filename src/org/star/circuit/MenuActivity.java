package org.star.circuit;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import org.star.R;
import org.star.game.BaseObject;
import org.star.game.GameRunnable;
import org.star.game.ImagePackXMLHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class MenuActivity extends Activity {

    /** The surface view that creates the game renderer */
    private MenuSurfaceView mGLView;

    /** the thread that updates the ui elements and other menu logic separate from the drawing loop */
    private GameRunnable mRunnable;

    /** thread that takes in the game thread as argument */
    private Thread mThread;

    /** true if the game thread is running */
    private boolean mRunning;

    /** map of the layout of the menu button grid to the level data */
    private ParsedMenuData mMenuData;

    /** persisted user data */
    private SharedPreferences mSavedUserState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BaseObject.conditionallyInitializeBaseObjects();
        BaseObject.sSystemRegistry.mAssetLibrary.conditionallyLoadFonts(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("DEBUG", "Real dpi: " + metrics.densityDpi);
        Log.d("DEBUG", "screen dimensions in dpi: " + metrics.widthPixels + " x " + metrics.heightPixels);

        mSavedUserState = getSharedPreferences(CircuitConstants.SHARED_PREFS_KEY, 0);

        // begin loading in XML data
        SAXParserFactory spf = SAXParserFactory.newInstance();

        // parse the image packs
        try {
            SAXParser sp = spf.newSAXParser();
            ImagePackXMLHandler imagePackXMLHandler = new ImagePackXMLHandler();
            sp.parse(this.getResources().openRawResource(R.raw.imagepacks), imagePackXMLHandler);
            BaseObject.sSystemRegistry.mAssetLibrary.loadImagePacks(imagePackXMLHandler.getImagePacks());
        } catch (Exception e) {
            Log.e("joelog", "QueryError, imagepacks.xml could not be parsed", e);
        }

        // parse the menumap.xml file
        try {
            SAXParser sp = spf.newSAXParser();
            MenuXMLHandler menuXMLHandler = new MenuXMLHandler();
            sp.parse(this.getResources().openRawResource(R.raw.menumap), menuXMLHandler);
            mMenuData = menuXMLHandler.getParsedData();
        } catch (Exception e) {
            Log.e("DEBUG", "QueryError, menumap.xml could not be parsed", e);
        }

        createUIElements();

        mGLView = (MenuSurfaceView) findViewById(R.id.MenuSurfaceView01);

        mRunnable = new GameRunnable();
        mRunnable.setGameManager(new MenuManager(metrics));
        mRunnable.setGameRenderer(mGLView.getRenderer());
        start();
    }

    /**
     * Show the help dialog
     */
    private void showHelpDialog() {
        Dialog dialog = new Dialog(this, R.style.CircuitDialogStyle);
        dialog.setContentView(R.layout.help_dialog);

        TextView titleTextView = (TextView) dialog.findViewById(R.id.help_body);
        titleTextView.setText(getString(R.string.help_body));

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * Create the UI elements including the level buttons, the badges, and the scrollable rows
     */
    private void createUIElements() {

        setContentView(R.layout.main);

        Button helpButton = (Button) findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showHelpDialog();
            }
        });

        HorizontalScrollView sView1 = (HorizontalScrollView) findViewById(R.id.ScrollView01);
        sView1.setVerticalScrollBarEnabled(false);
        sView1.setHorizontalScrollBarEnabled(false);

        HorizontalScrollView sView2 = (HorizontalScrollView) findViewById(R.id.ScrollView02);
        sView2.setVerticalScrollBarEnabled(false);
        sView2.setHorizontalScrollBarEnabled(false);

        HorizontalScrollView sView3 = (HorizontalScrollView) findViewById(R.id.ScrollView03);
        sView3.setVerticalScrollBarEnabled(false);
        sView3.setHorizontalScrollBarEnabled(false);

        // create the level buttons and add the callbacks for each with tags
        int numRows = mMenuData.getNumRows();
        int numCols = mMenuData.getNumColumns();

        Button[][] mapButtons = new Button[numRows][numCols];
        for(int j = 0; j < numRows; j++) {
            for(int i = 0; i < numCols; i++) {
                String buttonName = mMenuData.getButtonName(j,i); //"Button" + j + i;
                if(!buttonName.equals("")) {
                    String mapName = mMenuData.getMapName(buttonName);
                    int saveId = mMenuData.getSaveId(buttonName);
                    Class resourceIdClass = R.id.class;
                    Class stringClass = R.string.class;
                    try {
                        int resourceID = (Integer)resourceIdClass.getField(buttonName).get(resourceIdClass);
                        int mapNameResourceID = (Integer)stringClass.getField(mapName).get(stringClass);
                        Button currentButton = (Button) findViewById(resourceID);
                        currentButton.setText(getResources().getString(mapNameResourceID));
                        LayerDrawable temp = getBadgesFromSaveId(currentButton, saveId);
                        currentButton.setBackgroundDrawable(temp);
                        mapButtons[j][i] = currentButton;
                    } catch (Exception e) {
                        Log.e("DEBUG", "Error", e);
                    }

                    mapButtons[j][i].setTag(buttonName);
                    mapButtons[j][i].setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Class c1 = R.raw.class;
                            Class st = R.string.class;
                            String buttonName = (String)v.getTag();

                            String mapSource = mMenuData.getMapSourceFileName(buttonName);
                            String mapName = mMenuData.getMapName(buttonName);
                            int saveId = mMenuData.getSaveId(buttonName);
                            Log.d("joelog", "button name is: "+buttonName+" and the save id is "+saveId);
                            Intent startGameIntent = new Intent(MenuActivity.this, GameActivity.class);
                            try {
                                int resourceID = (Integer)c1.getField(mapSource).get(c1);
                                int mapNameResourceID = (Integer)st.getField(mapName).get(st);
                                startGameIntent.putExtra(ParsedMenuData.MAP_RESOURCE_KEY, resourceID );
                                startGameIntent.putExtra(ParsedMenuData.MAP_NAME_RESOURCE_KEY, mapNameResourceID );
                                startGameIntent.putExtra(ParsedMenuData.MAP_SAVE_ID_KEY, saveId);

                            } catch (Exception e) {
                                Log.e("DEBUG", "Error", e);
                            }
                            startActivity(startGameIntent);
                        }
                    });
                }
            }
        }
    }

    /**
     * Returns a layered drawable with each badge the player has unlocked for the map
     * number passed in
     *
     * @param   button      the current button so we can reference the base background image
     * @param   saveId      map number to look up the saved state with
     * @return
     */
    private LayerDrawable getBadgesFromSaveId(Button button, int saveId) {

        Boolean hasSilerBadge = mSavedUserState.getBoolean(saveId + CircuitConstants.SAVE_POSTFIX_BADGE + ReleaseManager.BADGE_INDEX_SILVER, false);
        Boolean hasGoldBadge = mSavedUserState.getBoolean(saveId + CircuitConstants.SAVE_POSTFIX_BADGE + ReleaseManager.BADGE_INDEX_GOLD, false);

        Resources res = this.getResources();
        LayerDrawable layerDrawable = (LayerDrawable) button.getBackground();
        if(hasSilerBadge) {
            Drawable silverBadgeDrawable = res.getDrawable(R.drawable.badge_silver);
            layerDrawable.setDrawableByLayerId(R.id.silver_badge_id, silverBadgeDrawable);
        }
        if(hasGoldBadge) {
            Drawable silverBadgeDrawable = res.getDrawable(R.drawable.badge_gold);
            layerDrawable.setDrawableByLayerId(R.id.gold_badge_id, silverBadgeDrawable);
        }

        return layerDrawable;
    }

    /**
     * Update the badges you may have earned after the last play session
     */
    private void updateBadgesEarned() {
        int numRows = mMenuData.getNumRows();
        int numCols = mMenuData.getNumColumns();

        Button[][] mapButtons = new Button[numRows][numCols];
        for(int j = 0; j < numRows; j++) {
            for(int i = 0; i < numCols; i++) {
                String buttonName = mMenuData.getButtonName(j,i); //"Button" + j + i;
                if(!buttonName.equals("")) {
                    int saveId = mMenuData.getSaveId(buttonName);
                    Class resourceIdClass = R.id.class;
                    try {
                        int resourceID = (Integer)resourceIdClass.getField(buttonName).get(resourceIdClass);
                        Button currentButton = (Button) findViewById(resourceID);
                        LayerDrawable tempVar = getBadgesFromSaveId(currentButton, saveId);
                        currentButton.setBackgroundDrawable(tempVar);
                        mapButtons[j][i] = currentButton;
                    } catch (Exception e) {
                        Log.e("DEBUG", "Error", e);
                    }
                }
            }
        }
    }

    /**
     * start the menu thread
     */
    public void start() {
        if (!mRunning) {
            assert mThread == null;
            // Now's a good time to run the GC.
            Runtime r = Runtime.getRuntime();
            r.gc();
            mThread = new Thread(mRunnable);
            mThread.setName("Menu");
            mThread.start();
            mRunning = true;
        } else {
            mRunnable.resumeGame();
        }
    }

    /**
     * stop the menu thread
     */
    public void stop() {
        if (mRunning) {
            if (mRunnable.getPaused()) {
                mRunnable.resumeGame();
            }
            mRunnable.stopGame();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                mThread.interrupt();
            }
            mThread = null;
            mRunning = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRunning) {
            mRunnable.pauseGame();
        }
        mGLView.onPause();

        Log.d("DEBUG", "Menu paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunnable.resumeGame();
        mGLView.onResume();
        updateBadgesEarned();
        Log.d("DEBUG", "Menu resumed");
    }

}
