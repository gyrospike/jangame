package jan.game;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

public class Game {
	private GameThread mGameThread;
	private Thread mGame;
	private OGLSurfaceView mSurfaceView;
	private GameManager mGameRoot;

	private float pixToDpiScale;
	private int screenWidth;
	private int screenHeight;

	public Game(int screenScale, int sWidth, int sHeight) {
		// gives a scalar to adjust for different screen resolutions so that
		// sprites are scaled 1:1 to their pixels
		// NOTE: this is NOT a good / permanent solution
		pixToDpiScale = screenScale / 160.0f;
		screenWidth = sWidth;
		screenHeight = sHeight;
		mGameRoot = new GameManager();
	}

	public void bootstrap(Context context) {

		BaseObject.sSystemRegistry.openGLSystem = new OpenGLSystem(null);

		RenderSystem renderer = new RenderSystem();
		BaseObject.sSystemRegistry.renderSystem = renderer;

		TextureLibrary longTermTextureLibrary = new TextureLibrary();
		BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;
		
		Grid mGrid = new Grid(3, 5, (32.0f / pixToDpiScale), screenWidth, screenHeight);
		
		mGrid.mSpark.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.spark), (32.0f / pixToDpiScale), (32.0f / pixToDpiScale));
		mGameRoot.add(mGrid.mSpark);
		
		for(int i = 0; i < mGrid.getWidth(); i++) {
			for(int j = 0; j < mGrid.getHeight(); j++) {
				mGrid.mNodes[i][j].mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.grey_node), (32.0f / pixToDpiScale), (32.0f / pixToDpiScale));
				mGrid.mNodes[i][j].mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.yellow_node), 32 / pixToDpiScale, 32 / pixToDpiScale);
				mGrid.mNodes[i][j].mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.green_node), 32 / pixToDpiScale, 32 / pixToDpiScale);
				mGameRoot.add(mGrid.mNodes[i][j]);
			}
		}
		
		for(int i = 0; i < mGrid.MAX_WIRE_SEGMENTS; i++) {
			mGrid.mWire[i].mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.wire_segment), (16.0f / pixToDpiScale), (4.0f / pixToDpiScale));
			mGameRoot.add(mGrid.mWire[i]);
		}
		
		//marker is useful for showing where a certain dpi location is
		Marker myMarker = new Marker(320, 10);
		myMarker.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.red_box), (4.0f / pixToDpiScale), (4.0f / pixToDpiScale));
		mGameRoot.add(myMarker);
		
		mGameRoot.addGrid(mGrid);

		/*
		Particle[] particleArray = new Particle[400];

		for (int i = 0; i < particleArray.length; i++) {
			particleArray[i] = new Particle();
		}
		
		for (int j = 0; j < particleArray.length; j++) {
			if ((j % 2) == 0) {
				particleArray[j].mSprite.setTexture(longTermTextureLibrary.
						allocateTexture(R.drawable.red_box), 4 / pixToDpiScale, 4 / pixToDpiScale);
			} else {
				particleArray[j].mSprite.setTexture(longTermTextureLibrary.
						allocateTexture(R.drawable.blue_box), 4 / pixToDpiScale, 4 / pixToDpiScale);
			}
		}

		mGameRoot.setParticleArray(particleArray);
		*/

		mGameThread = new GameThread(mSurfaceView.getRenderer());
		mGameThread.setGameRoot(mGameRoot);
		start();
	}

	public void start() {
		mGame = new Thread(mGameThread);
		mGame.setName("Game");
		mGame.start();
	}
	
	public void pause() {
		mGameThread.pauseGame();
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