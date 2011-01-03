package jan.game;

import android.content.Context;
import android.util.Log;

public class Game {
	private GameThread mGameThread;
	private Thread mGame;
	private OGLSurfaceView mSurfaceView;
	private ObjectManager mGameRoot;

	public Game() {
		mGameRoot = new ObjectManager();
	}

	public void bootstrap(Context context) {
		
		BaseObject.sSystemRegistry.openGLSystem = new OpenGLSystem(null);
		
		RenderSystem renderer = new RenderSystem();
		BaseObject.sSystemRegistry.renderSystem = renderer;

		TextureLibrary longTermTextureLibrary = new TextureLibrary();
		BaseObject.sSystemRegistry.longTermTextureLibrary = longTermTextureLibrary;
		
		Body ship = new Body(240, 300, 0.0f);
		Body redBody = new Body(240, 427, 0.1f);
		Body blueBody = new Body(40, 40, 0.4f);
		
		blueBody.setRotationOrigin(new Vector2(100, 100));
		
		ship.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.ship), 32, 32);
		redBody.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.red_box), 4, 4);
		blueBody.mSprite.setTexture(longTermTextureLibrary.allocateTexture(R.drawable.blue_box), 4, 4);
		
		mGameRoot.add(ship);
		mGameRoot.add(redBody);
		mGameRoot.add(blueBody);
		
		mGameThread = new GameThread(mSurfaceView.getRenderer());
		mGameThread.setGameRoot(mGameRoot);
		start();
	}

	public void start() {
		mGame = new Thread(mGameThread);
		mGame.setName("Game");
		mGame.start();
	}

	public void setSurfaceView(OGLSurfaceView view) {
		mSurfaceView = view;
	}
	
	public GameThread getGameThread() {
		return mGameThread;
	}
}