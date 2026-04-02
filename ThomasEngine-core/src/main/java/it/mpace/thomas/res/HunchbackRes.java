package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HunchbackRes {
	public static TextureAtlas atlas;
	public static TextureAtlas crowAtlas;
	public static TextureAtlas flameAtlas;

	// Animazioni
	public static Animation<TextureRegion> walkAnim;
	public static Animation<TextureRegion> attackHighAnim;
	public static Animation<TextureRegion> attackLowAnim;
	public static Animation<TextureRegion> hurtAnim; // Per lo stato GRABBED
	public static Animation<TextureRegion> dieAnim;
	public static Animation<TextureRegion> attackMidAnim;
	public static Animation<TextureRegion> appearAnim;
	public static Animation<TextureRegion> disappearAnim;
	public static Animation<TextureRegion> disappearNoHeadAnim;
	public static Animation<TextureRegion> headRotationAnim;
	public static Animation<TextureRegion> flameHorizAnim;
	public static Animation<TextureRegion> flameDiagAnim;
	public static Animation<TextureRegion> crowAnim;
	
	public static TextureRegion hurtMidFrame;

	

	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/hunchback.atlas"));
		crowAtlas = new TextureAtlas(Gdx.files.internal("sprites/crow.atlas"));
		flameAtlas = new TextureAtlas(Gdx.files.internal("sprites/flame.atlas"));

		// HURT
		hurtMidFrame = atlas.findRegion("hunchback_hit_mid");
		

		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		walkAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_walk"), Animation.PlayMode.LOOP);
		attackHighAnim = new Animation<>(0.08f, atlas.findRegions("hunchback_attack_high"), Animation.PlayMode.NORMAL);
		attackLowAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_attack_low"), Animation.PlayMode.NORMAL);
		attackMidAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_attack_mid"), Animation.PlayMode.NORMAL);
		dieAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_die"), Animation.PlayMode.NORMAL);
		
		appearAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_appear"), Animation.PlayMode.NORMAL);
		disappearAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_disappear"), Animation.PlayMode.NORMAL);
		disappearNoHeadAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_nohead_disappear"), Animation.PlayMode.NORMAL);
		headRotationAnim = new Animation<>(0.1f, atlas.findRegions("hunchback_head"), Animation.PlayMode.LOOP);
		crowAnim = new Animation<>(0.25f, crowAtlas.findRegions("crow"), Animation.PlayMode.LOOP);
		flameHorizAnim = new Animation<>(0.1f, flameAtlas.findRegions("flame_e"), Animation.PlayMode.LOOP);
		flameDiagAnim = new Animation<>(0.1f, flameAtlas.findRegions("flame_se"), Animation.PlayMode.LOOP);

		
	}

	public static void dispose() {
	    if (atlas != null) atlas.dispose();
	    if (flameAtlas != null) flameAtlas.dispose();
	    if (crowAtlas != null) crowAtlas.dispose();
	}
}