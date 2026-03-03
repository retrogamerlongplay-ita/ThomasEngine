package it.mpace.thomas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.PlayerRes;

public class Player {
	// Stati del giocatore
	public enum State {
		IDLE, WALKING, PUNCHING, KICKING, CROUCHING, JUMPING, GRABBED, DEAD, PUNCHING_CROUCH, KICKING_CROUCH,
		KICKING_JUMP
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
		this.screen=s;
	}

	public void takeHit(float damage) {
		if (currentState == State.DEAD)
			return;

		GameControlRes.decrementEnergy(damage);
		this.currentState = State.GRABBED; // Usiamo l'animazione hurt
		this.stateTime = 0;
		this.hurtTimer = HURT_DURATION;
	}

	public void triggerDeath() {
		this.currentState = State.DEAD;
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
		float bodyWidth = 20; // Imposta la larghezza reale del torso di Thomas ritagliato

		// Gestione altezza Hurtbox dinamica
		float currentHurtboxHeight = (currentState == State.CROUCHING) ? 40 : 65;
		hurtbox.set(position.x - (bodyWidth / 2), position.y, bodyWidth, currentHurtboxHeight);

		if (hurtTimer > 0) {
			hurtTimer -= deltaTime;
			if (hurtTimer <= 0 && currentState == State.GRABBED) {
				currentState = State.IDLE; // Torna subito attivo
			}
			// Mentre è stordito, non processiamo l'input di attacco/movimento
			return;
		}
		
		if (autoWalking) {
		    currentState = State.WALKING;
		    position.x -= speed * 0.5f * deltaTime; // Cammina lentamente verso sinistra
		    stateTime += deltaTime;
		    // Se raggiunge il punto esatto delle scale, iniziamo a farlo salire (Y)
		    if (position.x <= LevelConstants.FIRST_FLOOR_LEFT) {
		        position.y += speed * 0.4f * deltaTime; // Sale le scale
		    }
		    return; // Ignora l'input dell'utente durante l'autoWalking
		}

		// --- FISICA DI CADUTA SEMPRE ATTIVA (anche se GRABBED o DEAD) ---
		if ((position.y > 510 || velocityY > 0) && currentState != State.DEAD) {
			position.y += velocityY * deltaTime;
			velocityY -= GRAVITY * deltaTime;

			// Atterraggio (solo se non siamo nello stato DEAD, dove Thomas deve "bucare" il
			// suolo)
			if (position.y <= 510 && currentState != State.DEAD) {
				position.y = 510;
				velocityY = 0;
				if (currentState == State.JUMPING)
					currentState = State.IDLE;
			}
		}

		// LIMITI DEL LIVELLO
		if (position.x > LevelConstants.FIRST_FLOOR_RIGHT) {
			position.x = LevelConstants.FIRST_FLOOR_RIGHT;
		}

		if (this.screen.getBoss().isActive()) {
		    // Se il boss è vivo, Thomas non passa
		    if (this.position.x < this.screen.getBoss().position.x + 10) {
		        this.position.x = this.screen.getBoss().position.x + 10;
		    }
		} else {
		    // Se il boss è morto, Thomas può raggiungere le scale (es. x = 30)
		    if (this.position.x < LevelConstants.FIRST_FLOOR_LEFT) {
		        this.screen.startLevelTransition(deltaTime);
		    }
		}

		if (currentState == State.DEAD) {
			//stateTime += deltaTime;
			// Sposta Thomas all'indietro rispetto a dove guarda
			float direction = facingRight ? -1 : 1;
			position.x += direction * 50f * deltaTime;
			// Fisica della caduta
			position.y += velocityY * deltaTime;
			velocityY -= GRAVITY * deltaTime;
			return;
		}

		if (currentState == State.GRABBED) {
			// 1. LOGICA DI LIBERAZIONE
			if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
				struggleCount++;
				// Effetto vibrazione: inverte l'offset a ogni pressione
				shakeOffset = (shakeOffset == 0) ? 2 : -2;
			} else {
				// Smorza la vibrazione gradualmente
				shakeOffset *= 0.9f;
			}
			hitbox.set(0, 0, 0, 0); // Non può attaccare mentre è afferrato
			return; // BLOCCA il resto dell'input
		}

