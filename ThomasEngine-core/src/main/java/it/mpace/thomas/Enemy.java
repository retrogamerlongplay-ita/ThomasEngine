package it.mpace.thomas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Enemy {
    public Vector2 position;
    public float speed; // <--- Definita qui per tutti
    public boolean active = true;
    public boolean facingRight;
    protected float stateTime = 0;
    protected float velocityY = 0;
    public Rectangle hurtbox;
    public boolean isDying = false;
    public int hp = 1; // Default 1 colpo

    public Enemy(float x, float y, int hp) {
        this.position = new Vector2(x, y);
        this.hp=hp;
        // Hurtbox standard, le sottoclassi possono ridimensionarla nel costruttore
        this.hurtbox = new Rectangle(x, y, 20, 65);
    }

    public void hit(Player p) {
    	 hp--;
         if (hp <= 0) {
             this.isDying = true;
             this.velocityY = 350f;
             this.stateTime = 0; 
             this.hurtbox.set(0, 0, 0, 0); 
         } else {
             // Se ha ancora HP, resettiamo lo stateTime per l'animazione di hurt
             this.stateTime = 0;
         }
    }

    protected void updateDyingPhysics(float dt) {
        stateTime += dt;
        // Effetto volo all'indietro rispetto a dove guarda
        position.x += (facingRight ? -80 : 80) * dt; 
        position.y += velocityY * dt;
        velocityY -= 1000 * dt; // Gravità arcade
        if (position.y < -50) active = false;
    }

    // --- METODI POLIMORFICI ---
    
    public abstract void update(float dt, Player player);
    
    // Nuovo metodo per gestire il disegno in modo centralizzato
    public abstract void draw(SpriteBatch batch);
    
    public abstract void flee();

    /**
     * Helper per le sottoclassi: gestisce il flip e il centraggio
     */
    protected void drawHelper(SpriteBatch batch, TextureRegion frame) {
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        float drawX = position.x - (w / 2);

        if (facingRight) {
            batch.draw(frame, drawX, position.y, w, h);
        } else {
            batch.draw(frame, drawX + w, position.y, -w, h);
        }
    }
    
    public boolean isActive() {
    	return this.active;
    }

    // Metodo "gancio" per ThomasMain: solo i Gripper lo sovrascriveranno
    public boolean canGrabPlayer() {
        return false; 
    }
}
