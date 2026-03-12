package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.KnifeThrowerRes;
import it.mpace.thomas.screen.LevelScreen;
import it.mpace.thomas.sprite.Knife;

public class KnifeThrower extends Enemy {

	public EnemyState state = EnemyState.WALKING;

	private float attackCooldownTimer = 0;
	private final float ATTACK_COOLDOWN_DURATION = 2.5f;

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
		this.speed = LevelConstants.FIRST_FLOOR_KNIFE_THROWER_SPEED; // Più lento del Gripper
	}

	// Sovrascriviamo l'update per accettare la lista dei coltelli
	// In ThomasMain chiameremo: ((KnifeThrower)e).update(dt, playerPos, knives);
	public void update(float dt, Player player, Array<Knife> fieldKnives) {
		if (isDying) {
			updateDyingPhysics(dt);
			return;
		}
		if (fleeingForever) {
			// Fuga definitiva: si allontana per sempre senza più tornare
			float retreatDir = (position.x > player.position.x) ? 1 : -1;
			facingRight = (retreatDir > 0);
			position.x += (speed * RETREAT_SPEED_MULT) * retreatDir * dt;
			state = EnemyState.RETREATING;
			stateTime += dt;
			hurtbox.set(position.x - 10, position.y, 20, 65);
			return; // Salta tutto il resto del comportamento
		}
		attackCooldownTimer -= dt;
		stateTime += dt;
		float distance = Math.abs(position.x - player.position.x);

		// Gestione stordimento ultra-rapido
		if (state == EnemyState.HURT) {
			if (stateTime >= HURT_DURATION) {
				state = EnemyState.RETREATING; // Passa subito alla fuga
				stateTime = 0;
			}
			// Aggiorna comunque la hurtbox nel caso Thomas tiri un secondo colpo rapido
			hurtbox.set(position.x - 10, position.y, 20, 65);
			return;
		}

		switch (state) {
		case IDLE:
		    // Se Thomas si allontana o si gira, torna in WALKING per riposizionarti
		    distance = Math.abs(position.x - player.position.x);
		    boolean stillFacingMe = (player.position.x < position.x && player.facingRight) || 
		                            (player.position.x > position.x && !player.facingRight);
		    
		    // Esci da IDLE se: 1. Cooldown finito, 2. Distanza cambiata, 3. Thomas si è girato
		    if (attackCooldownTimer <= 0 || distance > STOP_DISTANCE + 10f || distance < STOP_DISTANCE - 10f || !stillFacingMe) {
		        state = EnemyState.WALKING;
		        stateTime = 0;
		    }
		    break;
		case WALKING:
			distance = Math.abs(position.x - player.position.x);
			boolean playerFacingMe = (player.position.x < position.x && player.facingRight)
					|| (player.position.x > position.x && !player.facingRight);
			// 1. CONDIZIONE DI ATTACCO
			// Attacca se è vicino E il giocatore lo sta guardando, oppure se è vicinissimo
			if (distance <= STOP_DISTANCE && (playerFacingMe || distance < 80f)) {
				if (attackCooldownTimer <= 0) {
					state = EnemyState.ATTACKING;
					stateTime = 0;
					throwTimer = 0;
					knivesThrown = 0;
				} else {
					// Se è vicino ma in cooldown, resta fermo in guardia
					state = EnemyState.IDLE;
				}
			}
			// 2. CONDIZIONE DI INSEGUIMENTO (Il Player è voltato dall'altra parte)
			else if (!playerFacingMe && distance > RETREAT_DISTANCE) {
				// Insegue Thomas perché lui non lo sta guardando
				facingRight = (position.x < player.position.x);
				position.x += (facingRight ? speed : -speed) * dt;
			}
			// 3. MOVIMENTO STANDARD
			else {
				facingRight = (position.x < player.position.x);
				position.x += (facingRight ? speed : -speed) * dt;
			}
			break;
		case ATTACKING:
		case ATTACKING_HIGH:
		case ATTACKING_LOW:
			throwTimer += dt;
			// 1. MIRA DINAMICA: Anche mentre è fermo per lanciare, si gira verso Thomas
			facingRight = (position.x < player.position.x);
			Animation<TextureRegion> currentAnim = (state == EnemyState.ATTACKING_HIGH) ? KnifeThrowerRes.throwHighAnim
					: KnifeThrowerRes.throwLowAnim;
			// NON cammina mentre attacca
			if (knivesThrown < 2 && throwTimer > 0.8f && (currentAnim.getKeyFrameIndex(stateTime) == 1)) {
				boolean shootHigh = MathUtils.randomBoolean();
				fieldKnives.add(new Knife(position.x, shootHigh ? position.y + 45 : position.y + 15, facingRight));
				knivesThrown++;
				throwTimer = 0;
				stateTime = 0;
				this.state = shootHigh ? EnemyState.ATTACKING_HIGH : EnemyState.ATTACKING_LOW;
			}
			// Dopo 2 lanci, attiva il cooldown e scappa
			if (knivesThrown >= 2 && throwTimer > 0.5f) {
				state = EnemyState.RETREATING;
				stateTime = 0;
				attackCooldownTimer = ATTACK_COOLDOWN_DURATION;
			}
			break;
		case RETREATING:
			// 1. Calcolo direzione di fuga (scappa DA Thomas)
			float retreatDir = (position.x > player.position.x) ? 1 : -1;

			// 2. CONTROLLO "SGUARDO" DI THOMAS (Il cuore della tua richiesta)
			// Se Thomas guarda dalla parte OPPOSTA a dove si trova il KnifeThrower
			boolean playerIsLookingAway = (position.x > player.position.x && !player.facingRight)
					|| (position.x < player.position.x && player.facingRight);

			if (playerIsLookingAway && !fleeingForever) {
				// Thomas non mi guarda? Torno all'attacco!
				state = EnemyState.WALKING;
				stateTime = 0;
				// Non resettiamo il retreatCount qui, così manteniamo il limite di fughe totali
			} else {
				// Thomas mi guarda o sono in fuga definitiva: continua a scappare
				facingRight = (retreatDir > 0);
				position.x += (speed * RETREAT_SPEED_MULT) * retreatDir * dt;

				// Se è scappato abbastanza lontano, può decidere di tornare a camminare
				// normalmente
				if (!fleeingForever && Math.abs(position.x - player.position.x) > STOP_DISTANCE) {
					state = EnemyState.WALKING;
					attackCooldownTimer = 0; // Pronto per un nuovo lancio se torna vicino
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
		// Questo verrà usato se non passiamo i coltelli,// ma noi useremo l'overload
		// sopra in ThomasMain
	}

	@Override
	public void hit(Player p, LevelScreen level) {
		super.hit(p, level); // Sottrae HP
		if (hp > 0) {
			this.state = EnemyState.HURT;
			// Piccola spinta all'indietro quando viene colpito
			position.x += (facingRight ? -15 : 15);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		TextureRegion frame;
		if (isDying) {
			frame = KnifeThrowerRes.dieAnim.getKeyFrame(stateTime);
		} else if (state == EnemyState.HURT) {
			// Usa i frame hurt che hai già caricato in KnifeThrowerRes
			frame = KnifeThrowerRes.hurtHigh;
		} else {
			switch (state) {
			case ATTACKING_HIGH:
				frame = KnifeThrowerRes.throwHighAnim.getKeyFrame(stateTime, false);
				// if (KnifeThrowerRes.throwHighAnim.isAnimationFinished(stateTime)) state =
				// EnemyState.WALKING; // Torna a camminare dopo il lancio alto
				break;
			case ATTACKING_LOW:
				frame = KnifeThrowerRes.throwLowAnim.getKeyFrame(stateTime, false);
				// if (KnifeThrowerRes.throwLowAnim.isAnimationFinished(stateTime)) state =
				// EnemyState.WALKING; // Torna a camminare dopo il lancio basso
				break;
			case IDLE:
				frame = KnifeThrowerRes.walkAnim.getKeyFrame(0);
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
		this.state = EnemyState.RETREATING;

	}

	public EnemyState getState() {
		return state;
	}

	public void setState(EnemyState state) {
		this.state = state;
	}

	@Override
	public Rectangle getHitBox() {
		// TODO Auto-generated method stub
		return new Rectangle(0, 0, 0, 0);
	}
}
