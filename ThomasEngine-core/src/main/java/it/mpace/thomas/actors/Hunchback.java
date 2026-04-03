package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.HunchbackRes;
import it.mpace.thomas.screen.Level4Screen;
import it.mpace.thomas.screen.LevelScreen;
import it.mpace.thomas.sprite.Crow;
import it.mpace.thomas.sprite.HeadProjectile;
import it.mpace.thomas.sprite.MagicFlame;

public class Hunchback extends Enemy {

	private float attackTimer = 0f;
	private final float ATTACK_COOLDOWN = 2.0f;

	private EnemyState currentState = EnemyState.WAITING;

	private LevelScreen level;
	private boolean isClone = false;
	private float lifeTimer = 0f;
	private final float CLONE_DURATION = 3.0f;

	private float cloneDieTimer = 0f;

	// --- DISAPPEAR / APPEAR LOGIC ---
	private boolean disappearing = false;
	private boolean noHead = false;
	private float disappearTimer = 0f;
	private final float REAPPEAR_DELAY = 0.3f;

	private float appearTimer = 0f; // Timer di apparizione
	private boolean invulnerable = false;

	private boolean headLaunched = false;

	private final float ACTIVATION_RANGE = 200f;

	public Hunchback(float x, float y, boolean isClone, LevelScreen screen) {
		super(x, y, isClone ? 1 : 40);

		this.isClone = isClone;
		this.level = screen;

		this.speed = 0;
		this.facingRight = this.isClone;
		this.hurtbox = new Rectangle(x, y, 25, 60);

		this.currentState = isClone ? EnemyState.ATTACKING : EnemyState.WAITING;
		this.active = true;
		this.isDying = false;

		this.position.set(x, y);
		this.stateTime = 0;
		this.lifeTimer = 0;
	}

	@Override
	public void update(float dt, Player player) {

		// ---------- DEATH ----------
		if (currentState == EnemyState.DEAD || isDying) {
			stateTime += dt;
			updateDyingPhysics(dt);

			if (HunchbackRes.dieAnim.isAnimationFinished(stateTime)) {
				active = false;
			}
			return;
		}

		// ---------- DISAPPEARING ----------
		if (disappearing) {

			disappearTimer += dt;
			stateTime += dt;

			hurtbox.set(0, 0, 0, 0);

			if (disappearTimer >= REAPPEAR_DELAY) {
				disappearing = false;
				noHead = false;
				appearTimer = 0;
				invulnerable = true;
				stateTime = 0;
			}
			return;
		}

		// ---------- APPEARING ----------
		if (invulnerable) {

			appearTimer += dt;
			stateTime += dt;

			if (appearTimer < 0.3f) {
				hurtbox.set(0, 0, 0, 0);
			} else {
				invulnerable = false;
				appearTimer = 0;
			}
		}

		// ---------- CLONE ----------
		if (isClone) {
			stateTime += dt;
			lifeTimer += dt;

			position.x += (facingRight ? speed : -speed) * dt;

			if (lifeTimer >= CLONE_DURATION) {
				active = false;
			}

			hurtbox.setPosition(position.x - 12, position.y);
			return;
		}

		// ---------- NORMAL HUNCHBACK ----------
		stateTime += dt;

		float dist = Math.abs(position.x - player.position.x);

		if (currentState == EnemyState.WAITING && dist < ACTIVATION_RANGE) {
			currentState = EnemyState.IDLE;
			stateTime = 0;
		}

		if (currentState != EnemyState.WAITING) {
			attackTimer += dt;

			if (attackTimer > ATTACK_COOLDOWN && currentState != EnemyState.ATTACKING) {
				startRandomAttack();
				attackTimer = 0;
			}
		}

		if (currentState == EnemyState.ATTACKING && HunchbackRes.attackHighAnim.isAnimationFinished(stateTime)) {
			currentState = EnemyState.IDLE;
		}

		// ✅ SEMPRE RIPRISTINARE LA HURTBOX SE NON INVULNERABILE
		if (!invulnerable && !disappearing) {
			hurtbox.set(position.x - 12, position.y, 25, 60);
		}
	}

