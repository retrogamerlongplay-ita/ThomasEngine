package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.DragonRes;
import it.mpace.thomas.screen.LevelScreen;

public class Dragon extends FallingHazard {

	private float flameTimer = 0;
	private final float FLAME_DURATION = 1.2f; // Durata fiammata
	private boolean isSpitting = false;
	public Rectangle flameHitbox = new Rectangle(0, 0, 0, 0);

	public Dragon(float x, float y, float groundY, LevelScreen level) {
		super(x, y, groundY, DragonRes.potFallingFrame, level);
		this.hp = 1;
	}

	@Override
	public int getHitScoreValue() {
		return this.getDieScoreValue();
	}

	@Override
	public int getDieScoreValue() {
		switch (this.hazardState) {
		case FALLING:
			return 200;
		case ACTIVE:
			return 500;
		default:
			return 200;
		}

	}
	
	@Override
	protected Animation<TextureRegion> getExplodingAnimation() {
	    return DragonRes.explodingAnim;
	}

	@Override
	public void setState(EnemyState newState) {
		// empty only for enemies, hazards don't have states like this

	}
	
    @Override
    public void hit(Player p, LevelScreen level) {
        super.hit(p, level); // Sottrae HP, imposta isDying = true, genera score
        this.stateTime = 0;  // Resetta il tempo per far partire l'animazione di esplosione
    }
	
	

	@Override
	public void update(float dt, Player player) {
		super.update(dt, player);
		if (this.isDying && hazardState != HazardState.EXPLODING) {
	        isSpitting = false; // Smette subito di sputare fuoco
	        flameHitbox.set(0, 0, 0, 0); // Sparisce la hitbox di danno
	        
	        // Se l'animazione di sparizione è finita, rimuovi l'oggetto
	        if (DragonRes.dragonDisappearAnim.isAnimationFinished(stateTime)) {
	            this.active = false;
	        }
	     //   return; // ESCI: non processare altre logiche se è in fase di morte
		}
		
	}

	@Override
	protected void handleImpact() {
		this.hazardState = HazardState.IMPACT;
		this.stateTime = 0;
	}

	@Override
	protected boolean isImpactAnimationFinished() {
		return DragonRes.dragonApperAnim.isAnimationFinished(stateTime);
	}

	@Override
	protected void spawnInnerEntity() {
		this.stateTime = 0;
		this.flameTimer = 0;
		// Il drago decide la direzione fissa basandosi su Thomas al momento della
		// nascita
		AudioRes.playSound(AudioRes.potCrushSound);
		Player player = level.player;
		this.facingRight = (this.position.x < player.position.x);
	}

	@Override
	protected void updateActiveLogic(float dt, Player player) {
		if (this.isDying) {
			isSpitting = false;
			flameHitbox.set(0, 0, 0, 0); // Spegne il fuoco istantaneamente
			return; // ESCI: non processare timer o fiammate
		}

		flameTimer += dt;

		// 2. LOGICA DI ATTACCO
		if (flameTimer > 0.4f && flameTimer < FLAME_DURATION) {
			isSpitting = true;
			float flameWidth = 45f;
			float flameHeight = 12f;
			final int flameAlt=40;

			// La hitbox della fiamma segue la direzione bloccata al punto 1
			if (facingRight) {
				flameHitbox.set(position.x + 15, position.y + flameAlt, flameWidth, flameHeight);
			} else {
				flameHitbox.set(position.x - flameWidth - 15, position.y + flameAlt, flameWidth, flameHeight);
			}

			if (flameHitbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
				player.takeHit(8f, this.level, flameHitbox.x, flameHitbox.y + 5);
			}
		} else if (flameTimer >= FLAME_DURATION) {
			this.active = false;
		}
	}

	@Override
	protected void updateHurtbox() {
		if (hazardState == HazardState.FALLING) {
			hurtbox.set(position.x - 8, position.y, 16, 16);
		} else {
			// HURTBOX SOLO SULLA FACCIA (Punto debole)
			float faceW = 12;
			float faceH = 12;
			// La faccia si trova in alto e in avanti rispetto alla posizione base
			// float offsetX = facingRight ? 18 : -18 - faceW;
			float offsetY = 40;

			hurtbox.set(position.x /* + offsetX */, position.y + offsetY, faceW, faceH);
		}
	}
	
	@Override
	protected boolean handleFallingPlayerCollision(Player player) {
        if (this.hurtbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
            // Damage and immediate removal/explosion (no spawn of inner entity)
            player.takeHit(18f, this.level, position.x, position.y);
            this.isDying = true;
            this.hazardState = HazardState.EXPLODING;
            this.stateTime = 0;
            this.flameHitbox.set(0,0,0,0);
            this.velocityY = 0;
        }
        return true;
    }

	@Override
	protected void drawSpecificHazard(SpriteBatch batch) {
		TextureRegion frame;

		 if (isDying) {
		        // PRIORITÀ 1: Se sta sparendo, usa l'animazione disappear
		        frame = DragonRes.dragonDisappearAnim.getKeyFrame(stateTime, false);
		    } else if (hazardState == HazardState.IMPACT) {
		        // PRIORITÀ 2: Se sta nascendo
		        frame = DragonRes.dragonApperAnim.getKeyFrame(stateTime, false);
		    } else if (isSpitting) {
		        // PRIORITÀ 3: Se sputa fuoco
		        frame = DragonRes.dragonFlameFrame;
		    } else {
		        // DEFAULT: Idle
		        frame = DragonRes.idleFrame;
		    }
		// CALCOLO ORIGINE:
		// Invece di usare position.x come angolo sinistro,
		// lo usiamo come CENTRO del corpo del drago.
		float width = frame.getRegionWidth();
		float height = frame.getRegionHeight();

		// Se il drago guarda a destra, la fiammata si allunga a destra.
		// Se guarda a sinistra, dobbiamo compensare la larghezza per non far traslare
		// il corpo.
		// float drawX = position.x - (width / 2);

		float bodyAnchor = 10;

		if (facingRight) {
			// Disegno normale: il corpo sta a position.x, la fiamma si allunga a DESTRA
			batch.draw(frame, position.x - bodyAnchor, position.y, width, height);
		} else {
			// Disegno flippato: partiamo dallo STESSO punto, ma invertiamo la larghezza
			// La fiamma si allungherà a SINISTRA, ma il corpo resterà su position.x
			batch.draw(frame, position.x + bodyAnchor, position.y, -width, height);
		}
	}

	@Override
	public void flee() {
		this.active = false;
	}

	@Override
	public Rectangle getHitBox() {
		return flameHitbox;
	}

	@Override
	public EnemyState getState() {
		return EnemyState.ATTACKING;
	}
}