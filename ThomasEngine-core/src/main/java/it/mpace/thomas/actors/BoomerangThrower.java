package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.BoomerangThrowerRes;
import it.mpace.thomas.screen.LevelScreen;
import it.mpace.thomas.sprite.Boomerang;

public class BoomerangThrower extends Enemy {

	private float attackTimer = 0;
	private final float ATTACK_COOLDOWN = 3.0f;
	private int boomerangsThrown = 0;
	private EnemyState currentState = EnemyState.WAITING;
	
	//private boolean movingRight = false; // Direzione del pattugliamento
	private float idealDistance = 100f;  // La distanza che il boss vuole mantenere

	public Array<Boomerang> activeBoomerangs = new Array<>();
	
	private Rectangle hitbox = new Rectangle(0, 0, 0, 0);

	public BoomerangThrower(float x, float y) {
		super(x, y, 20); // 2 colpi per sconfiggerlo
		this.speed = 40f;
		this.hurtbox = new Rectangle(x, y, 25, 65);
		this.facingRight = false; // Di default guarda a sinistra
		//this.hp=100;
	}

	public void update(float dt, Player player, Array<Boomerang> levelBoomerangs) {
		if (isDying) {
			updateDyingPhysics(dt);
			return;
		}

		stateTime += dt;
		attackTimer += dt;
		
		 float dist = Math.abs(position.x - player.position.x);
		 facingRight = (position.x < player.position.x);
		// 1. Calcola la distanza dalle scale (SECOND_FLOOR_RIGHT_STAIR è circa 1669)
		 float distanceToWall = Math.abs(position.x - (LevelConstants.SECOND_FLOOR_RIGHT_STAIR - 5)); 
		 

		 // 2. Cooldown dinamico: se ha le spalle al muro, attacca più velocemente!
		 float dynamicCooldown = (distanceToWall < 50f) ? ATTACK_COOLDOWN / 2f : ATTACK_COOLDOWN;
		 
		 

		 boolean trappedAtWall = position.x >= LevelConstants.SECOND_FLOOR_RIGHT_STAIR - 5; // Se è contro il muro a destra

		// 3. Trigger Attacco con IA predittiva
		 if (attackTimer > dynamicCooldown && currentState == EnemyState.WALKING) {
		     stateTime = 0;
		     boomerangsThrown = 0;
		     
		     // IA PREDITTIVA: Se Thomas salta spesso, lancia alto. Se è accovacciato, lancia basso.
		     if (player.currentState == Player.State.JUMPING || player.currentState == Player.State.KICKING_JUMP) {
		         currentState = EnemyState.ATTACKING_HIGH;
		     } else if (player.currentState == Player.State.CROUCHING) {
		         currentState = EnemyState.ATTACKING_LOW;
		     } else {
		         // Altrimenti scelta casuale come prima
		         currentState = MathUtils.randomBoolean() ? EnemyState.ATTACKING_HIGH : EnemyState.ATTACKING_LOW;
		     }
		 }
		 
		 switch (currentState) {
	        case WAITING:
	        case WALKING:
	            // IA DINAMICA: Cerca di mantenere la distanza ideale
	            if (dist > idealDistance + 20 || trappedAtWall) {
	                // Troppo lontano: avanza verso il player
	                float dir = facingRight ? 1 : -1;
	                position.x += speed * dir * dt;
	            } else if (dist < idealDistance - 20 && !trappedAtWall) {
	                // Troppo vicino: indietreggia (scappa) per poter lanciare
	                float dir = facingRight ? -1 : 1;
	                position.x += (speed * 1.2f) * dir * dt; // Indietreggia un po' più veloce
	            } else {
	                // Distanza perfetta: "oscilla" sul posto in attesa del cooldown
	                position.x += (MathUtils.sin(stateTime * 4) * 15f) * dt;
	                
	                // TRIGGER ATTACCO
	                if (attackTimer > ATTACK_COOLDOWN) {
	                    stateTime = 0;
	                    boomerangsThrown = 0;
	                    currentState = MathUtils.randomBoolean() ? EnemyState.ATTACKING_HIGH : EnemyState.ATTACKING_LOW;
	                }
	            }
	            if (trappedAtWall && dist < 60f && currentState == EnemyState.WALKING) {
	                attackTimer = ATTACK_COOLDOWN; // Forza il trigger al prossimo frame
	            }
	            break;
	        case ATTACKING_HIGH:
	        case ATTACKING_LOW:
	            // Durante il lancio sta fermo
	            if (boomerangsThrown == 0 && stateTime > 0.3f) {
	                float launchY = (currentState == EnemyState.ATTACKING_HIGH) ? position.y + 45 : position.y + 15;
	                levelBoomerangs.add(new Boomerang(position.x, launchY, facingRight));
	                boomerangsThrown = 1;
	                AudioRes.playSound(AudioRes.punchSound);
	            }

	            // Dopo il lancio, torna a muoversi immediatamente
	            if (boomerangsThrown >= 1 && stateTime > 0.8f) {
	                currentState = EnemyState.WALKING;
	                attackTimer = 0;
	            }
	            break;
		 }
		 
		 if (position.x > LevelConstants.SECOND_FLOOR_RIGHT_STAIR) { // Usa il valore di SECOND_FLOOR_RIGHT_STAIR o simile
			    position.x = LevelConstants.SECOND_FLOOR_RIGHT_STAIR-1;
			    // Se sta cercando di scappare oltre il muro, lo forziamo a guardare Thomas
			    facingRight = false; 
			}
		hurtbox.setPosition(position.x - 12, position.y);
	}
	
	

	@Override
	public void setState(EnemyState newState) {
		this.currentState = newState;
		
	}
	
	// Ensure boss correctly transitions to DEAD state when hit to 0 HP
	@Override
	public void hit(Player player, LevelScreen level) {
		super.hit(player, level);
		if (hp <= 0) {
			triggerDeath();
		}
	}

	public void triggerDeath() {
		this.currentState = EnemyState.DEAD;
		this.isDying = true;
		this.stateTime = 0;
		this.velocityY = 400f;
		this.hurtbox.set(0, 0, 0, 0);
		this.active = false;
		System.out.println("BOOMERANG THROWER DEFEATED!");
		
	}

	@Override
	public void draw(SpriteBatch batch) {
		TextureRegion frame;
		if (isDying) {
			frame = BoomerangThrowerRes.dieAnim.getKeyFrame(stateTime);
		} else if (currentState == EnemyState.ATTACKING_HIGH) {
			frame = BoomerangThrowerRes.throwHighAnim.getKeyFrame(stateTime);
		} else if (currentState == EnemyState.ATTACKING_LOW) {
			frame = BoomerangThrowerRes.throwLowAnim.getKeyFrame(stateTime);
		}else if (currentState == EnemyState.ATTACKING) {
			frame = BoomerangThrowerRes.walkAnim.getKeyFrame(stateTime);
		}else {
			frame = BoomerangThrowerRes.walkAnim.getKeyFrame(stateTime, true);
		}
		drawHelper(batch, frame);
	}

	// Metodi obbligatori...
	@Override
	public void update(float dt, Player player) {
	    this.update(dt, player, activeBoomerangs); // Passa la lista dei boomerang del livello per aggiungere i nuovi
	}

	@Override
	public void flee() {
		this.active = false;
	}

	@Override
	public Rectangle getHitBox() {
		return this.hitbox;
	}

	@Override
	public EnemyState getState() {
		return currentState;
	}

	@Override
	public int getHitScoreValue() {
		return 50;
	}

	@Override
	public int getDieScoreValue() {
		return 2000;
	}
}
