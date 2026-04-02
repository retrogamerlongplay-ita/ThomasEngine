package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import it.mpace.thomas.res.BoomerangThrowerRes;

public class Boomerang {
    public Vector2 position;
    public float speed;
    public boolean active = true;
    public boolean returning = false;
    public Rectangle hitbox;
    
    private float startX;
    private final float MAX_DISTANCE = 130f; // Distanza prima di tornare
    private float stateTime = 0;
    private boolean facingRight;

    public Boomerang(float x, float y, boolean facingRight) {
        this.position = new Vector2(x, y);
        this.startX = x;
        this.facingRight = facingRight;
        this.speed = facingRight ? 140f : -140f; // Velocità iniziale
        this.hitbox = new Rectangle(x, y, 12, 12);
    }

    public void update(float dt) {
        stateTime += dt;
        position.x += speed * dt;

        // LOGICA DI RITORNO
        float distanceTraveled = Math.abs(position.x - startX);
        
        if (!returning && distanceTraveled >= MAX_DISTANCE) {
            returning = true;
            speed = -speed; // Inverte la rotta
        }

        // Se torna indietro e supera il punto di partenza (o esce dallo schermo), sparisce
        if (returning && ((facingRight && position.x < startX - 20) || (!facingRight && position.x > startX + 20))) {
            active = false;
        }

        hitbox.setPosition(position.x, position.y);
    }

    public void draw(SpriteBatch batch) {
        // L'animazione del boomerang lo fa ruotare velocemente
        TextureRegion frame = BoomerangThrowerRes.boomerangAnim.getKeyFrame(stateTime, true);
        batch.draw(frame, position.x, position.y, 16, 16);
    }
}
