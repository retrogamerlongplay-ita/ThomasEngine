package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class HitEffect {
    public float x, y;
    public float timer;
    public final float DURATION = 0.15f; // Durata lampo (molto breve)
    public boolean active = true;
    private TextureRegion frame;

    public HitEffect(float x, float y, TextureRegion frame) {
        this.x = x;
        this.y = y;
        this.frame = frame;
        this.timer = 0;
    }

    public void update(float dt) {
        timer += dt;
        if (timer >= DURATION) active = false;
    }

    public void draw(SpriteBatch batch) {
        // Disegniamo la scintilla centrata sul punto di impatto
        batch.draw(frame, x - (frame.getRegionWidth()/2f), y - (frame.getRegionHeight()/2f));
    }
}
