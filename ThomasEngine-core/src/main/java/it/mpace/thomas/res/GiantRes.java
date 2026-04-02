package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GiantRes {
	public static TextureAtlas atlas;

	// Animazioni
	public static Animation<TextureRegion> walkAnim;
	public static Animation<TextureRegion> punchAnim;
	public static Animation<TextureRegion> kickAnim;
	public static Animation<TextureRegion> dieAnim;

	public static void load() {
		// Carica il file descrittore
		atlas = new TextureAtlas(Gdx.files.internal("sprites/giant.atlas"));
	
		// ANIMAZIONI: findRegions (al plurale) cerca tutti i file con prefisso e numero
		walkAnim = new Animation<>(0.1f, atlas.findRegions("giant_walk"), Animation.PlayMode.LOOP);
		punchAnim = new Animation<>(0.08f, atlas.findRegions("giant_punch"), Animation.PlayMode.NORMAL);
		kickAnim = new Animation<>(0.1f, atlas.findRegions("giant_kick"), Animation.PlayMode.NORMAL);
		dieAnim = new Animation<>(0.1f, atlas.findRegions("giant_die"), Animation.PlayMode.NORMAL);
		
		
	}

	public static void dispose() {
	    if (atlas != null) atlas.dispose();
	}
}