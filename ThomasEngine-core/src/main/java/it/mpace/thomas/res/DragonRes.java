package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DragonRes {
	public static TextureAtlas atlas;
	public static TextureAtlas explodingAtlas;
	

	// Animazioni
	public static Animation<TextureRegion> explodingAnim;
	public static Animation<TextureRegion> dragonDisappearAnim;
	public static Animation<TextureRegion> dragonApperAnim;
	public static TextureRegion potFallingFrame;
	public static TextureRegion idleFrame;
	public static TextureRegion dragonFlameFrame;

	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/dragon.atlas"));
		explodingAtlas = new TextureAtlas(Gdx.files.internal("sprites/ExplodingBall.atlas"));
		
		// IDLE
		potFallingFrame = atlas.findRegion("dragon_ball");
		idleFrame = atlas.findRegion("dragon_idle");
		dragonFlameFrame = atlas.findRegion("dragon_flame");

		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		dragonDisappearAnim = new Animation<>(0.1f, atlas.findRegions("dragon_disappear"), Animation.PlayMode.NORMAL);
		dragonApperAnim = new Animation<>(0.08f, atlas.findRegions("dragon_smoke"), Animation.PlayMode.NORMAL);
		explodingAnim = new Animation<>(0.1f, explodingAtlas.findRegions("exploding_ball_explode"), Animation.PlayMode.NORMAL);
		
	}

	public static void dispose() {
		if (atlas != null)
			atlas.dispose();
		if(explodingAtlas != null)
			explodingAtlas.dispose();
		
	}
}