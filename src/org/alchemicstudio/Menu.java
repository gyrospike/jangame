package org.alchemicstudio;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;


public class Menu extends Activity {
	
	/** number of columns of levels - TODO - generalize */
	private static final int LEVEL_COLS = 4;
	
	/** number of rows of levels - TODO - generalize */
	private static final int LEVEL_ROWS = 3;
	
	/** The surface view that creates the game renderer */
	private MenuBackgroundView mGLView;
	
	/** the thread that updates the ui elements and other menu logic separate from the drawing loop */
	// TODO - this is almost exactly the same as the GameThread, soon to be GameRunnable, generalize
	private MenuRunnable mRunnable;
	
	/** thread that takes in the game thread as argument */
	private Thread mThread;
	
	/** true if the game thread is running */
	private boolean mRunning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// system wide processes
		// TODO - find a better place to store these initializations
		// these need to be ready before an activity can be launched
		RenderSystem renderer = new RenderSystem();
		BaseObject.sSystemRegistry.mRenderSystem = renderer;
		
		AssetLibrary assetLibrary = new AssetLibrary();
		BaseObject.sSystemRegistry.mAssetLibrary = assetLibrary;
		
		createUIElements();
		
		mGLView = (MenuBackgroundView) findViewById(R.id.MenuBackgroundView01);
		
		mRunnable = new MenuRunnable();
		mRunnable.setGameManager(new MenuManager());
		mRunnable.setGameRenderer(mGLView.getRenderer());
		start();
	}

	/**
	 * Create the UI elements including the level buttons, the badges, and the scrollable rows
	 */
	private void createUIElements() {
		// sets badges on level icons
		Resources res = this.getResources();
		Drawable blueBadgeDrawable = res.getDrawable(R.drawable.blue_badge);
		Drawable redBadgeDrawable = res.getDrawable(R.drawable.red_badge);

		LayerDrawable layerDrawable = (LayerDrawable) res.getDrawable(R.layout.level01_button);
		layerDrawable.setDrawableByLayerId(R.id.red_badge_id, redBadgeDrawable);
		layerDrawable.setDrawableByLayerId(R.id.blue_badge_id, blueBadgeDrawable);

		setContentView(R.layout.main);

		HorizontalScrollView sView1 = (HorizontalScrollView) findViewById(R.id.ScrollView01);
		// Hide the Scollbar
		sView1.setVerticalScrollBarEnabled(false);
		sView1.setHorizontalScrollBarEnabled(false);

		HorizontalScrollView sView2 = (HorizontalScrollView) findViewById(R.id.ScrollView02);
		sView2.setVerticalScrollBarEnabled(false);
		sView2.setHorizontalScrollBarEnabled(false);

		HorizontalScrollView sView3 = (HorizontalScrollView) findViewById(R.id.ScrollView03);
		sView3.setVerticalScrollBarEnabled(false);
		sView3.setHorizontalScrollBarEnabled(false);

		// create the level buttons and add the callbacks for each with tags
		Button[][] mapButtons = new Button[LEVEL_ROWS][LEVEL_COLS];
		for(int j = 0; j < LEVEL_ROWS; j++) {
			for(int i = 0; i < LEVEL_COLS; i++) {
				String buttonName = "Button" + j + i;
				Class c = R.id.class;
				try {
					int resourceID = (Integer)c.getField(buttonName).get(c);
					mapButtons[j][i] = (Button) findViewById(resourceID);
				} catch (Exception e) {
					Log.e("DEBUG", "Error", e);
				}

				mapButtons[j][i].setTag("map" + j + i);
				mapButtons[j][i].setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Class c1 = R.raw.class;
						String passedTag = (String)v.getTag();
						Intent StartGameIntent = new Intent(Menu.this, BaseActivity.class);
						try {
							int resourceID = (Integer)c1.getField(passedTag).get(c1);
							StartGameIntent.putExtra("mapNumber", resourceID );
						} catch (Exception e) {
							Log.e("DEBUG", "Error", e);
						}
						startActivity(StartGameIntent);
					}
				});
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
		Log.d("DEBUG", "Menu resumed");
	}

}
