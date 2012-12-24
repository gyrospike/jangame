package org.star.circuit;

import org.star.common.game.*;
import org.star.common.types.Vector2;


public class Spark extends BaseObject {

    /** const representing the red id */
    public static final int KEY_ID_RED = 1;

    /** const representing the green id */
    public static final int KEY_ID_GREEN = 2;

    /** the max velocity at which a spark will be able to move */
    public static final float MAX_VELOCITY = 400.0f;

    /** the starting velocity at which a spark will move */
    public static final float STARTING_VELOCITY = 70.0f;

    /** the standard acceleration for a spark */
    private static final float BASE_ACCELERATION = 20.0f;

    /** the velocity at which the spark will turn blue */
    private static final float SPARK_SPEED_BLUE_THRESHOLD = 200.0f;

    /** the scale of the spark sprite */
    private static final int SPARK_SPRITE_SCALE = 48;

    /** the spark's sprite */
    public Sprite mSprite;

    /** the physical representation of the spark */
    private OnRailsPhysicsObject mPhysicsObject;

    /** the current target node for this spark */
    private Node mTargetNode = null;

    /** the vector which leads from the spark last node to it's next one */
    private Vector2 mTargetVector = new Vector2();

    /** the scale in the x dimension of the sprite used in the spark */
    private float mSpriteScaleX;

    /** the scale in the x dimension of the sprite used in the spark */
    private float mSpriteScaleY;

    /** the force being applied to this spark */
    private float mForce = 0.0f;

    /** the x position of the target node, stored so we don't have to look it up */
    private float mTargetX = 0.0f;

    /** the y position of the target node, stored so we don't have to look it up */
    private float mTargetY = 0.0f;

    /** the normal x component of the target vector, stored so we don't have to look it up */
    private float mTargetVectorNormalX = 0.0f;

    /** the normal y component of the target vector, stored so we don't have to look it up */
    private float mTargetVectorNormalY = 0.0f;

    /** the distance to the target node for the x component, store so we don't have to look it up */
    private double mDistanceToTargetX;

    /** the distance to the target node for the y component, store so we don't have to look it up */
    private double mDistanceToTargetY;

    /** has this spark been released yet? */
    private boolean isReleased = false;

    /** record the last key this spark acquired */
    private int mCurrentKey = 0;


    public Spark() {
        ImagePack imagePack = BaseObject.sSystemRegistry.mAssetLibrary.getImagePack("spark");
        mSprite = new Sprite(imagePack, 1, SPARK_SPRITE_SCALE, SPARK_SPRITE_SCALE);
        mSpriteScaleX = mSprite.getPolyScale().x;
        mSpriteScaleY = mSprite.getPolyScale().y;
        mPhysicsObject = new OnRailsPhysicsObject();
    }

    /**
     * calculate the force behind the spark
     */
    private void calculateForce() {
        mDistanceToTargetX = mTargetX - mPhysicsObject.getXPos();
        mDistanceToTargetY = mTargetY - mPhysicsObject.getYPos();
        if(mPhysicsObject.getVelocity() < MAX_VELOCITY) {
            mForce = BASE_ACCELERATION;
        } else {
            //Log.d("DEBUG", "reached max velocity");
            mForce = 0.0f;
        }
        mPhysicsObject.setForce(mForce * mTargetVectorNormalX, mForce * mTargetVectorNormalY);
    }

    /**
     * set the position of the spritegimp-`
     *
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        mPhysicsObject.setPosition(getOffsetPositionX(x), getOffsetPositionY(y));
    }

    /**
     * get the position of the sprite
     */
    public Vector2 getPosition() {
        return new Vector2(mPhysicsObject.getXPos(), mPhysicsObject.getYPos());
    }

    /**
     * @param d	the starting velocity the spark should travel at
     */
    public void setStartingSpeed(float d) {
        mPhysicsObject.setStartingSpeed(d * mTargetVectorNormalX, d * mTargetVectorNormalY);
    }

    public double getCurrentSpeed() {
        return mPhysicsObject.getVelocity();
    }

    public int getCurrentKey() {
        return mCurrentKey;
    }

    public void setCurrentKey(int id) {
        switch(id) {
            case KEY_ID_RED:
                mSprite.setImageId("red");
                break;
            case KEY_ID_GREEN:
                mSprite.setImageId("green");
                break;
            default:
                mSprite.setImageId("idle");
                break;
        }
        mCurrentKey = id;
    }

