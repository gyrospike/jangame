package org.alchemicstudio;


import android.app.Activity;
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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class Menu extends Activity {

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

        mSavedUserState = getSharedPreferences(BaseObject.SHARED_PREFS_KEY, 0);

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
     * Create the UI elements including the level buttons, the badges, and the scrollable rows
     */
    private void createUIElements() {

        setContentView(R.layout.main);

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
                int mapNum = mMenuData.getMapNumber(j, i);
                String buttonName = mMenuData.getButtonName(j,i); //"Button" + j + i;
                Class resourceIdClass = R.id.class;
                try {
                    int resourceID = (Integer)resourceIdClass.getField(buttonName).get(resourceIdClass);
                    Button currentButton = (Button) findViewById(resourceID);
                    currentButton.setBackgroundDrawable(getBadgesFromMapNumber(currentButton, mapNum));
                    mapButtons[j][i] = currentButton;
                } catch (Exception e) {
                    Log.e("DEBUG", "Error", e);
                }

				mapButtons[j][i].setTag(mapNum);
                mapButtons[j][i].setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Class c1 = R.raw.class;
                        int mapNum = (Integer)v.getTag();
                        Intent startGameIntent = new Intent(Menu.this, BaseActivity.class);
                        try {
                            int resourceID = (Integer)c1.getField("map"+mapNum).get(c1);
                            startGameIntent.putExtra(ParsedMenuData.MAP_RESOURCE_KEY, resourceID );
                            startGameIntent.putExtra(ParsedMenuData.MAP_NUMBER_KEY, mapNum );
                        } catch (Exception e) {
                            Log.e("DEBUG", "Error", e);
                        }
                        startActivity(startGameIntent);
                    }
                });
            }
        }
    }

    /**
     * Returns a layered drawable with each badge the player has unlocked for the map
     * number passed in
     *
     * @param   button      the current button so we can reference the base background image
     * @param   mapNumber   map number to look up the saved state with
     * @return
     */
    private LayerDrawable getBadgesFromMapNumber(Button button, int mapNumber) {

        Boolean hasBronzeBadge = mSavedUserState.getBoolean(mapNumber+BaseObject.SAVE_POSTFIX_BADGE+ReleaseManager.BADGE_INDEX_BRONZE, false);

        Resources res = this.getResources();

        //Drawable greenBadgeDrawable = res.getDrawable(R.drawable.badge_green);
        //Drawable blueBadgeDrawable = res.getDrawable(R.drawable.badge_blue);

        LayerDrawable layerDrawable = (LayerDrawable) button.getBackground();
        if(hasBronzeBadge) {
            Drawable redBadgeDrawable = res.getDrawable(R.drawable.badge_red);
            layerDrawable.setDrawableByLayerId(R.id.red_badge_id, redBadgeDrawable);
        }

        //layerDrawable.setDrawableByLayerId(R.id.green_badge_id, greenBadgeDrawable);
        //layerDrawable.setDrawableByLayerId(R.id.blue_badge_id, blueBadgeDrawable);

        return layerDrawable;
    }

    private void updateBadgesEarned() {
        int numRows = mMenuData.getNumRows();
        int numCols = mMenuData.getNumColumns();

        Button[][] mapButtons = new Button[numRows][numCols];
        for(int j = 0; j < numRows; j++) {
            for(int i = 0; i < numCols; i++) {
                int mapNum = mMenuData.getMapNumber(j, i);
                String buttonName = mMenuData.getButtonName(j,i); //"Button" + j + i;
                Class resourceIdClass = R.id.class;
                try {
                    int resourceID = (Integer)resourceIdClass.getField(buttonName).get(resourceIdClass);
                    Button currentButton = (Button) findViewById(resourceID);
                    currentButton.setBackgroundDrawable(getBadgesFromMapNumber(currentButton, mapNum));
                    mapButtons[j][i] = currentButton;
                } catch (Exception e) {
                    Log.e("DEBUG", "Error", e);
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
