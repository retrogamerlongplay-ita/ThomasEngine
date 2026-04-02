package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ExplodingBallRes {
	public static TextureAtlas atlas;
	public static TextureAtlas symbolAtlas;
	

	// Animazioni
	public static Animation<TextureRegion> explodingAnim;
	public static Animation<TextureRegion> shakingAnim;
	public static TextureRegion potFallingFrame;
	public static TextureRegion idleFrame;
	public static TextureRegion leftProjectileFrame;
	public static TextureRegion rightProjectileFrame;

	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/ExplodingBall.atlas"));
		symbolAtlas = new TextureAtlas(Gdx.files.internal("sprites/symbols.atlas"));
		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		explodingAnim = new Animation<>(0.1f, atlas.findRegions("exploding_ball_explode"), Animation.PlayMode.NORMAL);
		shakingAnim = new Animation<>(0.08f, atlas.findRegions("exploding_ball_idle"), Animation.PlayMode.NORMAL);
		idleFrame = shakingAnim.getKeyFrames()[0]; // Il primo frame dell'animazione di shaking come idle
		leftProjectileFrame = symbolAtlas.findRegion("PotProjectile_left");
		if(leftProjectileFrame == null) {
			System.out.println("Errore: PotProjectile_left non trovato nell'atlas!");
		}
		rightProjectileFrame = symbolAtlas.findRegion("PotProjectile_right");
	}

	public static void dispose() {
		if (atlas != null)
			atlas.dispose();
		if (symbolAtlas != null)
			symbolAtlas.dispose();
	}
}