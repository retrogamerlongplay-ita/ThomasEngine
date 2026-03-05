package it.mpace.thomas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.PlayerRes;

public class Player {
	// Stati del giocatore
	public enum State {
		IDLE, WALKING, PUNCHING, KICKING, CROUCHING, JUMPING, GRABBED, DEAD, PUNCHING_CROUCH, KICKING_CROUCH,
		KICKING_JUMP, PUNCHING_JUMP,  CLIMBING_STAIRS
	}

	private final float HURT_DURATION = 0.1f; // Durata del "dolore"

	public State currentState = State.IDLE;
	public Vector2 position;
	public int struggleCount = 0;
	private final int STRUGGLE_THRESHOLD = 10; // Quante volte premere per liberarsi

	private float speed = GameControlRes.PLAYER_SPEED;
	public boolean facingRight = false;
	private float stateTime;

	private float velocityY = 0;
	private final float GRAVITY = 800f;

	private float shakeOffset = 0; // Per l'effetto vibrazione

	public Rectangle hitbox;
	Rectangle hurtbox;
	private float hurtTimer = 0;
	private LevelScreen screen = null;

	// Nella classe Player
	public boolean autoWalking = false;

	public Player(float x, float y, LevelScreen s) {
		position = new Vector2(x, y);
		hitbox = new Rectangle(0, 0, 0, 0); // Inizialmente vuota
		hurtbox = new Rectangle(0, 0, 32, 70); // Dimensioni Thomas
		stateTime = 0f;
		this.screen = s;
	}

	public void takeHit(float damage) {
		if (currentState == State.DEAD)
			return;

		GameControlRes.decrementEnergy(damage);
		this.currentState = State.GRABBED; // Usiamo l'animazione hurt
		this.stateTime = 0;
		this.hurtTimer = HURT_DURATION;
		AudioRes.playSound(AudioRes.playerHurt);

	}

	public void triggerDeath() {
		this.currentState = State.DEAD;
		AudioRes.playSound(AudioRes.dieSound);
		this.stateTime = 0;
		this.velocityY = 250f; // Spinta verso l'alto
		this.hitbox.set(0, 0, 0, 0); // Disabilita attacchi
	}

	public Vector2 getPosition() {
		return this.position;
	}

	// Metodo per resettare il giocatore dopo una vita persa
	public void respawn(float x, float y) {
		this.position.set(x, y);
		this.currentState = State.IDLE;
		this.struggleCount = 0;
		this.stateTime = 0;
		this.hitbox.set(0, 0, 0, 0);
	}

	public void update(float deltaTime) {
		stateTime += deltaTime;
		float bodyWidth = 20;

		// 1. GESTIONE HURTBOX DINAMICA
		float currentHurtboxHeight = (currentState == State.CROUCHING) ? 40 : 65;
		hurtbox.set(position.x - (bodyWidth / 2), position.y, bodyWidth, currentHurtboxHeight);

		// 2. GESTIONE STORDIMENTO (HURT)
		if (hurtTimer > 0) {
			hurtTimer -= deltaTime;
			if (hurtTimer <= 0 && currentState == State.GRABBED) {
				currentState = State.IDLE;
			}
			return;
		}

		// 3. AUTO-WALKING (TRANSIZIONI LIVELLO)
		if (autoWalking) {
			if (position.x > LevelConstants.FIRST_FLOOR_LEFT_STAIR) {
				currentState = State.WALKING;
				facingRight = false;
				position.x -= speed * 0.5f * deltaTime;
			} else {
				currentState = State.CLIMBING_STAIRS;
				position.x -= speed * 0.3f * deltaTime;
				position.y += speed * 0.4f * deltaTime;
			}
			return;
		}

		// 4. FISICA DI CADUTA E GRAVITÀ
		if ((position.y > 510 || velocityY > 0) && currentState != State.DEAD) {
			position.y += velocityY * deltaTime;
			velocityY -= GRAVITY * deltaTime;
		}

		// 5. LOGICA SPECIFICA CALCIO VOLANTE (DIAGONALE E STATICO)
		if (currentState == State.KICKING_JUMP) {
			// Permette il movimento orizzontale in aria durante il calcio
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				position.x -= speed * deltaTime;
				facingRight = false;
			} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				position.x += speed * deltaTime;
				facingRight = true;
			}

