package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.screen.LevelScreen;

public abstract class FallingHazard extends Enemy {

	protected enum HazardState {
		FALLING, IMPACT, SPAWNING_ENTITY, ACTIVE, DONE, EXPLODING
	}

	protected HazardState hazardState = HazardState.FALLING;

	// Default damage applied if this hazard hits the player while still falling
	protected float fallDamage = 20f;

	/**
	 * Hook: subclasses that already implement custom behavior when colliding with the player
	 * while falling should override this and return true to indicate the collision was handled.
	 */
	protected boolean handleFallingPlayerCollision(Player player) {
		return false;
	}

	protected float gravity = 600f;
	protected float groundY;
	protected TextureRegion potFrame;
	protected LevelScreen level;

	public FallingHazard(float x, float y, float groundY, TextureRegion potFrame, LevelScreen level) {
		super(x, y, 1); // 1 HP di default per l'involucro
		this.level = level;
		this.groundY = groundY;
		this.potFrame = potFrame;
		this.speed = 0; // Cade verticalmente
		this.hurtbox = new Rectangle(x, y, 16, 16); // Involucro piccolo
	}

	@Override
	public void update(float dt, Player player) {
		if (this.isDying) {
	        if (hazardState == HazardState.FALLING || hazardState == HazardState.EXPLODING) {
	            // Se colpito mentre cade, entra in EXPLODING (il vaso esplode)
	            if (hazardState != HazardState.EXPLODING) {
	                this.hazardState = HazardState.EXPLODING;
	                this.stateTime = 0;
	                this.velocityY = 0; 
	            }
	            stateTime += dt;
	            if (getExplodingAnimation().isAnimationFinished(stateTime)) {
	                this.active = false;
	            }
	            return; // Blocca il resto della logica
	        }
		}

		stateTime += dt;
		switch (hazardState) {
		case EXPLODING:
			// Usiamo l'animazione di esplosione (caricata nelle Res specifiche)
			if (getExplodingAnimation().isAnimationFinished(stateTime)) {
				this.active = false; // Rimuove l'oggetto solo a fine animazione
			}
			break;
		case FALLING:
			// Fisica di caduta libera
			velocityY -= gravity * dt;
			position.y += velocityY * dt;

			// Controllo impatto al suolo
			if (position.y <= groundY) {
				position.y = groundY;
				velocityY = 0;
				handleImpact();
			}

			// Collisione con il player mentre cade: se non è ancora stato spawnato l'entità interna,
			// esplode immediatamente e infligge danno.
			if (this.hurtbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
				// Let subclass handle it if it wants to (ExplodingBall already handles its own collision)
				if (!handleFallingPlayerCollision(player)) {
					// Default behaviour: damage player and switch to EXPLODING state
					player.takeHit(fallDamage, this.level, position.x, position.y);
					this.hazardState = HazardState.EXPLODING;
					this.stateTime = 0;
					this.velocityY = 0;
				}
			}
			break;
		case IMPACT:
			// Fase di rottura del vaso/uovo
			if (isImpactAnimationFinished()) {
				hazardState = HazardState.SPAWNING_ENTITY;
				stateTime = 0;
			}
			break;
		case SPAWNING_ENTITY:
			// Il momento in cui appare il Drago o lo Snake
			spawnInnerEntity();
			hazardState = HazardState.ACTIVE;
			break;
		case ACTIVE:
			// Logica specifica del nemico (Snake striscia, Dragon sputa fuoco)
			updateActiveLogic(dt, player);
			break;
		case DONE:
			// Logica specifica del nemico (Snake scappa, Dragon sparisce)
			updateActiveLogic(dt, player);
			break;
		}

		// Aggiorna sempre la hurtbox per permettere a Thomas di colpirli al volo
		if (hazardState != HazardState.EXPLODING) {
			updateHurtbox();
		} else {
			hurtbox.set(0, 0, 0, 0); // Disabilita collisioni durante l'esplosione
		}
	}

	// Metodi che ogni sottoclasse implementerà in modo diverso
	protected abstract void handleImpact();

	protected abstract boolean isImpactAnimationFinished();

	protected abstract void spawnInnerEntity();

	protected abstract void updateActiveLogic(float dt, Player player);

	protected abstract void updateHurtbox();

	// Metodo astratto che ogni Hazard implementerà per fornire la sua animazione
	protected abstract Animation<TextureRegion> getExplodingAnimation();

	@Override
	public void draw(SpriteBatch batch) {
	    TextureRegion frame = null;

	    if (hazardState == HazardState.EXPLODING) {
	    	//System.out.println("Disegnando esplosione frame: ["+stateTime+"]" + getExplodingAnimation().getKeyFrameIndex(stateTime));
	        frame = getExplodingAnimation().getKeyFrame(stateTime, false);
	    } else if (hazardState == HazardState.FALLING) {
	        frame = potFrame;
	    } else {
	        // Negli stati IMPACT o ACTIVE, lasciamo che la sottoclasse disegni 
	        // ed USCIAMO dal metodo per non chiamare il drawHelper sotto
	        drawSpecificHazard(batch);
	        return; 
	    }

	    // Se siamo arrivati qui, frame NON è null (è EXPLODING o FALLING)
	    drawHelper(batch, frame);
	}

	protected abstract void drawSpecificHazard(SpriteBatch batch);
}