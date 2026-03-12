package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.StickFighterRes;
import it.mpace.thomas.screen.LevelScreen;

public class StickFighter extends Enemy {

	private final float HURT_DURATION = 0.2f; // 200 millisecondi di stordimento

	private EnemyState currentState = EnemyState.WAITING;

	private float stateTime = 0;
	private int hp = 100; // Richiede più colpi dei nemici base
	private float attackRange = 70f; // Il raggio del bastone
	private float attackCooldown = 0.6f;
	private float lastAttackTime = -1;
	private boolean movingRight = false; // Direzione del pattugliamento
	public Rectangle stickHitbox = new Rectangle(0, 0, 0, 0);

	public StickFighter(float x, float y) {
		super(x, y, 10);
		this.speed = LevelConstants.STICK_FIGHTER_SPEED;
		this.hurtbox = new Rectangle(x, y, 32, 64); // Dimensioni standard libGDX
		currentState = EnemyState.WAITING;
	}

	public EnemyState getState() {
		return this.currentState;
	}

	@Override
	public void hit(Player player, LevelScreen level) {
		super.hit(player, level); // Applica il danno e crea l'effetto visivo
	    if (isDying || currentState == EnemyState.DEAD) return;

	    hp=hp-10;
	    stateTime = 0; // Reset timer per l'animazione di danno
	    
	    // Se è vicino alle scale, "accorciamo" il cooldown dell'ultimo attacco 
	    // per farlo reagire subito dopo il colpo ricevuto
	    float distanceToWall = position.x - (LevelConstants.FIRST_FLOOR_LEFT_STAIR + 20);
	    if (distanceToWall < 50f) {
	        lastAttackTime -= 1.0f; // Accelera il prossimo attacco "rubando" tempo al cooldown
	    }

	    // IA REATTIVA: Sceglie l'animazione di danno in base al colpo di Thomas
	    if (player.currentState == Player.State.PUNCHING_CROUCH || player.currentState == Player.State.KICKING_CROUCH) {
	        currentState = EnemyState.HURT_LOW; // Assicurati che HURT_LOW sia nell'enum State
	    } else {
	        currentState = EnemyState.HURT_HIGH;
	    }

	    if (hp <= 0) {
	        triggerDeath();
	    } else {
	        // Feedback fisico: il boss viene respinto leggermente
	        float pushDir = facingRight ? -15 : 15;
	        position.x += pushDir;
	        if (position.x < LevelConstants.FIRST_FLOOR_LEFT_STAIR + 10) {
	            position.x = LevelConstants.FIRST_FLOOR_LEFT_STAIR + 10;
	        }
	    }
	}
	
	public Rectangle getHitBox() {
		return this.stickHitbox;
	}

	public void triggerDeath() {
		this.currentState = EnemyState.DEAD;
		this.isDying = true;
		this.stateTime = 0;

		// Spinta arcade: vola verso l'alto e all'indietro rispetto a Thomas
		this.velocityY = 300f;

		// Disabilita la hurtbox per evitare che Thomas lo colpisca ancora mentre "vola"
		this.hurtbox.set(0, 0, 0, 0);

		// Logica opzionale: il Boss urla o lo schermo trema
		System.out.println("BOSS DEFEATED! THE STAIRS ARE OPEN.");
	}
	

