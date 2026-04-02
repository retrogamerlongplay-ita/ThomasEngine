package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.HunchbackRes;
import it.mpace.thomas.screen.Level4Screen;
import it.mpace.thomas.screen.LevelScreen;
import it.mpace.thomas.sprite.Crow;
import it.mpace.thomas.sprite.MagicFlame;

public class Hunchback extends Enemy {

	private float attackTimer = 0;
	private final float ATTACK_COOLDOWN = 2.0f;
	private EnemyState currentState = EnemyState.WAITING;
	private LevelScreen level;
	private boolean isClone = false;
	private float lifeTimer = 0;
	private final float CLONE_DURATION = 3.0f;
	private float cloneDieTimer = 0;

	// Distanza di attivazione per iniziare a lanciare magie
	private final float ACTIVATION_RANGE = 200f;

	public Hunchback(float x, float y, boolean isClone, LevelScreen screen) {
		super(x, y, isClone ? 1 : 40); // Boss con 40 HP
		this.isClone = isClone;
		this.speed = 0; // Resta fermo a fine livello
		this.facingRight = this.isClone; // Guarda verso sinistra (Thomas che arriva)
		this.hurtbox = new Rectangle(x, y, 25, 60);
		this.active = true; // <--- FORZA active a true
		this.isDying = false;
		this.level = screen;
		this.stateTime = 0; // <--- RESETTA il tempo
		this.lifeTimer = 0;

		// Lo stato deve essere ATTACKING per essere visibile subito
		if (this.isClone)
			this.state = EnemyState.ATTACKING;

		this.position.set(x, y);
		this.hurtbox = new Rectangle(x, y, 25, 60);
	}

	private void updateDyingClonePhysics(float dt) {
		stateTime += dt;
		cloneDieTimer += dt;
		// Effetto volo all'indietro rispetto a dove guarda
//		position.x += (facingRight ? -80 : 80) * dt;
//		position.y += velocityY * dt;
//		velocityY -= 1000 * dt; // Gravità arcade
		if (cloneDieTimer > 1.0f)
			active = false;
	}

	@Override
	public void update(float dt, Player player) {
		if (isDying) {
			if (this.isClone) {
				updateDyingClonePhysics(dt);
			} else {
				updateDyingPhysics(dt);
				return;
			}
		}

		stateTime += dt;
		if (isClone) {
			lifeTimer += dt;
			// Il clone cammina verso il player o lancia una sola fiamma e sparisce
			position.x += (facingRight ? speed : -speed) * dt;
			if (lifeTimer > CLONE_DURATION) {
				active = false;
				System.out
						.println("Clone rimosso. Motore: " + (lifeTimer > CLONE_DURATION ? "Timeout" : "Culling/Hit"));
			}
		} else {

			float dist = Math.abs(position.x - player.position.x);

			// 1. LOGICA DI ATTIVAZIONE
			if (currentState == EnemyState.WAITING && dist < ACTIVATION_RANGE) {
				currentState = EnemyState.IDLE;
			}

			// 2. CICLO DI ATTACCO
			if (currentState != EnemyState.WAITING && currentState != EnemyState.DEAD) {
				attackTimer += dt;

				if (attackTimer > ATTACK_COOLDOWN && currentState != EnemyState.ATTACKING) {
					startRandomAttack();
					attackTimer = 0;
				}
			}

			// Torna in IDLE dopo l'animazione di attacco
			if (currentState == EnemyState.ATTACKING && HunchbackRes.attackHighAnim.isAnimationFinished(stateTime)) {
				currentState = EnemyState.IDLE;
			}
		}

		// Aggiorna sempre la hurtbox sulla posizione fissa
		hurtbox.setPosition(position.x - 12, position.y);
	}

	@Override
	public void hit(Player p, LevelScreen level) {
		super.hit(p, level);
		if (isClone) {
			this.isDying = true; // Il clone esplode subito
			// this.active = false;
			System.out.println("Clone rimosso. Motore: " + (lifeTimer > CLONE_DURATION ? "Timeout" : "Culling/Hit"));
		}
	}

	private void startRandomAttack() {
		stateTime = 0;
		currentState = EnemyState.ATTACKING;

		float choice = MathUtils.random();

		if (choice < 0.33f) {
			spawnCrow();
		} else if (choice < 0.66f) {
			spawnMagicFlame();
		} else {
			spawnIllusionClone();
		}
	}

	private void spawnCrow() {
		System.out.println("MAGIA: Lancio Corvo ad altezza testa!");
		// Qui chiamerai: level.spawnCrow(position.x, position.y + 45, facingRight);
		((Level4Screen) level).crows.add(new Crow(position.x, position.y + 45, facingRight));
		// AudioRes.playSound(AudioRes.crowSound); // Se hai un jingle per il corvo
	}

	private void spawnMagicFlame() {

		boolean diagonal = MathUtils.randomBoolean();
		float spawnY = diagonal ? position.y + 80 : position.y + 45;
		((Level4Screen) level).flames.add(new MagicFlame(position.x, spawnY, facingRight, diagonal));
//        if (diagonal) {
//            System.out.println("MAGIA: Fiamma Diagonale dall'alto!");
//        } else {
//            System.out.println("MAGIA: Fiamma Orizzontale altezza testa!");
//        }
	}

	private void spawnIllusionClone() {
		if (this.isClone) {
			System.out.println("A Clone cannot spawn a clone.");
		} else {
			System.out.println("MAGIA: Appare un clone dall'altro lato!");
			Hunchback clone = new Hunchback(level.player.position.x - 30, position.y, true, level);
			((Level4Screen) level).enemies.add(clone);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {
		TextureRegion frame;
		if (isDying) {
			if (this.isClone) {
				frame = HunchbackRes.disappearAnim.getKeyFrame(stateTime);
			} else {
				frame = HunchbackRes.dieAnim.getKeyFrame(stateTime);
			}
		} else if (currentState == EnemyState.ATTACKING) {
			frame = HunchbackRes.attackHighAnim.getKeyFrame(stateTime, false);
		} else {
			frame = HunchbackRes.walkAnim.getKeyFrame(stateTime, true);
		}

		drawHelper(batch, frame);
	}

	// Metodi necessari per l'interfaccia Enemy
	@Override
	public void flee() {
		this.active = false;
		System.out.println("Clone rimosso. Flee Motore: " + (lifeTimer > CLONE_DURATION ? "Timeout" : "Culling/Hit"));
	}

	@Override
	public Rectangle getHitBox() {
		return null;
	} // Non tocca fisicamente il player

	@Override
	public EnemyState getState() {
		return currentState;
	}

	@Override
	public void setState(EnemyState newState) {
		this.currentState = newState;
	}

	@Override
	public int getHitScoreValue() {
		return 100;
	}

	@Override
	public int getDieScoreValue() {
		if (this.isClone) {
			return 50;
		} else {
			return 5000;
		}
	}
}
