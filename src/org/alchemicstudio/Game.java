package org.alchemicstudio;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import android.content.Context;
import android.util.Log;

public class Game {
	private GameThread mGameThread;
	private Thread mGame;
	private OGLSurfaceView mSurfaceView;
	private GameManager mGameRoot;
	private ParsedDataSet parsedMapData;

	private int screenWidth;
	private int screenHeight;
	private boolean mRunning;

	public Game(int sWidth, int sHeight) {
		screenWidth = sWidth;
		screenHeight = sHeight;
		mGameRoot = new GameManager();
	}

	public void bootstrap(Context context, int mapNumber) {

		BaseObject.sSystemRegistry.openGLSystem = new OpenGLSystem(null);

		RenderSystem renderer = new RenderSystem();
		BaseObject.sSystemRegistry.renderSystem = renderer;

		TextureLibrary longTermTextureLibrary = new TextureLibrary();
		BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;

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

		} catch (Exception e) {
			Log.e("DEBUG", "QueryError", e);
		}

		Grid mGrid = new Grid(parsedMapData.mapWidth, parsedMapData.mapHeight, parsedMapData.mapSpacing, 32.0f, screenWidth, screenHeight);

		mGrid.mSpark.mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.spark), 32.0f, 32.0f);
		mGameRoot.add(mGrid.mSpark);

		for (int k = 0; k < parsedMapData.specialNodes.getCount(); k++) {
			int tempI = parsedMapData.specialNodes.get(k).i;
			int tempJ = parsedMapData.specialNodes.get(k).j;

			mGrid.mNodes[tempI][tempJ].type = parsedMapData.specialNodes.get(k).type;
			mGrid.mNodes[tempI][tempJ].link = parsedMapData.specialNodes.get(k).link;
			mGrid.mNodes[tempI][tempJ].minSpeedLimit = parsedMapData.specialNodes.get(k).minSpeed;
			mGrid.mNodes[tempI][tempJ].maxSpeedLimit = parsedMapData.specialNodes.get(k).maxSpeed;
			if (parsedMapData.specialNodes.get(k).source) {
				mGrid.mNodes[tempI][tempJ].setSource();
			}
		}

		for (int i = 0; i < mGrid.getWidth(); i++) {
			for (int j = 0; j < mGrid.getHeight(); j++) {
				if (mGrid.mNodes[i][j].type == 2) {
					mGrid.mNodes[i][j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.grey_gate_node), 32.0f, 32.0f);
					mGrid.mNodes[i][j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.yellow_gate_node), 32.0f, 32.0f);
					mGrid.mNodes[i][j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.green_gate_node), 32.0f, 32.0f);
				} else {
					mGrid.mNodes[i][j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.grey_node), 32.0f, 32.0f);
					mGrid.mNodes[i][j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.yellow_node), 32.0f, 32.0f);
					mGrid.mNodes[i][j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.green_node), 32.0f, 32.0f);
				}
				mGameRoot.add(mGrid.mNodes[i][j]);
			}
		}

		for (int i = 0; i < mGrid.maxWireSegments; i++) {
			mGrid.mWire[i].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.wire_segment), 16.0f, 4.0f);
			mGameRoot.add(mGrid.mWire[i]);
		}
		
		mGameRoot.addGrid(mGrid);

		Particle[] particleArray = new Particle[20];

		for (int i = 0; i < particleArray.length; i++) {
			particleArray[i] = new Particle();
		}

		for (int j = 0; j < particleArray.length; j++) {
			if ((j % 2) == 0) {
				particleArray[j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.white_box), 4 , 4);
			} else {
				particleArray[j].mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.yellow_box), 4, 4);
			}
		}

		mGameRoot.setParticleArray(particleArray);
		
		/*
		Marker myMarker = new Marker(103, 230);
		Marker myMarker2 = new Marker(60, 230);
		myMarker.mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.red_box),4.0f, 4.0f);
		myMarker2.mSprite.setTextureFrame(longTermTextureLibrary.allocateTexture(R.drawable.red_box),4.0f, 4.0f );
		mGameRoot.add(myMarker);
		mGameRoot.add(myMarker2);
		*/
		
		mGameThread = new GameThread(mSurfaceView.getRenderer());
		mGameThread.setGameRoot(mGameRoot);
		mGameRoot.beginGame();
		start();
	}

	public void start() {
		if (!mRunning) {
			assert mGame == null;
			// Now's a good time to run the GC.
			Runtime r = Runtime.getRuntime();
			r.gc();
			mGame = new Thread(mGameThread);
			mGame.setName("Game");
			mGame.start();
			mRunning = true;
		} else {
			mGameThread.resumeGame();
		}
	}

	public void stop() {
		if (mRunning) {
			if (mGameThread.getPaused()) {
				mGameThread.resumeGame();
			}
			mGameThread.stopGame();
			try {
				mGame.join();
			} catch (InterruptedException e) {
				mGame.interrupt();
			}
			mGame = null;
			mRunning = false;
		}
	}

	public void pause() {
		if (mRunning) {
			mGameThread.pauseGame();
		}
	}

	public void resume() {
		mGameThread.resumeGame();
	}

	public void setSurfaceView(OGLSurfaceView view) {
		mSurfaceView = view;
	}

	public GameThread getGameThread() {
		return mGameThread;
	}
}