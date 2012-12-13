package org.star.game;

import org.star.game.AssetLibrary;
import org.star.game.BaseObject;
import org.star.game.RenderSystem;

public class ObjectRegistry extends BaseObject {

    public RenderSystem mRenderSystem = null;
    public AssetLibrary mAssetLibrary = null;
    
    public ObjectRegistry() {
        super();
    }
}