			// Hitbox persistente del calcio volante
			float hw = 22;
			hitbox.set(facingRight ? position.x : position.x - hw, position.y + 35, hw, 10);

			// Atterraggio
			if (position.y <= 510) {
				position.y = 510;
				velocityY = 0;
				currentState = State.IDLE;
				hitbox.set(0, 0, 0, 0);
			}
			return; // BLOCCA il resto dell'update (evita walking anim in aria)
		}
		
		if (currentState == State.PUNCHING_JUMP) {
		    // Movimento diagonale
		    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { position.x -= speed * deltaTime; facingRight = false; }
		    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { position.x += speed * deltaTime; facingRight = true; }

		    // Hitbox del pugno volante (leggermente più corta del calcio)
		    float hw = 18;
		    hitbox.set(facingRight ? position.x : position.x - hw, position.y + 40, hw, 8);

		    // Atterraggio
		    if (position.y <= 510) {
		        position.y = 510; velocityY = 0;
		        currentState = State.IDLE;
		        hitbox.set(0, 0, 0, 0);
		    }
		    return; // Protegge lo stato
		}

		// 6. MOVIMENTO ORIZZONTALE TERRA/ARIA (Solo se non attacca o è accovacciato)
		if (!isAttacking() && currentState != State.DEAD && currentState != State.GRABBED
				&& currentState != State.CROUCHING) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				position.x -= speed * deltaTime;
				facingRight = false;
			} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				position.x += speed * deltaTime;
				facingRight = true;
			}
		}

		// 7. GESTIONE ATTERRAGGIO (Salto normale o caduta)
		if (position.y <= 510 && currentState != State.DEAD) {
			position.y = 510;
			velocityY = 0;
			if (currentState == State.JUMPING)
				currentState = State.IDLE;
		}

		// 8. AGGIORNAMENTO STATO IDLE/WALKING (Solo se a terra e non occupato)
		if (position.y <= 510 && !isAttacking() && currentState != State.DEAD && currentState != State.GRABBED
				&& currentState != State.CROUCHING) {
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				currentState = State.WALKING;
			} else {
				currentState = State.IDLE;
			}
		}

		// 9. LIMITI LIVELLO E BOSS
		if (position.x > LevelConstants.FIRST_FLOOR_RIGHT)
			position.x = LevelConstants.FIRST_FLOOR_RIGHT;

		if (this.screen.getBoss().isActive()) {
			if (this.position.x < this.screen.getBoss().position.x + 10) {
				this.position.x = this.screen.getBoss().position.x + 10;
			}
		} else if (this.position.x < LevelConstants.FIRST_FLOOR_LEFT) {
			this.screen.startLevelTransition(deltaTime);
		}

		// 10. MORTE
		if (currentState == State.DEAD) {
			float direction = facingRight ? -1 : 1;
			position.x += direction * 50f * deltaTime;
			position.y += velocityY * deltaTime;
			velocityY -= GRAVITY * deltaTime;
			return;
		}

		// 11. LIBERAZIONE (GRABBED)
		if (currentState == State.GRABBED) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
				struggleCount++;
				shakeOffset = (shakeOffset == 0) ? 2 : -2;
			} else {
				shakeOffset *= 0.9f;
			}
			hitbox.set(0, 0, 0, 0);
			return;
		}

		// 12. GESTIONE ANIMAZIONI ATTACCO (Pugno/Calcio a terra)
		updateAttackStates();
	}

	// Metodo helper per pulire gli stati di attacco
	private void updateAttackStates() {
		if (currentState == State.KICKING_JUMP) {
			// Il calcio volante non finisce con l'animazione, ma con l'atterraggio
			// Quindi impostiamo solo la hitbox fissa
			float hw = 26;
			float hh = 10;
			hitbox.set(facingRight ? position.x : position.x - hw, position.y + 35, hw, hh);
		} else if (currentState == State.PUNCHING) {
			handleAttackAnimation(PlayerRes.punchAnim, State.IDLE, 40, 18, 8);
		} else if (currentState == State.KICKING) {
			handleAttackAnimation(PlayerRes.kickAnim, State.IDLE, 42, 22, 10);
		} else if (currentState == State.PUNCHING_CROUCH) {
			handleAttackAnimation(PlayerRes.punchCrouchAnim, State.CROUCHING, 27, 18, 8);
		} else if (currentState == State.KICKING_CROUCH) {
			handleAttackAnimation(PlayerRes.kickCrouchAnim, State.CROUCHING, 0, 26, 10);
		} else {
			hitbox.set(0, 0, 0, 0);
		}
	}

	private void handleAttackAnimation(com.badlogic.gdx.graphics.g2d.Animation anim, State nextState, float yOffset,
			float hw, float hh) {
		if (anim.isAnimationFinished(stateTime)) {
			currentState = nextState;
			hitbox.set(0, 0, 0, 0);
		} else if (anim.getKeyFrameIndex(stateTime) == 1) {
			hitbox.set(facingRight ? position.x : position.x - hw, position.y + yOffset, hw, hh);
		} else {
			hitbox.set(0, 0, 0, 0);
		}
	}

	public void handleJump() {
		// Può saltare solo se è IDLE o WALKING e si trova sul terreno
		if (!isBusy() && position.y <= 510) {
			currentState = State.JUMPING;
			velocityY = 280f; // Forza del salto (regola questo valore per l'altezza)
			stateTime = 0;
			// AudioRes.playSound(AudioRes.jumpSound); // Se hai un suono per il salto
		}
	}

	public void handleCrouch() {
		if (currentState == State.IDLE || currentState == State.WALKING) {
			currentState = State.CROUCHING;
		}
	}

	public void handleStandUp() {
		if (currentState == State.CROUCHING) {
			currentState = State.IDLE;
		}
	}

	private boolean isBusy() {
		return currentState == State.PUNCHING || currentState == State.KICKING || currentState == State.PUNCHING_CROUCH
				|| currentState == State.KICKING_CROUCH || currentState == State.DEAD || currentState == State.GRABBED;
	}

	public void handlePunch() {
	    if (isBusy() && currentState != State.JUMPING) return; 

	    stateTime = 0;
	    if (currentState == State.JUMPING) {
	        currentState = State.PUNCHING_JUMP;
	    } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
	        currentState = State.PUNCHING_CROUCH;
	    } else {
	        currentState = State.PUNCHING;
	    }
	    AudioRes.playSound(AudioRes.punchSound);
	}

	public void handleKick() {
		// 1. Blocco se già occupato
//		if (isBusy())
//			return;
//
//		stateTime = 0;
//		// 2. Controllo se è calcio alto o basso
//		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//			currentState = State.KICKING_CROUCH;
//		} else {
//			currentState = State.KICKING;
//		}
//		AudioRes.playSound(AudioRes.kickSound);
		if (isBusy() && currentState != State.JUMPING)
			return; // Blocca solo se non è in salto

		stateTime = 0;

		if (currentState == State.JUMPING) {
			// ATTACCO IN VOLO
			currentState = State.KICKING_JUMP;
		} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			// ATTACCO BASSO
			currentState = State.KICKING_CROUCH;
		} else {
			// ATTACCO NORMALE
			currentState = State.KICKING;
		}
		AudioRes.playSound(AudioRes.kickSound);
	}

	public void updateAnimationOnly(float deltaTime) {
		// 1. Fai avanzare il tempo dell'animazione corrente
		stateTime += deltaTime;

		// 2. Mantieni aggiornata la Hurtbox (fondamentale per il debug visivo e
		// coerenza)
		float bodyWidth = 20;
		float currentHurtboxHeight = (currentState == State.CROUCHING) ? 40 : 65;
		hurtbox.set(position.x - (bodyWidth / 2), position.y, bodyWidth, currentHurtboxHeight);

		// 3. Applica la gravità se Thomas non è a terra (es. se l'intro prevede un
		// salto)
		if (position.y > 510 || velocityY > 0) {
			position.y += velocityY * deltaTime;
			velocityY -= GRAVITY * deltaTime;

			if (position.y <= 510) {
				position.y = 510;
				velocityY = 0;
				if (currentState == State.JUMPING)
					currentState = State.IDLE;
			}
		}

		// 4. Reset della Hitbox di attacco
		// Durante l'intro Thomas non deve poter colpire nulla accidentalmente
		hitbox.set(0, 0, 0, 0);
	}

	public void draw(SpriteBatch batch) {
		TextureRegion keyFrame;
		// Applichiamo lo shakeOffset solo alla X nel momento del disegno
		// Scegliamo il frame in base allo stato
		switch (currentState) {
		case GRABBED:
			keyFrame = PlayerRes.hurtAnim.getKeyFrame(stateTime);
			break;
		case WALKING:
			keyFrame = PlayerRes.walkAnim.getKeyFrame(stateTime);
			break;
		case PUNCHING:
			keyFrame = PlayerRes.punchAnim.getKeyFrame(stateTime);
			break;
		case KICKING:
			keyFrame = PlayerRes.kickAnim.getKeyFrame(stateTime);
			break;
		case DEAD:
			keyFrame = PlayerRes.dieAnim.getKeyFrame(stateTime);
			break;
		case CROUCHING:
			keyFrame = PlayerRes.crouchFrame; // O animazione
			break;
		case JUMPING:
			keyFrame = PlayerRes.jumpAnim.getKeyFrame(stateTime);
			break;
		case PUNCHING_CROUCH:
			keyFrame = PlayerRes.punchCrouchAnim.getKeyFrame(stateTime);
			break;
		case KICKING_CROUCH:
			keyFrame = PlayerRes.kickCrouchAnim.getKeyFrame(stateTime);
			break;
		case CLIMBING_STAIRS:
			keyFrame = PlayerRes.climbAnim.getKeyFrame(stateTime, true);
			break;
		case KICKING_JUMP:
			// Prendi il frame specifico (solitamente l'ultimo del calcio o uno dedicato)
			keyFrame = PlayerRes.kickJumpFrame; // Oppure un frame statico dedicato
			break;
		case PUNCHING_JUMP:
			// Prendi il frame specifico (solitamente l'ultimo del calcio o uno dedicato)
			keyFrame = PlayerRes.punchJumpFrame; // Oppure un frame statico dedicato
			break;
		default:
			keyFrame = PlayerRes.idleFrame;
			break;
		}

		// Recuperiamo i dati di "originalWidth" salvati dal Packer
		// Se lo sprite era centrato in un quadro 70x70, rimarrà centrato!
		float width = keyFrame.getRegionWidth();
		float height = keyFrame.getRegionHeight();

		// Disegno centrato sulla position.x (asse dei piedi)
		float drawX = position.x - (width / 2) + shakeOffset;

		if (facingRight) {
			batch.draw(keyFrame, drawX, position.y, width, height);
		} else {
			batch.draw(keyFrame, drawX + width, position.y, -width, height);
		}
	}

	// Aggiungi questo metodo nella classe Player
	public boolean checkAndResetLiberation() {
		// System.out.println(struggleCount+" / "+STRUGGLE_THRESHOLD);
		if (struggleCount >= STRUGGLE_THRESHOLD) {
			// System.out.println("LIBERATO");
			struggleCount = 0;
			currentState = State.IDLE; // Thomas torna libero
			stateTime = 0;
			return true;
		}
		return false;
	}

	public boolean isAttacking() {
		if (this.currentState == Player.State.KICKING || this.currentState == Player.State.PUNCHING
				|| this.currentState == Player.State.PUNCHING_CROUCH || this.currentState == Player.State.KICKING_CROUCH
				|| this.currentState == Player.State.KICKING_JUMP|| this.currentState == Player.State.PUNCHING_JUMP) {
			return true;
		} else {
			return false;
		}
	}

	public void resetStateTime() {
		this.stateTime = 0;
	}

	public void dispose() {
		PlayerRes.dispose();
	}
}
