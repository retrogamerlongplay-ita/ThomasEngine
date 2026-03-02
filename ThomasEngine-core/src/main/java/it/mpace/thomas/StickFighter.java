package it.mpace.thomas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.StickFighterRes;

public class StickFighter extends Enemy {

	// Stati del Boss
	public enum State {
		WAITING, WALKING, ATTACKING_HIGH, ATTACKING_MID, ATTACKING_LOW, HURT_HIGH, HURT_MID, HURT_LOW, DEAD, IDLE
	}
	
	private final float HURT_DURATION = 0.2f; // 200 millisecondi di stordimento

	private State currentState = State.WAITING;

	private float stateTime = 0;
	private int health = 10; // Richiede più colpi dei nemici base
	private float attackRange = 70f; // Il raggio del bastone
	private float attackCooldown = 1.5f;
	private float lastAttackTime = 0;
	private boolean movingRight = false; // Direzione del pattugliamento
	public Rectangle stickHitbox = new Rectangle(0, 0, 0, 0);

	public StickFighter(float x, float y) {
		super(x, y, 10);
		this.speed = GameControlRes.STICK_FIGHTER_SPEED;
		this.hurtbox = new Rectangle(x, y, 32, 64); // Dimensioni standard libGDX
		currentState = State.WAITING;
	}

	public State getState() {
		return this.currentState;
	}

	@Override
	public void hit(Player player) {
	    if (isDying || currentState == State.DEAD) return;

	    hp--;
	    stateTime = 0; // Reset timer per l'animazione di danno

	    // IA REATTIVA: Sceglie l'animazione di danno in base al colpo di Thomas
	    if (player.currentState == Player.State.PUNCHING_CROUCH || player.currentState == Player.State.KICKING_CROUCH) {
	        currentState = State.HURT_LOW; // Assicurati che HURT_LOW sia nell'enum State
	    } else {
	        currentState = State.HURT_HIGH;
	    }

	    if (hp <= 0) {
	        triggerDeath();
	    } else {
	        // Feedback fisico: il boss viene respinto leggermente
	        float pushDir = facingRight ? -15 : 15;
	        position.x += pushDir;
	    }
	}

	public void triggerDeath() {
		this.currentState = State.DEAD;
		this.isDying = true;
		this.stateTime = 0;

		// Spinta arcade: vola verso l'alto e all'indietro rispetto a Thomas
		this.velocityY = 400f;

		// Disabilita la hurtbox per evitare che Thomas lo colpisca ancora mentre "vola"
		this.hurtbox.set(0, 0, 0, 0);

		// Logica opzionale: il Boss urla o lo schermo trema
		System.out.println("BOSS DEFEATED! THE STAIRS ARE OPEN.");
	}

	public void update(float delta, Player player) {
		stateTime += delta;
		float distanceToPlayer = Math.abs(player.position.x - position.x);
		// Reset della hitbox di attacco a ogni frame
		stickHitbox.set(0, 0, 0, 0);
		
		if (currentState == State.HURT_HIGH || currentState == State.HURT_LOW || currentState == State.HURT_MID) {
		    if (stateTime >= HURT_DURATION) {
		        currentState = State.WALKING;
		        stateTime = 0;
		    }
		    // Durante lo stordimento il boss non si muove e non attacca, quindi usciamo
		    // Aggiorna comunque la hurtbox prima di uscire
		    hurtbox.set(position.x - 10, position.y, 20, 64);
		    return; 
		}
		

		switch (currentState) {
		case WAITING:
			// 1. Logica di inversione marcia ai bordi
			if (position.x <= LevelConstants.FIRST_FLOOR_LEFT_STAIR) {
				movingRight = true;
				facingRight = true;
			} else if (position.x >= LevelConstants.FIRST_FLOOR_BOSS_ICON) {
				movingRight = false;
				facingRight = true;
			}

			// 2. Movimento basato sulla direzione attuale
			if (movingRight) {
				position.x += speed * delta;
			} else {
				position.x -= speed * delta;
			}

			// 3. Transizione allo stato WALKING (Combattimento)
			// Se Thomas si avvicina troppo, il boss smette di pattugliare e lo punta
			if (distanceToPlayer < attackRange) {
				currentState = State.WALKING;
			}
			
		case WALKING:
		    float dist = Math.abs(player.position.x - position.x);
		    float idealDist = attackRange + 5;

		    if (dist > idealDist + 10) {
		        // Thomas è lontano: il Boss avanza deciso
		        moveTowardsPlayer(player.position.x, delta);
		    } else if (dist < idealDist - 10) {
		        // Thomas è troppo vicino: il Boss indietreggia per colpire
		        float retreatDir = (player.position.x > player.position.x) ? 1 : -1;
		        position.x += speed * 0.7f * retreatDir * delta;
		        facingRight = (player.position.x > position.x);
		    } else {
		        // Distanza ideale: "oscilla" e tenta l'attacco
		        attemptAttack(player);
		        // Piccola oscillazione per non stare immobile
		        position.x += (MathUtils.sin(stateTime * 5) * 10f) * delta;
		    }
		    break;

		case ATTACKING_HIGH:
			if (stateTime > 0.2f && stateTime < 0.4f) {
				float stickWidth = 35f; // Gittata del bastone
				float stickHeight = 8f;

				if (facingRight) {
					// Il bastone esce a destra del corpo
					stickHitbox.set(position.x + 20, position.y + 47, stickWidth, stickHeight);
				} else {
					// Il bastone esce a sinistra
					stickHitbox.set(position.x - stickWidth + 30, position.y + 47, stickWidth, stickHeight);
				}
			}
			if (stateTime > 0.5f) { // Durata animazione attacco
				currentState = State.WALKING;
				stateTime = 0;
			}
			break;
		case ATTACKING_MID:
			if (stateTime > 0.2f && stateTime < 0.4f) {
				float stickWidth = 35f; // Gittata del bastone
				float stickHeight = 8f;

				if (facingRight) {
					// Il bastone esce a destra del corpo
					stickHitbox.set(position.x + 20, position.y + 35, stickWidth, stickHeight);
				} else {
					// Il bastone esce a sinistra
					stickHitbox.set(position.x - stickWidth + 30, position.y + 35, stickWidth, stickHeight);
				}
			}
			if (stateTime > 0.5f) { // Durata animazione attacco
				currentState = State.WALKING;
				stateTime = 0;
			}
			break;
		case ATTACKING_LOW:
			if (stateTime > 0.2f && stateTime < 0.4f) {
				float stickWidth = 35f; // Gittata del bastone
				float stickHeight = 8f;

				if (facingRight) {
					// Il bastone esce a destra del corpo
					stickHitbox.set(position.x + 20, position.y + 15, stickWidth, stickHeight);
				} else {
					// Il bastone esce a sinistra
					stickHitbox.set(position.x - stickWidth + 30, position.y + 15, stickWidth, stickHeight);
				}
			}
			if (stateTime > 0.5f) { // Durata animazione attacco
				currentState = State.WALKING;
				stateTime = 0;
			}
			break;
		}
		float bh = (currentState == State.ATTACKING_LOW) ? 40 : 64; // Si abbassa se attacca basso
		//hurtbox.set(position.x - (bw/2), position.y, bw, bh);
		float bodyWidth = 30; 
	    hurtbox.set(position.x - (bodyWidth / 4), position.y, bodyWidth+ (bodyWidth / 3), bh);
		//hurtbox.setPosition(position.x, position.y);
	}

