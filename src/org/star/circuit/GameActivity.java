package org.star.circuit;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import org.star.R;
import org.star.common.game.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GameActivity extends Activity {

    /** maximum number of input objects that we'll store */
    public static final int INPUT_QUEUE_SIZE = 32;

    /** TODO */
    private ArrayBlockingQueue<InputObject> mInputObjectPool;

    /** The surface view that creates the game renderer */
    private GameSurfaceView mGLView;

    /** timer system that takes in input, updates game logic, then updates drawable content */
    private GameRunnable mGameRunnable;

    /** thread that takes in the game runnable as an argument */
    private Thread mThread;

    /** game logic manager */
    private GameManager mGameManager;

    /** true if the game thread is running */
    private boolean mRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initializeActivityText();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d("joelog:Graphics", "Real dpi: " + metrics.densityDpi);
        Log.d("joelog:Graphics", "screen dimensions in dpi: " + metrics.widthPixels + " x " + metrics.heightPixels);

        setContentView(R.layout.game);
        mGLView = (GameSurfaceView)findViewById(R.id.GameSurfaceView01);

        Handler levelCompleteHandler = new Handler() {
            public void handleMessage(Message msg) {
                String title = msg.getData().getString("title");
                String[] textBlocks = msg.getData().getStringArray("textArray");
                int[] resIDBlocks = msg.getData().getIntArray("resIDArray");
                showLevelCompleteDialog(title, textBlocks, resIDBlocks);
            }
        };

        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            MapXMLHandler myMapXMLHandler = new MapXMLHandler();

            int mapResource = R.raw.map0;
            int mapNameResource = R.string.level0;
            int saveId = 0;
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mapResource = extras.getInt(ParsedMenuData.MAP_RESOURCE_KEY, R.raw.map0);
                mapNameResource = extras.getInt(ParsedMenuData.MAP_NAME_RESOURCE_KEY);
                saveId = extras.getInt(ParsedMenuData.MAP_SAVE_ID_KEY);
            }

            sp.parse(getResources().openRawResource(mapResource), myMapXMLHandler);
            ParsedMapData parsedMapData = myMapXMLHandler.getParsedData();

            mGameManager = new GameManager(metrics, this, levelCompleteHandler);
            mGameRunnable = new GameRunnable();
            mGameRunnable.setGameRenderer(mGLView.getGameRenderer());
            mGameRunnable.setGameManager(mGameManager);

            mGameManager.loadData(parsedMapData, mapNameResource, saveId);
            HUD.getInstance().flushAll();
            start();

        } catch (Exception e) {
            Log.e("DEBUG", "QueryError", e);
        }

        createInputObjectPool();
    }

    /**
     * Order dependent block of text/font initialization
     */
    private void initializeActivityText() {
        BaseObject.conditionallyInitializeBaseObjects();

        HashMap<String, Typeface> fontHashMap = new HashMap<String, Typeface>();
        fontHashMap.put(CircuitConstants.TYPE_FACE_AGENCY, Typeface.createFromAsset(getAssets(), "fonts/AGENCYR.TTF"));
        BaseObject.sSystemRegistry.mAssetLibrary.setCustomFonts(fontHashMap);

        TextBoxBase[] preRenderedText = new TextBoxBase[1];
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_CIRCUIT] = new TextBoxBase();
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_CIRCUIT].setText(getResources().getString(R.string.app_title));
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_CIRCUIT].setARGB(255, 255, 255, 255);
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_CIRCUIT].setTextSize(128);
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_CIRCUIT].setCustomTypeFace(CircuitConstants.TYPE_FACE_AGENCY);

        /*
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_COMPLETE] = new TextBoxBase();
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_COMPLETE].setText(getResources().getString(R.string.circuit_complete));
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_COMPLETE].setARGB(255, 0, 255, 0);
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_COMPLETE].setTextSize(48);

        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_INCOMPLETE] = new TextBoxBase();
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_INCOMPLETE].setText(getResources().getString(R.string.circuit_incomplete));
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_INCOMPLETE].setARGB(255, 255, 0, 0);
        preRenderedText[CircuitConstants.PRERENDERED_TEXT_INDEX_INCOMPLETE].setTextSize(48);
        */

        BaseObject.sSystemRegistry.mAssetLibrary.setPrerenderedText(preRenderedText);
    }

    /**
     * Display the custom level complete dialog alerting users to any badges the earned
     *
     * @param title         the title of the dialog
     * @param textBlocks    the text to append to the list element
     * @param resIDBlocks   the resource id for the image to append to the list element
     */
    private void showLevelCompleteDialog(String title, String[] textBlocks, int[] resIDBlocks) {

        Dialog dialog = new Dialog(this, R.style.CircuitDialogStyle);
        dialog.setContentView(R.layout.complete_dialog);

        TextView titleTextView = (TextView) dialog.findViewById(R.id.title);
        titleTextView.setText(title);

        ListView list=(ListView)dialog.findViewById(R.id.list);
        CompleteDialogAdapter adapter=new CompleteDialogAdapter(this, textBlocks, resIDBlocks);
        list.setAdapter(adapter);

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
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
            mThread = new Thread(mGameRunnable);
            mThread.setName("Game");
            mThread.start();
            mRunning = true;
        } else {
            mGameRunnable.resumeGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRunning) {
            mGameRunnable.pauseGame();
        }
        mGLView.onPause();
        Log.d("joelog", "Game paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DEBUG", "resuming...");
        mGameRunnable.resumeGame();
        mGLView.onResume();
        Log.d("joelog", "Game resumed");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            if (mRunning) {
                if (mGameRunnable.getPaused()) {
                    mGameRunnable.resumeGame();
                }
                mGameRunnable.stopGame();
                try {
                    mThread.join();
                } catch (InterruptedException e) {
                    mThread.interrupt();
                }
                mThread = null;
                mRunning = false;
            }
            result = true;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // we only care about down actions in this game.
        try {
            // history first
            int hist = event.getHistorySize();
            if (hist > 0) {
                // add from oldest to newest
                for (int i = 0; i < hist; i++) {
                    InputObject input = mInputObjectPool.take();
                    input.useEventHistory(event, i);
                    mGameManager.feedInput(input);
                }
            }
            // current last
            InputObject input = mInputObjectPool.take();
            input.useEvent(event);
            mGameManager.feedInput(input);
        } catch (InterruptedException e) {
        }
        // don't allow more than 60 motion events per second
        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
        }
        return true;
    }

    /**
     * initializes the input pool
     */
    private void createInputObjectPool() {
        mInputObjectPool = new ArrayBlockingQueue<InputObject>(INPUT_QUEUE_SIZE);
        for (int i = 0; i < INPUT_QUEUE_SIZE; i++) {
            mInputObjectPool.add(new InputObject(mInputObjectPool));
        }
    }

}