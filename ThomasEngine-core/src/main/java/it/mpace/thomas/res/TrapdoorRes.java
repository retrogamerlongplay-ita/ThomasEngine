package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TrapdoorRes {
    public static TextureAtlas atlas;
   
    public static Animation<TextureRegion> closingLeftAnim;
	public static Animation<TextureRegion> closingRightAnim;
    
  

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/trapdoor.atlas"));
        
        closingLeftAnim = new Animation<>(0.20f, atlas.findRegions("trapdoor_left"), Animation.PlayMode.NORMAL);
        closingRightAnim = new Animation<>(0.20f, atlas.findRegions("trapdoor_right"), Animation.PlayMode.NORMAL);
       
       
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
        
    }
}