package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class PotProjectile {
    public Vector2 position;
    public Vector2 velocity;
    public boolean active = true;
    public Rectangle hitbox;
    private TextureRegion frame;
    private float stateTime = 0f;
    private float gravity = 800f; // regola per comportamento arcade

    public PotProjectile(float x, float y, float vx, float vy, TextureRegion frame) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(vx, vy);
        this.frame = frame;
        this.hitbox = new Rectangle(x, y, frame.getRegionWidth(), frame.getRegionHeight());
    }

    public void update(float dt) {
        stateTime += dt;
        // applica gravità (vel.y verso il basso, ricordare coordinate Y aumentano verso l'alto in libGDX)
        velocity.y -= gravity * dt;
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        hitbox.setPosition(position.x, position.y);

        // disattiva se sotto lo schermo o fuori range (adatta threshold)
        if (position.y < -100 || Math.abs(position.x) > 2000) {
            active = false;
        }
    }

    public void draw(SpriteBatch batch) {
        if (frame == null) return;
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        batch.draw(frame, position.x, position.y, w, h);
    }
}