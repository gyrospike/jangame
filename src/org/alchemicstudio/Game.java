package org.alchemicstudio;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class Game {
	
	/** timer system that takes in input, updates game logic, then updates drawable content */
	private GameRunnable mGameThread;
	
	/** thread that takes in the game thread as argument */
	private Thread mThread;
	
	/** open gl surface view - don't know too much about this */
	private GameSurfaceView mSurfaceView;
	
	/** game logic manager */
	private GameManager mGameManager;

	/** width of the screen */
	private int mScreenWidth;
	
	/** height of the screen */
	private int mScreenHeight;
	
	/** true if the game thread is running */
	private boolean mRunning;

	/**
	 * contains the logic and update thread for the game, the renderer is created by BaseActivity
	 * 
	 * @param sWidth	screen width
	 * @param sHeight	screen height
	 */
	public Game(int sWidth, int sHeight) {
		mScreenWidth = sWidth;
		mScreenHeight = sHeight;
	}

	/**
	 * load external resources and then initialize the game
	 * 
	 * @param context
	 * @param extras	game specific info, like which level to load
	 */
	public void bootstrap(Context context, Bundle extras) {
		ParsedDataSet parsedMapData = null;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLHandler myXMLHandler = new XMLHandler();

			// XMLReader xr = sp.getXMLReader();
			// xr.setContentHandler(myExampleHandler);
			// xr.parse(new InputSource(isr));

			// This code is used for accessing online XMLs
			/*
			 * 
			 * InputStreamReader isr = new InputStreamReader( new
			 * URL("http://www.anddev.org/images/tut/basic/parsingxml/example.xml"
			 * ).openStream() );
			 * 
			 * sp.parse(new InputSource(isr), myExampleHandler);
			 */
			
			int mapNumber = R.raw.map00;
			if (extras != null) {
				mapNumber = extras.getInt("mapNumber", R.raw.map00);
			}
			
			sp.parse(context.getResources().openRawResource(mapNumber), myXMLHandler);
			parsedMapData = myXMLHandler.getParsedData();
			
			DebugWindow mDWindow = new DebugWindow();
			
			mGameManager = new GameManager();
			
			mGameThread = new GameRunnable(mDWindow);
			mGameThread.setGameRenderer(mSurfaceView.getGameRenderer());
			mGameThread.setGameManager(mGameManager);
			
			mGameManager.loadData(context, parsedMapData, mScreenWidth, mScreenHeight, mDWindow);
			start();
		} catch (Exception e) {
			Log.e("DEBUG", "QueryError", e);
		}
	}

	/**
	 * start the game thread
	 */
	public void start() {
		if (!mRunning) {
			assert mThread == null;
			// Now's a good time to run the GC.
			Runtime r = Runtime.getRuntime();
			r.gc();
			mThread = new Thread(mGameThread);
			mThread.setName("Game");
			mThread.start();
			mRunning = true;
		} else {
			mGameThread.resumeGame();
		}
	}

	/**
	 * stop the game thread
	 */
	public void stop() {
		if (mRunning) {
			if (mGameThread.getPaused()) {
				mGameThread.resumeGame();
			}
			mGameThread.stopGame();
			try {
				mThread.join();
			} catch (InterruptedException e) {
				mThread.interrupt();
			}
			mThread = null;
			mRunning = false;
		}
	}

	/**
	 * pause the game thread
	 */
	public void pause() {
		if (mRunning) {
			mGameThread.pauseGame();
		}
	}

	/**
	 * resume the game thread
	 */
	public void resume() {
		Log.d("DEBUG", "resuming...");
		mGameThread.resumeGame();
	}

	/** setter */
	public void setSurfaceView(GameSurfaceView view) {
		mSurfaceView = view;
	}
	
	/** getter */
	public GameManager getGameManager() {
		return mGameManager;
	}
}