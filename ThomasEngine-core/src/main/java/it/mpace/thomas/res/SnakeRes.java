package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SnakeRes {
	public static TextureAtlas atlas;
	public static TextureAtlas explodingAtlas;
	

	// Animazioni
	public static Animation<TextureRegion> explodingAnim;
	public static Animation<TextureRegion> potCrashingAnim;
	public static Animation<TextureRegion> snakeWallkingAnim;
	public static TextureRegion potFallingFrame;
	
	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/snake.atlas"));
		explodingAtlas = new TextureAtlas(Gdx.files.internal("sprites/ExplodingBall.atlas"));
		
		// IDLE
		potFallingFrame = atlas.findRegion("pot_falling");
		
		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		potCrashingAnim = new Animation<>(0.1f, atlas.findRegions("pot_crashing"), Animation.PlayMode.NORMAL);
		snakeWallkingAnim = new Animation<>(0.08f, atlas.findRegions("snake_walking"), Animation.PlayMode.LOOP);
		explodingAnim = new Animation<>(0.3f, explodingAtlas.findRegions("exploding_ball_explode"), Animation.PlayMode.NORMAL);
		
	}

	public static void dispose() {
		if (atlas != null)
			atlas.dispose();
		if(explodingAtlas != null)
			explodingAtlas.dispose();
	}
}