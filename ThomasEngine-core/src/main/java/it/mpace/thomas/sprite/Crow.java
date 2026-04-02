package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import it.mpace.thomas.res.HunchbackRes;

public class Crow {
    public Vector2 position;
    public float speed = 120f;
    public boolean active = true;
    public boolean facingRight;
    public Rectangle hitbox;
    
    private float stateTime = 0;
    private float startY;
    private final float AMPLITUDE = 15f; // Oscillazione su/giù
    private final float FREQUENCY = 6f;

    public Crow(float x, float y, boolean facingRight) {
        this.position = new Vector2(x, y);
        this.startY = y;
        this.facingRight = facingRight;
        // La hitbox è piccola, centrata sul corpo del corvo
        this.hitbox = new Rectangle(x, y, 12, 10);
    }

    public void update(float dt) {
        stateTime += dt;
        
        // Movimento orizzontale
        position.x += (facingRight ? speed : -speed) * dt;
        
        // Movimento sinusoidale (opzionale, per renderlo più "vivo")
        position.y = startY + MathUtils.sin(stateTime * FREQUENCY) * AMPLITUDE;

        hitbox.setPosition(position.x - 6, position.y - 5);
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = HunchbackRes.crowAnim.getKeyFrame(stateTime, true);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        
        // Disegno con flip basato sulla direzione
        if (facingRight) {
            batch.draw(frame, position.x - (w/2), position.y - (h/2), w, h);
        } else {
            batch.draw(frame, position.x + (w/2), position.y - (h/2), -w, h);
        }
    }
}
