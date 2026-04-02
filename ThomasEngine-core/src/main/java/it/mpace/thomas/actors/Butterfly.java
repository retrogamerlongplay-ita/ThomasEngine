package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.ButterflyRes;
import it.mpace.thomas.screen.LevelScreen;

public class Butterfly extends Enemy {
	private float startY;
	private float amplitude = 30f; // Quanto oscilla in alto/basso
	private float frequency = 5f; // Velocità dell'oscillazione
	private float moveDirection;
	private LevelScreen level;

	// Nuovi campi per il comportamento di "affacciarsi" / idle
	private float idleDelay = 0.8f; // secondi da restare fermi all'apparizione
	private float idleTimer = 0f;
	private boolean hasStartedFlying = false;

	// Nuovi campi per la svolta dopo aver oltrepassato Thomas
	private boolean isTurning = false;
	private float turningTimer = 0f;
	private final float turningDuration = 0.35f; // durata della rotazione (secondi)
	private boolean hasReversed = false; // evita flip continui

	public Butterfly(float x, float y, LevelScreen level) {
		super(x, y, 1);
		this.level = level;
		this.startY = y;
		this.speed = 80f; // Abbastanza veloce
		this.hurtbox = new Rectangle(x, y, 12, 12);

		// Decide la direzione in base a dove si trova Thomas
		this.facingRight = (x < level.player.position.x);
		this.moveDirection = facingRight ? 1 : -1;

		// Imposta il timer di idle: piccolo offset casuale per variare il comportamento
		this.idleTimer = idleDelay + MathUtils.random(-0.2f, 0.2f);
	}

	@Override
	public void update(float dt, Player player) {
		if (isDying) {
			// Se è in stato di morte, facciamo avanzare l'animazione di esplosione
			stateTime += dt;
			// Quando l'animazione di esplosione finisce, rimuoviamo l'entità
			if (ButterflyRes.explodingAnim.isAnimationFinished(stateTime)) {
				this.active = false;
			}
			// Non proseguire con il resto della logica
			return;
		}

		// Se non ha ancora iniziato a volare, decrementa il timer di idle
		if (!hasStartedFlying) {
			idleTimer -= dt;
			// Resta fermo durante l'idle: non aggiornare stateTime per la volo
			if (idleTimer <= 0f) {
				hasStartedFlying = true;
				// resetta stateTime per far partire l'animazione di volo dall'inizio
				this.stateTime = 0f;
			}
			// Mantieni hurtbox in posizione corretta e non muovere
			hurtbox.setPosition(position.x - 6, position.y - 6);
			return;
		}

		// Se siamo in fase di rotazione/ritorno, decrementa il timer
		if (isTurning) {
			turningTimer -= dt;
			// Mantieni hurtbox in posizione corretta e non muovere mentre ruota
			hurtbox.setPosition(position.x - 6, position.y - 6);
			if (turningTimer <= 0f) {
				isTurning = false; // fine rotazione: riprendi il volo
				// permetti eventualmente future inversioni solo dopo essersi allontanati
			}
			return;
		}

		// Ora è in volo: comportamento come prima
		stateTime += dt;

		// Controllo di oltrepassamento di Thomas: se lo supera, fa una rotazione e torna indietro
		if (!hasReversed) {
			float passThreshold = 80f; // richiede di superare il player di almeno 80 pixel
			if ((moveDirection > 0 && position.x > player.position.x + passThreshold) || (moveDirection < 0 && position.x < player.position.x - passThreshold)) {
				// Inizia la rotazione: pausa breve e inverti direzione
				isTurning = true;
				turningTimer = turningDuration;
				// Invertiamo direzione di movimento e lo specchiamo (per il disegno futuro)
				moveDirection = -moveDirection;
				facingRight = !facingRight;
				hasReversed = true;
				return;
			}
		}

		// Movimento orizzontale
		position.x += speed * moveDirection * dt;

		// Movimento SINUSOIDALE (Il "volo" dell'insetto)
		position.y = startY + MathUtils.sin(stateTime * frequency) * amplitude;

		// Aggiorna Hurtbox
		hurtbox.setPosition(position.x - 6, position.y - 6);

		// Collisione con Thomas (Danno al contatto)
		if (hurtbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
			player.takeHit(5f, this.level, position.x, position.y);
			AudioRes.playSound(AudioRes.hazardHitSound);
			this.active = false; // Muore dopo il contatto come un proiettile
		}

		// Auto-distruzione se fuori schermo e reset del flag di inversione
		if (Math.abs(position.x - player.position.x) > 200) {
			active = false;
			// reset allow future reversals quando ricomparirà
			hasReversed = false;
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		TextureRegion frame;
		if (isDying) {
			// Mostra l'animazione di esplosione caricata in ButterflyRes
			frame = ButterflyRes.explodingAnim.getKeyFrame(stateTime, false);
			// Usiamo drawHelper per esplosione (si può specchiare se necessario)
			drawHelper(batch, frame);
			return;
		}

		// Se sta ancora in idle, mostra il frame idle statico
		if (!hasStartedFlying) {
			frame = ButterflyRes.idleFrame;
			drawHelper(batch, frame);
			return;
		}

		// Se è in rotazione, mostriamo un frame orientato verso il basso (south variants)
		if (isTurning) {
			TextureRegion turnFrame;
			// Scegliamo il frame in base alla direzione di movimento dopo la rotazione
			if (moveDirection > 0) {
				turnFrame = ButterflyRes.southEastFrame;
			} else if (moveDirection < 0) {
				turnFrame = ButterflyRes.southWestFrame;
			} else {
				turnFrame = ButterflyRes.southFrame;
			}

			// Disegniamo il frame di svolta SENZA usareil drawHelper perchè vogliamo usare
			// esattamente la variante presente nell'atlas (southEast/southWest/south)
			float w = turnFrame.getRegionWidth();
			float h = turnFrame.getRegionHeight();
			float drawX = position.x - (w / 2);
			batch.draw(turnFrame, drawX, position.y, w, h);
			return;
		}

		// Animazione di volo
		frame = ButterflyRes.flyingAnim.getKeyFrame(stateTime, true);
		drawHelper(batch, frame);
	}

	@Override
	public void flee() {
		active = false;
	}

	@Override
	public Rectangle getHitBox() {
		return hurtbox;
	}

	@Override
	public EnemyState getState() {
		return EnemyState.WALKING;
	}

	@Override
	public int getHitScoreValue() {
		return this.getDieScoreValue();
	}

	@Override
	public int getDieScoreValue() {
		return 100;
	}

	@Override
	public void setState(EnemyState newState) {
	}
}