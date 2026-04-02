package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.SylviaRes;

public class Sylvia {

    public enum State {
        SITTING,    // all’inizio
        STANDING,   // appena liberata
        RUNNING,    // corre verso Thomas
        HUGGING     // abbraccio finale
    }

    public float x, y;
    public boolean facingRight = true;
    public float stateTime = 0;

    public State state = State.SITTING;

    public Rectangle hurtbox = new Rectangle();

    public Sylvia(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float dt, float targetX) {
        stateTime += dt;

        switch (state) {
        case SITTING:
            // nulla, resta ferma
            break;

        case STANDING:
            // piccola pausa opzionale se vuoi
            break;

        case RUNNING:
            float speed = 60f;
            facingRight = (targetX > x);
            x += (facingRight ? speed : -speed) * dt;

            // transizione automatico a HUG
            if (Math.abs(x - targetX) < 14f) {
                state = State.HUGGING;
                stateTime = 0;
            }
            break;

        case HUGGING:
            // frame fisso
            break;
        }

        // hurtbox minimale
        hurtbox.set(x - 8, y, 16, 50);
    }

    public void draw(SpriteBatch batch) {

        TextureRegion frame;

        switch (state) {

        case SITTING:
            frame = SylviaRes.sitAnim.getKeyFrame(stateTime, true);
            break;

        case STANDING:
            frame = SylviaRes.walkAnim.getKeyFrame(0); // un frame neutro
            break;

        case RUNNING:
            frame = SylviaRes.walkAnim.getKeyFrame(stateTime, true);
            break;

        case HUGGING:
        default:
            frame = SylviaRes.hugframe;
            break;
        }

        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();

        if (facingRight)
            batch.draw(frame, x, y, w, h);
        else
            batch.draw(frame, x + w, y, -w, h);
    }
}