		// 1. GESTIONE STATI DI ATTACCO (Hanno la precedenza)
		if (currentState == State.PUNCHING) {
			if (PlayerRes.punchAnim.isAnimationFinished(stateTime)) {
				currentState = State.IDLE;
				hitbox.set(0, 0, 0, 0); // Reset a fine animazione
			} else {
				// Attiviamo la hitbox SOLO al frame dell'impatto (es. il frame 1)
				int frameIndex = PlayerRes.punchAnim.getKeyFrameIndex(stateTime);

				if (frameIndex == 1) { // Supponendo che il frame 1 sia il braccio teso
					float hw = 18; // Larghezza della hitbox
					float hh = 8; // Altezza della hitbox

					if (facingRight) {
						// Parte dal centro e va a destra
						hitbox.set(position.x, position.y + 40, hw, hh);
					} else {
						// Parte dal centro e va a sinistra
						hitbox.set(position.x - hw, position.y + 40, hw, hh);
					}
				} else {
					hitbox.set(0, 0, 0, 0); // Hitbox spenta negli altri frame
				}
			}
			return;
		}

		else if (currentState == State.KICKING) {
			if (PlayerRes.kickAnim.isAnimationFinished(stateTime)) {
				currentState = State.IDLE;
				hitbox.set(0, 0, 0, 0);
			} else {
				int frameIndex = PlayerRes.kickAnim.getKeyFrameIndex(stateTime);

				// Per il calcio, magari il frame di impatto è il 2 (più lento del pugno)
				if (frameIndex == 1 || frameIndex == 2) {
					float hw = 22; // Il calcio arriva più lontano
					float hh = 10;

					if (facingRight) {
						hitbox.set(position.x, position.y + 42, hw, hh);
					} else {
						hitbox.set(position.x - hw, position.y + 42, hw, hh);
					}
				} else {
					hitbox.set(0, 0, 0, 0);
				}
			}
			return;
		} else // --- ATTACCO BASSO (Pugno) ---
		if (currentState == State.PUNCHING_CROUCH) {
			if (PlayerRes.punchCrouchAnim.isAnimationFinished(stateTime)) {
				currentState = State.CROUCHING; // Torna abbassato, non IDLE!
			} else {
				if (PlayerRes.punchCrouchAnim.getKeyFrameIndex(stateTime) == 1) {
					float hw = 18;
					float hh = 8;
					// Y abbassata (es. GROUND_Y + 20 invece di 40)
					hitbox.set(facingRight ? position.x : position.x - hw, position.y + 27, hw, hh);
				}
			}
			return;
		} else // --- ATTACCO BASSO (Calcio) ---
		if (currentState == State.KICKING_CROUCH) {
			if (PlayerRes.kickCrouchAnim.isAnimationFinished(stateTime)) {
				currentState = State.CROUCHING;
			} else {
				if (PlayerRes.kickCrouchAnim.getKeyFrameIndex(stateTime) == 1) {
					float hw = 26;
					float hh = 10; // Il calcio basso è molto lungo!
					hitbox.set(facingRight ? position.x : position.x - hw, position.y, hw, hh);
				}
			}
			return;
		} else {
			hitbox.set(0, 0, 0, 0); // Disattiva la hitbox se non attacca
		}

		// --- INPUT EVASIONE E ATTACCO BASSO ---
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && currentState != State.JUMPING) {

			// Se premo Z mentre sono giù -> Pugno Basso
			if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
				currentState = State.PUNCHING_CROUCH;
				stateTime = 0;
			}
			// Se premo X mentre sono giù -> Calcio Basso
			else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
				currentState = State.KICKING_CROUCH;
				stateTime = 0;
			}
			// Altrimenti resta semplicemente abbassato
			else if (currentState != State.PUNCHING_CROUCH && currentState != State.KICKING_CROUCH) {
				currentState = State.CROUCHING;
			}
			return;
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && position.y <= 510 && currentState != State.JUMPING) {
			currentState = State.JUMPING;
			velocityY = 350f; // Forza del salto arcade
			return;
		}

		// 2. INPUT ATTACCO (Trigger)
		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			currentState = State.PUNCHING;
			stateTime = 0;
			return; // Esci per evitare che il movimento sovrascriva lo stato
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			currentState = State.KICKING;
			stateTime = 0;
			return;
		}

		// INPUT DI CONTROLLO DEL GIOCO forse bisogna metterli in una classe a parte
		if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
			System.out.println("D pressed");
			GameControlRes.debugMode = true;
			return;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
			System.out.println("N pressed");
			GameControlRes.debugMode = false;
			return;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
			System.out.println("POSITION (" + position.x + "," + position.y + ")");
			return;
		}

		// 3. LOGICA DI MOVIMENTO (Solo se non sta attaccando)
		boolean moving = false;
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			position.x += speed * deltaTime;
			facingRight = true;
			if (currentState != State.JUMPING)
				currentState = State.WALKING;
			moving = true;
		} else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			position.x -= speed * deltaTime;
			facingRight = false;
			if (currentState != State.JUMPING)
				currentState = State.WALKING;
			moving = true;
		}

		if (!moving && currentState != State.JUMPING) {
			currentState = State.IDLE;
		}
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
				|| this.currentState == Player.State.KICKING_JUMP) {
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
