package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.GripperRes;
import it.mpace.thomas.screen.LevelScreen;

public class Gripper extends Enemy {

	public boolean isGrabbedFromRight;
	// private final float APPROACH_DISTANCE = 100;

	public EnemyState state = EnemyState.WALKING;

	public Gripper(float x, float y) {
		super(x, y, 1); // Chiama il costruttore di Enemy che inizializza position e hurtbox
		this.speed = LevelConstants.FIRST_FLOOR_GRIPPER_SPEED;
	}

	@Override
	public void update(float deltaTime, Player player) {
		// Se lo stato è DYING, esegui solo la fisica di caduta e ESCI
		if (state == EnemyState.DYING || this.isDying) {
			updateDyingPhysics(deltaTime);
			return;
		}

		if (player.currentState == Player.State.DEAD && state == EnemyState.GRABBING) {
			state = EnemyState.WALKING;
			// Non resettiamo la Y qui, lasciamo che ognuno cada per i fatti suoi
		}

		stateTime += deltaTime;
		if (state == EnemyState.GRABBING && player.currentState != Player.State.DEAD) {
			// Mantiene la posizione fissa una volta agganciato
			float grabOffset = isGrabbedFromRight ? 15 : -10;
			position.x = player.position.x + grabOffset;
			position.y = player.position.y;
			facingRight = !isGrabbedFromRight;
		} else if (state == EnemyState.FLEEING) {
			// Scappa nella direzione opposta a Thomas o semplicemente verso l'uscita più
			// vicina
			float fleeDir = (position.x > player.position.x) ? 1 : -1;
			facingRight = (fleeDir > 0);
			position.x += (speed * 1.5f) * fleeDir * deltaTime; // Scappa più veloce del normale!

			// Se esce dallo schermo, disattivalo
			if (Math.abs(position.x - player.position.x) > 300)
				active = false;
			return; // Interrompe il resto della logica di inseguimento
		}

		else {
			// --- LOGICA ANTI-SFARFALLAMENTO ---
			boolean isRightOfPlayer = position.x > player.position.x;

			// Definiamo la "distanza di stop" ideale per l'abbraccio
			// Se è a destra si ferma a +15, se è a sinistra a -10
			float targetX = isRightOfPlayer ? player.position.x + 15 : player.position.x - 10;
			float distanceToTarget = Math.abs(position.x - targetX);

			if (distanceToTarget > 2f) { // Muoviti solo se non sei ancora al target
				if (position.x < targetX) {
					position.x += speed * deltaTime;
					facingRight = true;
				} else {
					position.x -= speed * deltaTime;
					facingRight = false;
				}
				state = EnemyState.WALKING;
			} else {
				// È arrivato in posizione di "abbraccio"
				// Se Thomas non è già occupato da un altro Gripper su questo lato, GRABBA!
				// (Per ora lo facciamo agganciare sempre, poi aggiungeremo il controllo slot
				// nel Player)
				state = EnemyState.GRABBING;
				isGrabbedFromRight = isRightOfPlayer;
				player.currentState = Player.State.GRABBED; // Notifica Thomas
			}
		}
		// Aggiorna la hurtbox ereditata (centrata su position.x)
		float bodyWidth = 20;
		hurtbox.set(position.x - (bodyWidth / 2), position.y, bodyWidth, 65);
	}

	@Override
	public void hit(Player p, LevelScreen level) {
		super.hit(p, level); // Imposta isDying = true e disabilita hurtbox
		this.state = EnemyState.DYING; // <--- Forza lo stato interno
		this.stateTime = 0; // Reset timer per l'animazione di morte
	}

	@Override
	public void draw(SpriteBatch batch) {
		TextureRegion currentFrame;
		if (isDying) {
			currentFrame = GripperRes.dieAnim.getKeyFrame(stateTime);
		} else {
			switch (state) {
			case GRABBING:
				currentFrame = GripperRes.grabAnim.getKeyFrame(stateTime);
				break;
			case APPROACHING:
				currentFrame = GripperRes.approachAnim.getKeyFrame(stateTime);
				break;
			default:
				currentFrame = GripperRes.walkAnim.getKeyFrame(stateTime);
				break;
			}
		}

		// Usa l'helper di Enemy per disegnare (gestisce già il flip e il pivot)
		drawHelper(batch, currentFrame);
	}

	@Override
	public boolean canGrabPlayer() {
		return !isDying && state != EnemyState.GRABBING;
	}

	@Override
	public void flee() {
		this.state = EnemyState.FLEEING;

	}

	public EnemyState getState() {
		return state;
	}

	public void setState(EnemyState state) {
		this.state = state;
	}

	@Override
	public Rectangle getHitBox() {
		return new Rectangle(0, 0, 0, 0);
	}
}
