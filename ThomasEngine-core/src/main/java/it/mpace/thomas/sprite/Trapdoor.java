package it.mpace.thomas.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import it.mpace.thomas.res.TrapdoorRes;

public class Trapdoor {
	public Vector2 position;
	public boolean active = true;
	public boolean returning = false;

	private float stateTime = 0;
	private boolean facingRight;

	public Trapdoor(float x, float y, boolean facingRight) {
		this.position = new Vector2(x, y);
		this.facingRight = facingRight;
	}

	public void update(float dt) {
        stateTime += dt;
         if (facingRight) {
			if (TrapdoorRes.closingRightAnim.isAnimationFinished(stateTime)) {
				active = false; // Disattiva la trappola quando l'animazione è finita
				//this.dispose();
			}
		} else {
			if (TrapdoorRes.closingLeftAnim.isAnimationFinished(stateTime)) {
				active = false; // Disattiva la trappola quando l'animazione è finita
			}
        }
	}

	public void draw(SpriteBatch batch) {
		if (!active) {
			return; // Non disegnare se la trappola è inattiva
		}
		TextureRegion frame;
		if (this.facingRight) {
			frame = TrapdoorRes.closingLeftAnim.getKeyFrame(stateTime, false);
		} else {
			frame = TrapdoorRes.closingRightAnim.getKeyFrame(stateTime, false);
		}
		batch.draw(frame, position.x, position.y, frame.getRegionWidth(), frame.getRegionHeight());
	}
}
