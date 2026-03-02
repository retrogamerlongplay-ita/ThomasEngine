package it.mpace.thomas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.KnifeThrowerRes;

public class KnifeThrower extends Enemy {

	public enum State {
		WALKING, ATTACKING, RETREATING, HURT, FLEEING
	}

	public State state = State.WALKING;

	private float throwTimer = 0;
	private int knivesThrown = 0;
	private final float STOP_DISTANCE = 140f;
	private final float RETREAT_DISTANCE = 150f;
	private final float RETREAT_SPEED_MULT = 1.3f;

	private int retreatCount = 0;
	private final int MAX_RETREATS = 2; // Dopo 2 ritirate, scappa per sempre
	private boolean fleeingForever = false; // Flag per la fuga definitiva
	private final float HURT_DURATION = 0.15f;
	
	private Knife activeKnife = null;

	public KnifeThrower(float x, float y) {
		super(x, y, 2);
		this.speed = GameControlRes.KNIFE_THROWER_SPEED; // Più lento del Gripper
	}

	// Sovrascriviamo l'update per accettare la lista dei coltelli
	// In ThomasMain chiameremo: ((KnifeThrower)e).update(dt, playerPos, knives);
	public void update(float dt, Player player, Array<Knife> fieldKnives) {
		

		if (isDying) {
			updateDyingPhysics(dt);
			return;
		}

		stateTime += dt;
		float distance = Math.abs(position.x - player.position.x);
		
		 // Gestione stordimento ultra-rapido
        if (state == State.HURT) {
            if (stateTime >= HURT_DURATION) {
                state = State.RETREATING; // Passa subito alla fuga
                stateTime = 0;
            }
            // Aggiorna comunque la hurtbox nel caso Thomas tiri un secondo colpo rapido
            hurtbox.set(position.x - 10, position.y, 20, 65);
            return; 
        }

		switch (state) {
		case WALKING:
			if (distance <= STOP_DISTANCE) {
				state = State.ATTACKING;
				throwTimer = 0;
				knivesThrown = 0;
			} else {
				// Inseguimento
				facingRight = (position.x < player.position.x);
				position.x += (facingRight ? speed : -speed) * dt;
			}
			break;

		case ATTACKING:
			throwTimer += dt;

			// LANCIO ALTO
			if (knivesThrown == 0) {
				// Il coltello parte esattamente quando l'animazione finisce
				// Se la tua animazione dura 0.45s (3 frame da 0.15s), usa 0.45f
				if (KnifeThrowerRes.throwHighAnim.isAnimationFinished(stateTime)) {
					fieldKnives.add(new Knife(position.x, position.y + 45, facingRight));
					knivesThrown++;
					// Non resettiamo lo stateTime qui, lo faremo dopo una piccola pausa
				}
			}
			// PAUSA TRA I LANCI E SECONDO LANCIO
			else if (knivesThrown == 1 && throwTimer > GameControlRes.KNIFE_INTERVAL) {
				// Se entriamo qui per la prima volta dopo la pausa, resettiamo l'animazione
				// bassa
				if (stateTime > 0.5f)
					stateTime = 0;

				if (KnifeThrowerRes.throwLowAnim.isAnimationFinished(stateTime)) {
					fieldKnives.add(new Knife(position.x, position.y + 15, facingRight));
					knivesThrown++;
				}
			}

			if (knivesThrown >= 2 && throwTimer > 1.8f) {
				state = State.RETREATING;
				stateTime = 0;
			}
			break;
		case RETREATING:
			// 1. Movimento di allontanamento
			float retreatDir = (position.x > player.position.x) ? 1 : -1;
			facingRight = (retreatDir > 0);
			position.x += (speed * RETREAT_SPEED_MULT) * retreatDir * dt;

			float currentDistance = Math.abs(position.x - player.position.x);

			// 2. Controllo se deve tornare o scappare
			if (!fleeingForever) {
				// Se è lontano abbastanza, decide cosa fare
				if (currentDistance > RETREAT_DISTANCE) {
					retreatCount++;

					if (retreatCount < MAX_RETREATS) {
						// TORNA ALL'ATTACCO
						state = State.WALKING;
						knivesThrown = 0;
						throwTimer = 0;
						stateTime = 0;
					} else {
						// SCAPPA PER SEMPRE
						fleeingForever = true;
					}
				}
			} else {
				// Logica di uscita definitiva dallo schermo
				if (currentDistance > 300f || position.x < LevelConstants.FIRST_FLOOR_LEFT_STAIR - 150
						|| position.x > LevelConstants.FIRST_FLOOR_RIGHT_START + 150) {
					active = false;
				}
			}
			break;
		}

		// Aggiorna Hurtbox
		hurtbox.set(position.x - 10, position.y, 20, 65);
	}

	// Metodo richiesto dalla superclasse Enemy (vuoto o chiama l'altro update)
	@Override
	public void update(float dt, Player player) {
		// Questo verrà usato se non passiamo i coltelli,
		// ma noi useremo l'overload sopra in ThomasMain
	}
	
	private void throwKnife(Array<Knife> worldKnives) {
	    // Crea il coltello
	    activeKnife = new Knife(position.x, position.y + 30, facingRight);
	    // Aggiungilo alla lista globale del LevelScreen per le collisioni
	    worldKnives.add(activeKnife);
	}

	@Override
	public void hit(Player p) {
		super.hit(p); // Sottrae HP
		if (hp > 0) {
			this.state = State.HURT;
			// Piccola spinta all'indietro quando viene colpito
			position.x += (facingRight ? -15 : 15);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		TextureRegion frame;

		if (isDying) {
			frame = KnifeThrowerRes.dieAnim.getKeyFrame(stateTime);
		} else if (state == State.HURT) {
			// Usa i frame hurt che hai già caricato in KnifeThrowerRes
			frame = KnifeThrowerRes.hurtHigh;
		} else {
			switch (state) {
			case ATTACKING:
				// Se non ha ancora lanciato, usa l'animazione ALTA, altrimenti BASSA
				if (knivesThrown == 0) {
					frame = KnifeThrowerRes.throwHighAnim.getKeyFrame(stateTime);
				} else {
					frame = KnifeThrowerRes.throwLowAnim.getKeyFrame(stateTime);
				}
				break;
			case RETREATING:
			case WALKING:
			default:
				frame = KnifeThrowerRes.walkAnim.getKeyFrame(stateTime);
				break;
			}
		}

		// Usiamo l'helper della superclasse per il flip!
		drawHelper(batch, frame);
	}

	@Override
	public void flee() {
		this.state=State.RETREATING;
		
	}
	
	
}
