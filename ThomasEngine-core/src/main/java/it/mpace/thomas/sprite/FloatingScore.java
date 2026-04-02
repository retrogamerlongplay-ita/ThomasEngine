package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class FloatingScore {
    public Vector2 position;
    public String text;
    public float alpha = 1.0f;
    public boolean active = true;
    private float timer = 0;
    private final float DURATION = 1.0f;

    public FloatingScore(float x, float y, int score) {
        this.position = new Vector2(x, y);
        this.text = String.valueOf(score);
    }

    public void update(float dt) {
        timer += dt;
        position.y += 20 * dt; // Sale verso l'alto
        alpha = 1.0f - (timer / DURATION); // Sfuma gradualmente
        if (timer >= DURATION) active = false;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        font.setColor(1, 1, 1, alpha); // Bianco con trasparenza
        font.draw(batch, text, position.x, position.y);
        font.setColor(Color.WHITE); // Reset per gli altri testi
    }
}