	public void update(float delta, Player player) {
		// 1. SE È MORTO: Applica solo la fisica di volo e ESCI
	    if (currentState == EnemyState.DEAD || isDying) {
	        stateTime += delta;
	        
	        // Parabola arcade: vola via dal lato opposto a Thomas
	        float direction = facingRight ? -1 : 1; 
	        position.x += direction * 60f * delta; // Spinta orizzontale
	        
	        position.y += velocityY * delta;      // Salita/Discesa
	        velocityY -= 1000f * delta;           // Gravità forte (arcade style)

	        // Rimuovi il boss se cade sotto lo schermo
	        if (position.y < 400) { // Un valore sotto il pavimento 510
	            active = false;
	        }
	        return; // IMPORTANTE: non eseguire il resto della IA
	    }
		stateTime += delta;
		float distanceToPlayer = Math.abs(player.position.x - position.x);
		// Reset della hitbox di attacco a ogni frame
		stickHitbox.set(0, 0, 0, 0);
		
		if (currentState == EnemyState.HURT_HIGH || currentState == EnemyState.HURT_LOW || currentState == EnemyState.HURT_MID) {
		    if (stateTime >= HURT_DURATION) {
		        currentState = EnemyState.WALKING;
		        stateTime = 0;
		    }
		    // Durante lo stordimento il boss non si muove e non attacca, quindi usciamo
		    // Aggiorna comunque la hurtbox prima di uscire
		    hurtbox.set(position.x - 10, position.y, 20, 64);
		    return; 
		}
		
		// Forza il Boss a restare a destra delle scale
		if (position.x < LevelConstants.FIRST_FLOOR_LEFT_STAIR + 20) { 
		    position.x = LevelConstants.FIRST_FLOOR_LEFT_STAIR + 20;
		    
		    // Se stava camminando a sinistra (pattugliamento), lo costringiamo a girarsi
		    if (currentState == EnemyState.WAITING) {
		        movingRight = true;
		        facingRight = true;
		    }
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
				currentState = EnemyState.WALKING;
			}
			
		case WALKING:
		    float dist = Math.abs(player.position.x - position.x);
		    float idealDist = attackRange + 5;

		    if (dist > idealDist + 10) {
		        // Thomas è lontano: il Boss avanza deciso
		        moveTowardsPlayer(player.position.x, delta);
		    } else if (dist < idealDist - 10) {
		        // Thomas è troppo vicino: il Boss indietreggia per colpire
		        //float retreatDir = (player.position.x > player.position.x) ? 1 : -1;
		    	float retreatDir = (position.x > player.position.x) ? 1 : -1;
		        //position.x += speed * 0.7f * retreatDir * delta;
		    	position.x += speed * 1.2f * retreatDir * delta;
		        facingRight = (player.position.x > position.x);
		    } else {
		        // Distanza ideale: "oscilla" e tenta l'attacco
		        attemptAttack(player);
		        // Piccola oscillazione per non stare immobile
		        position.x += (MathUtils.sin(stateTime * 5) * 10f) * delta;
		    }
		 // ATTENZIONE: Chiama sempre attemptAttack se sei a distanza di tiro!
		    if (distanceToPlayer < attackRange + 20) {
		        attemptAttack(player);
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
				currentState = EnemyState.WALKING;
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
				currentState = EnemyState.WALKING;
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
				currentState = EnemyState.WALKING;
				stateTime = 0;
			}
			break;
		}
		float bh = (currentState == EnemyState.ATTACKING_LOW) ? 40 : 64; // Si abbassa se attacca basso
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
		//System.out.println("Attempting attack. StateTime: " + stateTime + ", LastAttackTime: " + lastAttackTime);
		
		// Calcoliamo quanto il boss è vicino al limite sinistro (le scale)
	    float distanceToWall = position.x - (LevelConstants.FIRST_FLOOR_LEFT_STAIR + 20);
	    
	    // Se è a meno di 40 pixel dal muro, dimezziamo il tempo di attesa (attacca il doppio più veloce)
	    float dynamicCooldown = (distanceToWall < 40f) ? attackCooldown / 2f : attackCooldown;

	    if (stateTime - lastAttackTime > dynamicCooldown) {
	        // IA REATTIVA (La tua logica esistente)
	        if (player.currentState == Player.State.CROUCHING) {
	            currentState = EnemyState.ATTACKING_LOW;
	        } else if (player.currentState == Player.State.JUMPING) {
	            currentState = EnemyState.ATTACKING_HIGH;
	        } else {
	            currentState = (MathUtils.randomBoolean()) ? EnemyState.ATTACKING_MID : EnemyState.ATTACKING_HIGH;
	        }
	        
	        // Se è all'angolo, potremmo anche aumentare la probabilità di un attacco MID che è più difficile da schivare
	        if (distanceToWall < 30f && MathUtils.random() > 0.7f) {
	             currentState = EnemyState.ATTACKING_MID; 
	        }

	        lastAttackTime = Math.min(dynamicCooldown/2, stateTime);
	        stateTime = 0;
	    }
	}

	
	public int getHp() {
    	return this.hp;
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