	private void moveTowardsPlayer(float playerX, float delta) {
		if (playerX < position.x) {
			position.x -= speed * delta;
			facingRight = false;
		} else {
			position.x += speed * delta;
			facingRight = true;
		}
	}

	private void attemptAttack(Player player) {
		if (stateTime - lastAttackTime > attackCooldown) {
			if (player.currentState == Player.State.CROUCHING) {
				currentState = State.ATTACKING_LOW; // Stato da mappare nelle Res
			} else // IA REATTIVA
			if (player.currentState == Player.State.JUMPING) {
				currentState = State.ATTACKING_HIGH; // Intercetta il salto
			} else {
				// Alterna tra MID e HIGH per rompere la guardia
				currentState = (MathUtils.randomBoolean()) ? State.ATTACKING_MID : State.ATTACKING_HIGH;
			}
			lastAttackTime = stateTime;
			stateTime = 0;

			lastAttackTime = stateTime;
			// Qui andrebbe inserita la logica per danneggiare il giocatore
		}
	}

	public void takeDamage(int damage) {
		health -= damage;
		if (health <= 0)
			currentState = State.DEAD;
	}

	public void draw(SpriteBatch batch) {
		TextureRegion currentFrame;

		// Selezione dell'animazione in base allo stato
		switch (currentState) {
		case ATTACKING_HIGH:
			currentFrame = (TextureRegion) StickFighterRes.attackHighAnim.getKeyFrame(stateTime, false);
			break;
		case ATTACKING_MID:
			currentFrame = (TextureRegion) StickFighterRes.attackMidAnim.getKeyFrame(stateTime, false);
			break;
		case ATTACKING_LOW:
			currentFrame = (TextureRegion) StickFighterRes.attackCrouchAnim.getKeyFrame(stateTime, false);
			break;
		case HURT_HIGH:
			currentFrame = (TextureRegion) StickFighterRes.hurtHigh;
			break;
		case DEAD:
			currentFrame = (TextureRegion) StickFighterRes.dieAnim.getKeyFrame(stateTime, false);
			break;
		case WAITING:
			currentFrame = (TextureRegion) StickFighterRes.walkAnim.getKeyFrame(0, false);
		case WALKING:
		default:
			currentFrame = (TextureRegion) StickFighterRes.walkAnim.getKeyFrame(stateTime, true);
			break;
		}

		// Gestione del flip (orientamento) dello sprite
		// Se lo sprite originale guarda a destra e facingRight è false, flippiamo
		if (!facingRight && !currentFrame.isFlipX()) {
			currentFrame.flip(true, false);
		} else if (facingRight && currentFrame.isFlipX()) {
			currentFrame.flip(true, false);
		}

		batch.draw(currentFrame, position.x, position.y);
	}

	@Override
	public void flee() {
		System.out.println("I DON'T FLEE I AM THE BOSS.");

	}

}
