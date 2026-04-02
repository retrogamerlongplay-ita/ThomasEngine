package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BoomerangThrowerRes {
    public static TextureAtlas atlas;
    public static TextureAtlas boomerangAtlas;
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> dieAnim;
    public static Animation<TextureRegion> throwHighAnim;
    public static Animation<TextureRegion> throwLowAnim;
    public static Animation<TextureRegion> boomerangAnim;
    public static TextureRegion hurtHigh;
    public static TextureRegion hurtLow;
    public static TextureRegion hurtMid;
    public static TextureRegion knife;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/BoomerangThrower.atlas"));
        boomerangAtlas = new TextureAtlas(Gdx.files.internal("sprites/boomerang.atlas"));
        walkAnim = new Animation<>(0.15f, atlas.findRegions("boomerang_thrower_walk"), Animation.PlayMode.LOOP);
        dieAnim = new Animation<>(0.15f, atlas.findRegions("boomerang_thrower_die"), Animation.PlayMode.NORMAL);
        throwHighAnim = new Animation<>(0.15f, atlas.findRegions("boomerang_thrower_throw_high"), Animation.PlayMode.NORMAL);
        throwLowAnim = new Animation<>(0.15f, atlas.findRegions("boomerang_thrower_throw_low"), Animation.PlayMode.NORMAL);
        boomerangAnim = new Animation<>(0.1f, boomerangAtlas.findRegions("boomerang"), Animation.PlayMode.LOOP);
        hurtHigh = atlas.findRegion("boomerang_thrower_hit_high");
        hurtLow = atlas.findRegion("boomerang_thrower_hit_low");
        hurtMid = atlas.findRegion("boomerang_thrower_hit_mid");
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
        if(boomerangAtlas != null) boomerangAtlas.dispose();
    }
}