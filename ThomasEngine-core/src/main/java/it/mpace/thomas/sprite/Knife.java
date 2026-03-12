package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.KnifeThrowerRes;

public class Knife {
    public Vector2 position;
    public float speed = LevelConstants.FIRST_FLOOR_KNIFE_SPEED;
    public boolean active = true;
    public boolean facingRight;
    public Rectangle hitbox;
    
    public Knife(float x, float y, boolean facingRight) {
        this.position = new Vector2(x, y);
        this.facingRight = facingRight;
        this.hitbox = new Rectangle(x, y, 12, 4); // Dimensioni tipiche di un coltello arcade
    }

    public void update(float dt) {
        position.x += (facingRight ? speed : -speed) * dt;
        hitbox.setPosition(position.x, position.y);
        
        // Disabilita se esce troppo dalla visuale (es. 300 pixel dal centro)
        // La logica di rimozione effettiva la faremo in ThomasMain
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = KnifeThrowerRes.knife; // Da aggiungere alle tue res
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        
        if (facingRight) {
            batch.draw(frame, position.x, position.y, w, h);
        } else {
            batch.draw(frame, position.x + w, position.y, -w, h);
        }
    }
}