package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MrXRes {
	public static TextureAtlas atlas;
	
	// Animazioni
	public static Animation<TextureRegion> walkAnim;
	public static Animation<TextureRegion> punchAnim;
	public static Animation<TextureRegion> kickAnim;
	public static Animation<TextureRegion> guardAnim;
	public static Animation<TextureRegion> dieAnim;
	public static Animation<TextureRegion> jumpAnim;
	public static Animation<TextureRegion> kickCrouchAnim;
	public static TextureRegion idleFrame;
	public static TextureRegion crouchFrame;
	public static TextureRegion kickJumpFrame;
	public static TextureRegion parryFrame;
	public static TextureRegion hurtHighFrame;
	public static TextureRegion hurtLowFrame;
	public static TextureRegion hurtMidFrame;
	public static TextureRegion disparryFrame;


	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/evil_mrx.atlas"));
		
		// IDLE
		crouchFrame = atlas.findRegion("evil_crouch");
		hurtHighFrame = atlas.findRegion("evil_hurt_high");
		hurtLowFrame =atlas.findRegion("evil_hurt_low");
		hurtMidFrame =atlas.findRegion("evil_hurt_mid");
		disparryFrame =atlas.findRegion("evil_disparry");
		kickJumpFrame = atlas.findRegion("evil_jump_kick");
		parryFrame = atlas.findRegion("evil_parry");

		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		walkAnim = new Animation<>(0.1f, atlas.findRegions("evil_walk"), Animation.PlayMode.LOOP);
		guardAnim = new Animation<>(0.1f, atlas.findRegions("evil_guard"), Animation.PlayMode.LOOP);
		punchAnim = new Animation<>(0.08f, atlas.findRegions("evil_punch"), Animation.PlayMode.NORMAL);
		kickAnim = new Animation<>(0.1f, atlas.findRegions("evil_kick"), Animation.PlayMode.NORMAL);
		dieAnim = new Animation<>(0.1f, atlas.findRegions("evil_die"), Animation.PlayMode.NORMAL);
		jumpAnim = new Animation<>(0.1f, atlas.findRegions("evil_jump"), Animation.PlayMode.NORMAL);
		kickCrouchAnim = new Animation<>(0.1f, atlas.findRegions("evil_crouch_kick"), Animation.PlayMode.NORMAL);
		idleFrame = guardAnim.getKeyFrames()[0];
	}

	public static void dispose() {
	    if (atlas != null) atlas.dispose();
	    
	}
}