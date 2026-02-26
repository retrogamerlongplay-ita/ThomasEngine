package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class KnifeThrowerRes {
    public static TextureAtlas atlas;
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> dieAnim;
    public static Animation<TextureRegion> throwHighAnim;
    public static Animation<TextureRegion> throwLowAnim;
    public static TextureRegion hurtHigh;
    public static TextureRegion hurtLow;
    public static TextureRegion knife;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/knife_thrower.atlas"));
        
        walkAnim = new Animation<>(0.15f, atlas.findRegions("knife_thrower_walk"), Animation.PlayMode.LOOP);
        dieAnim = new Animation<>(0.15f, atlas.findRegions("knife_thrower_throw_die"), Animation.PlayMode.NORMAL);
        throwHighAnim = new Animation<>(0.15f, atlas.findRegions("knife_thrower_throw_high"), Animation.PlayMode.NORMAL);
        throwLowAnim = new Animation<>(0.15f, atlas.findRegions("knife_thrower_throw_low"), Animation.PlayMode.NORMAL);
        hurtHigh = atlas.findRegion("knife_thrower_hurt");
        hurtLow = atlas.findRegion("knife_thrower_throw_hurt");
        knife = atlas.findRegion("knife");
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
    }
}