	@Override
	public void hit(Player p, LevelScreen level) {

		if (invulnerable || disappearing || currentState == EnemyState.DEAD) {
			return;
		}

		super.hit(p, level);

		if (isClone) {
			startCloneDeath();
			return;
		}

		if (this.hp <= 0) {
			triggerDeath();
			return;
		}

		boolean isLowKick = p.currentState == Player.State.KICKING_CROUCH;

		boolean isHeadLossAttack = p.currentState == Player.State.KICKING || p.currentState == Player.State.KICKING_JUMP
				|| p.currentState == Player.State.PUNCHING || p.currentState == Player.State.PUNCHING_JUMP;

		if (isLowKick) {
			startDisappear(false);
			return;
		}

		if (isHeadLossAttack) {
			startDisappear(true);
			return;
		}
	}

	private void startCloneDeath() {
		this.disappearing = true;
		this.noHead = false; // i cloni non perdono la testa
		this.invulnerable = true;

		this.disappearTimer = 0;
		this.appearTimer = 0;
		this.stateTime = 0;

		hurtbox.set(0, 0, 0, 0);
	}

	private void startDisappear(boolean loseHead) {

		this.disappearing = true;
		this.noHead = loseHead;
		this.invulnerable = true;

		this.disappearTimer = 0;
		this.appearTimer = 0;
		this.stateTime = 0;

		hurtbox.set(0, 0, 0, 0);

		if (loseHead && !headLaunched) {
			headLaunched = true;

			((Level4Screen) level).headProjectiles.add(new HeadProjectile(position.x, position.y + 40, facingRight));
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
		((Level4Screen) level).crows.add(new Crow(position.x, position.y + 45, facingRight));
	}

	private void spawnMagicFlame() {
		boolean diagonal = MathUtils.randomBoolean();
		float spawnY = diagonal ? position.y + 80 : position.y + 45;

		((Level4Screen) level).flames.add(new MagicFlame(position.x, spawnY, facingRight, diagonal));
	}

	private void spawnIllusionClone() {
		if (!isClone) {
			Hunchback clone = new Hunchback(level.player.position.x - 30, position.y, true, level);

			((Level4Screen) level).enemies.add(clone);
		}
	}

	@Override
	public void draw(SpriteBatch batch) {

		TextureRegion frame;

		// DEAD
		if (currentState == EnemyState.DEAD || isDying) {
			frame = HunchbackRes.dieAnim.getKeyFrame(stateTime, false);
			drawHelper(batch, frame);
			return;
		}

		// DISAPPEAR
		if (disappearing) {
			frame = noHead ? HunchbackRes.disappearNoHeadAnim.getKeyFrame(stateTime, false)
					: HunchbackRes.disappearAnim.getKeyFrame(stateTime, false);

			drawHelper(batch, frame);
			return;
		}

		// APPEAR
		if (invulnerable && appearTimer < 0.3f) {
			frame = HunchbackRes.appearAnim.getKeyFrame(stateTime, false);
			drawHelper(batch, frame);
			return;
		}

		// NORMAL ANIMATIONS
		if (currentState == EnemyState.ATTACKING) {
			frame = HunchbackRes.attackHighAnim.getKeyFrame(stateTime, false);
		} else {
			frame = HunchbackRes.walkAnim.getKeyFrame(stateTime, true);
		}

		drawHelper(batch, frame);
	}

	public void triggerDeath() {
		this.currentState = EnemyState.DEAD;
		this.isDying = true;
		this.stateTime = 0;
		this.velocityY = 400f;
		this.hurtbox.set(0, 0, 0, 0);
	}

	@Override
	public void flee() {
		this.active = false;
	}

	@Override
	public Rectangle getHitBox() {
		return null;
	}

	@Override
	public EnemyState getState() {
		return currentState;
	}

	@Override
	public void setState(EnemyState s) {
		this.currentState = s;
	}

	@Override
	public int getHitScoreValue() {
		return 100;
	}

	@Override
	public int getDieScoreValue() {
		return isClone ? 50 : 5000;
	}
}