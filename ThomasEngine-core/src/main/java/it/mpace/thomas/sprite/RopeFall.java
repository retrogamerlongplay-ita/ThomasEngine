package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import it.mpace.thomas.res.SylviaRes;

public class RopeFall {

    public float x, y;
    public float velocityY = -200f;
    public boolean landed = false;

    private TextureRegion frame;

    public RopeFall(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        // Primo frame dell’animazione (la corda verticale)
        frame = SylviaRes.stringAnim.getKeyFrame(0f);
    }

    public void update(float dt, float groundY) {

        if (!landed) {
            y += velocityY * dt;

            // Quando la corda tocca il pavimento → fermala
            if (y <= groundY) {
                y = groundY;
                velocityY = 0;
                landed = true;
            }
        }
        // Dopo aver toccato terra → non facciamo più nulla
    }
    
    public boolean getLanded() {
    	return landed;
    }

    public void draw(SpriteBatch batch) {
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        batch.draw(frame, x, y, w, h);
    }
}