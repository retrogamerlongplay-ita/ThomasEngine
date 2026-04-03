package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import it.mpace.thomas.res.HunchbackRes;

public class HeadProjectile {
    public Vector2 position;
    public float velocityY = 200f;
    public float velocityX = 40f;
    public boolean active = true;
    private float stateTime = 0;

    public HeadProjectile(float x, float y, boolean facingRight) {
        this.position = new Vector2(x, y);
        this.velocityX = facingRight ? 40f : -40f;
    }

    public void update(float dt) {
        stateTime += dt;

        position.x += velocityX * dt;
        position.y += velocityY * dt;
        velocityY -= 800 * dt; // gravità simulata

        if (position.y < 500) { // limite pavimento
            active = false;
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = HunchbackRes.headRotationAnim.getKeyFrame(stateTime, true);
        batch.draw(frame, position.x, position.y);
    }
}