package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StickFighterRes {
    public static TextureAtlas atlas;
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> dieAnim;
    public static Animation<TextureRegion> attackHighAnim;
    public static Animation<TextureRegion> attackCrouchAnim;
    public static Animation<TextureRegion> attackMidAnim;
    public static TextureRegion hurtHigh;
    public static TextureRegion hurtLow;
    public static TextureRegion hurtMid;
    public static TextureRegion crouchHurtMid;
    public static TextureRegion crouchHurtLow;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/boss1.atlas"));
        walkAnim = new Animation<>(0.15f, atlas.findRegions("boss1_walk"), Animation.PlayMode.LOOP);
        dieAnim = new Animation<>(0.15f, atlas.findRegions("boss1_die"), Animation.PlayMode.NORMAL);
        attackHighAnim = new Animation<>(0.15f, atlas.findRegions("boss1_high_attack"), Animation.PlayMode.NORMAL);
        attackMidAnim = new Animation<>(0.15f, atlas.findRegions("boss1_attack_mid"), Animation.PlayMode.NORMAL);
        attackCrouchAnim = new Animation<>(0.15f, atlas.findRegions("boss1_crouch_attack"), Animation.PlayMode.NORMAL);
        hurtHigh = atlas.findRegion("boss1_hit_high");
        hurtLow = atlas.findRegion("boss1_hit_low");
        hurtMid = atlas.findRegion("boss1_hit_mid");
        crouchHurtMid = atlas.findRegion("boss1_crouch_hit_high");
        crouchHurtLow = atlas.findRegion("boss1_crouch_hit_low");
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
    }
}