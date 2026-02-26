package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GripperRes {
    public static TextureAtlas atlas;
    public static Animation<TextureRegion> walkAnim;
    public static Animation<TextureRegion> dieAnim;
    public static Animation<TextureRegion> grabAnim;
    public static Animation<TextureRegion> approachAnim;

    public static void load() {
        atlas = new TextureAtlas(Gdx.files.internal("sprites/gripper.atlas"));
        
        walkAnim = new Animation<>(0.15f, atlas.findRegions("gripper_walk"), Animation.PlayMode.LOOP);
        dieAnim = new Animation<>(0.15f, atlas.findRegions("gripper_die"), Animation.PlayMode.NORMAL);
        grabAnim = new Animation<>(0.15f, atlas.findRegions("gripper_grab"), Animation.PlayMode.LOOP);
        approachAnim = new Animation<>(0.15f, atlas.findRegions("gripper_approach"), Animation.PlayMode.LOOP);
    }
    
    public static void dispose() {
        if (atlas != null) atlas.dispose();
    }
}