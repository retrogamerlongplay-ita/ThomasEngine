package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SylviaRes {
    public static TextureAtlas atlas;
    public static TextureAtlas symbolAtlas;
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> stringAnim;
    public static Animation<TextureRegion> sitAnim;
    public static TextureRegion hugframe;
    public static TextureRegion chairFrame;
  

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/sylvia.atlas"));
        symbolAtlas = new TextureAtlas(Gdx.files.internal("sprites/symbols.atlas"));
        walkAnim = new Animation<>(0.15f, atlas.findRegions("sylvia_walk"), Animation.PlayMode.LOOP);
        stringAnim = new Animation<>(0.15f, symbolAtlas.findRegions("string_falling"), Animation.PlayMode.NORMAL);
        sitAnim = new Animation<>(0.15f, atlas.findRegions("sylvia_sit"), Animation.PlayMode.NORMAL);
        
       
        hugframe = atlas.findRegion("sylvia_hug_full");
        chairFrame = symbolAtlas.findRegion("chair");
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
        if(symbolAtlas != null) symbolAtlas.dispose();
    }
}