package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ButterflyRes {
	public static TextureAtlas atlas;
	public static TextureAtlas explodingAtlas;
	

	// Animazioni
	public static Animation<TextureRegion> flyingAnim;
	public static Animation<TextureRegion> explodingAnim;
	
	public static TextureRegion idleFrame;
	public static TextureRegion southFrame;
	public static TextureRegion southEastFrame;
	public static TextureRegion southWestFrame;
	
	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/butterfly.atlas"));
		explodingAtlas = new TextureAtlas(Gdx.files.internal("sprites/ExplodingBall.atlas"));
		
		// IDLE
		idleFrame = atlas.findRegion("butterfly_n");
		southFrame = atlas.findRegion("butterfly_s");
		southEastFrame = atlas.findRegion("butterfly_se");
		southWestFrame = atlas.findRegion("butterfly_sw");
		
		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		flyingAnim = new Animation<>(0.1f, atlas.findRegions("butterfly_e"), Animation.PlayMode.LOOP);
		explodingAnim = new Animation<>(0.1f, explodingAtlas.findRegions("exploding_ball_explode"), Animation.PlayMode.NORMAL);
		
	}

	public static void dispose() {
		if (atlas != null)
			atlas.dispose();
		if(explodingAtlas != null)
			explodingAtlas.dispose();
	}
}