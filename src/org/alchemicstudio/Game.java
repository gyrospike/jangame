package org.alchemicstudio;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import android.content.Context;
import android.util.Log;

public class Game {
	
	/** timer system that takes in input, updates game logic, then updates drawable content */
	private GameThread mGameThread;
	
	/** thread that takes in the game thread as argument */
	private Thread mThread;
	
	/** open gl surface view - don't know too much about this */
	private OGLSurfaceView mSurfaceView;
	
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
		mGameManager = new GameManager();
	}

	/**
	 * load external resources and then initialize the game
	 * 
	 * @param context
	 * @param mapNumber		refers to the xml file from which to get the grid and special features for the level
	 */
	public void bootstrap(Context context, int mapNumber) {

		// don't know what the purpose of this was
		//BaseObject.sSystemRegistry.openGLSystem = new OpenGLSystem(null);

		RenderSystem renderer = new RenderSystem();
		BaseObject.sSystemRegistry.mRenderSystem = renderer;
		
		TextureLibrary textureLibrary = new TextureLibrary();
		textureLibrary.loadGameTextures();
		BaseObject.sSystemRegistry.mTextureLibrary = textureLibrary;

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
			sp.parse(context.getResources().openRawResource(mapNumber), myXMLHandler);
			parsedMapData = myXMLHandler.getParsedData();
			
			mGameThread = new GameThread();
			mGameThread.setGameRenderer(mSurfaceView.getGameRenderer());
			mGameThread.setGameManager(mGameManager);
			
			mGameManager.initGame(parsedMapData, mScreenWidth, mScreenHeight);
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
		mGameThread.resumeGame();
	}

	/** setter */
	public void setSurfaceView(OGLSurfaceView view) {
		mSurfaceView = view;
	}

	/** getter */
	public GameThread getGameThread() {
		return mGameThread;
	}
	
	/** getter */
	public GameManager getGameManager() {
		return mGameManager;
	}
}