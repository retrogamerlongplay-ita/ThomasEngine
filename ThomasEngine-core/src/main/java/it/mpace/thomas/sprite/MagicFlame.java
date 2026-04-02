package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import it.mpace.thomas.res.HunchbackRes;

public class MagicFlame {
    public Vector2 position;
    public Vector2 velocity;
    public boolean active = true;
    public Rectangle hitbox;
    private float stateTime = 0;
    private boolean isDiagonal;

    public MagicFlame(float x, float y, boolean facingRight, boolean diagonal) {
        this.position = new Vector2(x, y);
        this.isDiagonal = diagonal;
        
        // Se diagonale, punta verso il basso. Se orizzontale, va dritta.
        float vx = facingRight ? 130f : -130f;
        float vy = diagonal ? -100f : 0f;
        this.velocity = new Vector2(vx, vy);
        
        this.hitbox = new Rectangle(x, y, 14, 10);
    }

    public void update(float dt) {
        stateTime += dt;
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
        hitbox.setPosition(position.x - 7, position.y - 5);

        // Si disattiva se tocca il suolo o esce dallo schermo
        if (position.y < 510 || Math.abs(position.x) > 3000) {
            active = false;
        }
    }

    public void draw(SpriteBatch batch) {
        // Usiamo l'animazione corretta caricata in HunchbackRes
        TextureRegion frame = isDiagonal ? 
            HunchbackRes.flameDiagAnim.getKeyFrame(stateTime, true) : 
            HunchbackRes.flameHorizAnim.getKeyFrame(stateTime, true);
        
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        
        // Disegno con flip orizzontale coerente con la velocità X
        if (velocity.x > 0) {
            batch.draw(frame, position.x - (w/2), position.y - (h/2), w, h);
        } else {
            batch.draw(frame, position.x + (w/2), position.y - (h/2), -w, h);
        }
    }
}
