package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TomTomRes {
    public static TextureAtlas atlas;
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> dieAnim;
    public static Animation<TextureRegion> grabAnim;
    public static Animation<TextureRegion> jumpAnim;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/TomTom.atlas"));
        
        walkAnim = new Animation<>(0.15f, atlas.findRegions("tomtom_walk"), Animation.PlayMode.LOOP);
        dieAnim = new Animation<>(0.15f, atlas.findRegions("tomtom_die"), Animation.PlayMode.NORMAL);
        grabAnim = new Animation<>(0.15f, atlas.findRegions("tomtom_grab"), Animation.PlayMode.LOOP);
        jumpAnim = new Animation<>(0.15f, atlas.findRegions("tomtom_jump"), Animation.PlayMode.NORMAL);
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
    }
}