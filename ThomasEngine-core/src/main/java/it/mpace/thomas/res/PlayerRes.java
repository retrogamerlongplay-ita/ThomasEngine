package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerRes {
    public static TextureAtlas atlas;
    
    // Animazioni
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> punchAnim;
    public static Animation<TextureRegion> kickAnim;
    public static Animation<TextureRegion> hurtAnim; // Per lo stato GRABBED
    public static Animation<TextureRegion> dieAnim;
    public static Animation<TextureRegion> jumpAnim;
    public static Animation<TextureRegion> punchCrouchAnim;
    public static Animation<TextureRegion> kickCrouchAnim;
    public static TextureRegion idleFrame;
    public static TextureRegion crouchFrame;

    public static void load() {
        // Carica il file descrittore
        atlas = new TextureAtlas(Gdx.files.internal("sprites/thomas.atlas"));

        // IDLE
        idleFrame = atlas.findRegion("thomas_idle");
        crouchFrame = atlas.findRegion("thomas_crouch");

        // ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
        walkAnim = new Animation<>(0.1f, atlas.findRegions("thomas_walk"), Animation.PlayMode.LOOP);
        punchAnim = new Animation<>(0.08f, atlas.findRegions("thomas_punch"), Animation.PlayMode.NORMAL);
        kickAnim = new Animation<>(0.1f, atlas.findRegions("thomas_kick"), Animation.PlayMode.NORMAL);
        hurtAnim = new Animation<>(0.15f, atlas.findRegions("thomas_hurt"), Animation.PlayMode.LOOP);
        dieAnim = new Animation<>(0.1f, atlas.findRegions("thomas_die"), Animation.PlayMode.NORMAL);
        jumpAnim = new Animation<>(0.1f, atlas.findRegions("thomas_jump"), Animation.PlayMode.NORMAL);
        punchCrouchAnim = new Animation<>(0.1f, atlas.findRegions("thomas_crouch_punch"), Animation.PlayMode.NORMAL);
        kickCrouchAnim = new Animation<>(0.1f, atlas.findRegions("thomas_crouch_kick"), Animation.PlayMode.NORMAL);
    }

    public static void dispose() {
        if (atlas != null) atlas.dispose();
    }
}