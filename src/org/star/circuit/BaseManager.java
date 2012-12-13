package org.star.circuit;

import android.util.DisplayMetrics;
import org.star.common.types.Vector2;

public class BaseManager {

    /** const representing origin method of aligning an object */
    public static final int ORIGIN_CENTER = 0;

    /** const representing origin method of aligning an object */
    public static final int ORIGIN_TOP_LEFT = 1;

    /** const representing origin method of aligning an object */
    public static final int ORIGIN_TOP_RIGHT = 2;

    /** const representing origin method of aligning an object */
    public static final int ORIGIN_BOTTOM_LEFT = 3;

    /** const representing origin method of aligning an object */
    public static final int ORIGIN_BOTTOM_RIGHT = 4;

    /** has the manager been initialized */
    protected boolean mInitialized = false;

    /** width of the screen */
    protected int mScreenWidth;

    /** height of the screen */
    protected int mScreenHeight;

    /** metrics describing the display, contains info like screen size and pixel density */
    protected DisplayMetrics mSMetrics;

    public BaseManager(DisplayMetrics metrics) {
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mSMetrics = metrics;
    }

    public void init() {
        mInitialized = true;
    }

    /**
     * Returns a vector location given the corner or center you want to make your reference point for aligning a sprite
     *
     * @param spriteScale
     * @param xOffset
     * @param yOffset
     * @param originCode
     * @return
     */
    protected Vector2 getRelativePosition(Vector2 spriteScale, float xOffset, float yOffset, int originCode) {
        Vector2 originPoint = null;
        Vector2 result = new Vector2();
        switch(originCode) {
            case ORIGIN_CENTER:
                originPoint = new Vector2(mScreenWidth/2, mScreenHeight/2);
                result.x = originPoint.x + xOffset + spriteScale.x/2;
                result.y = originPoint.y + yOffset + spriteScale.y/2;
                break;
            case ORIGIN_TOP_LEFT:
                originPoint = new Vector2(0, 0);
                result.x = originPoint.x + xOffset;
                result.y = originPoint.y + yOffset + spriteScale.y;
                break;
            case ORIGIN_TOP_RIGHT:
                originPoint = new Vector2(mScreenWidth, 0);
                result.x = originPoint.x - xOffset - spriteScale.x;
                result.y = originPoint.y + yOffset + spriteScale.y;
                break;
            case ORIGIN_BOTTOM_LEFT:
                originPoint = new Vector2(0, mScreenHeight);
                result.x = originPoint.x + xOffset;
                result.y = originPoint.y - yOffset;
                break;
            case ORIGIN_BOTTOM_RIGHT:
                originPoint = new Vector2(mScreenWidth,mScreenHeight);
                result.x = originPoint.x - xOffset - spriteScale.x;
                result.y = originPoint.y - yOffset;
                break;
        }
        return result;
    }

    /**
     * update
     *
     * @param timeDelta
     */
    public void update(long timeDelta) {
    }

    /**
     *
     * @return	true if initialized
     */
    public boolean getInitialized() {
        return mInitialized;
    }

}
