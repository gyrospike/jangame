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

	public Game(int screenScale) {
		// gives a scalar to adjust for different screen resolutions so that
		// sprites are scaled 1:1 to their pixels
		// NOTE: this is NOT a good / permanent solution
		pixToDpiScale = screenScale / 160.0f;
		mGameRoot = new GameManager();
	}

	public void bootstrap(Context context) {

		BaseObject.sSystemRegistry.openGLSystem = new OpenGLSystem(null);

		RenderSystem renderer = new RenderSystem();
		BaseObject.sSystemRegistry.renderSystem = renderer;

		TextureLibrary longTermTextureLibrary = new TextureLibrary();
		BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;

		// on D2G these units: 1 DPI = 1.5 PIXELS, so screen dims are 320 X 570
		// Body ship = new Body(240, 300, 0.0f);
		// Body redBody = new Body(240, 427, 0.1f);
		// Body blueBody = new Body(320, 570, 0.4f);

		// blueBody.setRotationOrigin(new Vector2(100, 100));

		// ship.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.ship),
		// 32 / pixToDpiScale, 32 / pixToDpiScale);
		// redBody.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.red_box),
		// 4 / pixToDpiScale, 4 / pixToDpiScale);
		// blueBody.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.blue_box),
		// 4 / pixToDpiScale, 4 / pixToDpiScale);
		
		// mGameRoot.add(ship);
		// mGameRoot.add(redBody);
		// mGameRoot.add(blueBody);

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