    /**
     * Applies an offset given an initial position (from a node in current use case)
     *
     * @param initialPosX
     * @return
     */
    private float getOffsetPositionX(float initialPosX) {
        return initialPosX + (Grid.NODE_DIMENSION/2) - (mSpriteScaleX/2);
    }

    /**
     * Applies an offset given an initial position (from a node in current use case)
     *
     * @param initialPosY
     * @return
     */
    private float getOffsetPositionY(float initialPosY) {
        return initialPosY - (Grid.NODE_DIMENSION/2) + (mSpriteScaleY/2);
    }

    /**
     * assign a new target node for the spark to navigate towards
     * @param node
     */
    public void setTarget(Node node) {
        mTargetNode = node;
        // assume there are no more valid targets
        if(mTargetNode != null) {
            mTargetX = getOffsetPositionX(mTargetNode.getPosition().x);
            mTargetY = getOffsetPositionY(mTargetNode.getPosition().y);

            Vector2 oldTarget = new Vector2(mTargetVector.x, mTargetVector.y);
            mTargetVector.x = mTargetX - mPhysicsObject.getXPos();
            mTargetVector.y = mTargetY - mPhysicsObject.getYPos();

            mTargetVectorNormalX = mTargetVector.x / mTargetVector.normalize();
            mTargetVectorNormalY = mTargetVector.y / mTargetVector.normalize();

            if(oldTarget.x == mTargetVector.y || oldTarget.y == mTargetVector.x) {
                //Log.d("DEBUG", "switching momentum");
                float nonZero = (mTargetVector.x == 0.0f) ? mTargetVector.y : mTargetVector.x;
                mPhysicsObject.switchMomentum((int)(nonZero/Math.abs(nonZero)));
            } else {
                mPhysicsObject.addRemainder(1, false);
            }

            setReleased(true);
            //Log.d("DEBUG", "set target x: " + mTargetNode.getPosition().x);
            //Log.d("DEBUG", "set target y: " + mTargetNode.getPosition().y);
        } else {
            setReleased(false);
        }
    }

    /**
     * reset the spark to its beginning state
     */
    public void resetSpark() {
        setCurrentKey(0);
        mPhysicsObject = new OnRailsPhysicsObject();
        mForce = 0.0f;
        mDistanceToTargetX = 0.0;
        mDistanceToTargetY = 0.0;
        mTargetVector = new Vector2();
        mTargetNode = null;
        setReleased(false);
    }

    /**
     * @return	has the spark been released?
     */
    public boolean getReleased() {
        return isReleased;
    }

    /**
     * @param val set if the spark has been released or not
     */
    public void setReleased(boolean val) {
        isReleased = val;
    }

    /**
     * @return	the current target node
     */
    public Node getTarget() {
        return mTargetNode;
    }

    /**
     * @return	true if the spark is ready for a new target
     */
    public boolean getReadyForNextTarget() {
        return mPhysicsObject.hasRemainder();
    }

    /**
     * Update the appearance of the spark by changing the image it uses
     * @param timeDelta
     */
    private void updateAppearance(long timeDelta) {
        /*
        if(getCurrentSpeed() > SPARK_SPEED_BLUE_THRESHOLD) {
            mSprite.setImageId("blue");
        } else {
            mSprite.setImageId("idle");
        }
        */
        mSprite.updateFrame(timeDelta);
    }

    /**
     * update the sprite position and send it to be drawn
     */
    public void updateSprite(long timeDelta) {
        mSprite.setPosition(mPhysicsObject.getXPos(), mPhysicsObject.getYPos());
        updateAppearance(timeDelta);
        sSystemRegistry.mRenderSystem.scheduleForDraw(mSprite);
    }

    @Override
    public void update(long timeDelta) {
        calculateForce();
        mPhysicsObject.updateState(timeDelta, mDistanceToTargetX, mDistanceToTargetY);
        // HUD.getInstance().modifyTextElement(sSystemRegistry.mAssetLibrary.getStringById(R.string.speed_meter)+(int) getCurrentSpeed(), CircuitConstants.UNIQUE_ELEMENT_SPARK_SPEED);
        if(!mPhysicsObject.hasRemainder()) {
            updateSprite(timeDelta);
        }
    }
}

