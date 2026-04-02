package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.MrXRes;
import it.mpace.thomas.screen.LevelScreen;

/**
 * MrX boss implementation.
 * Behaviour modeled similarly to other bosses (Giant, BoomerangThrower).
 */
public class MrX extends Enemy {

    private EnemyState currentState = EnemyState.WAITING;
    private float localStateTime = 0f; // per le animazioni locali

    private Rectangle attackHitbox = new Rectangle(0, 0, 0, 0);
    private final float ATTACK_RANGE = 80f;
    private final float ATTACK_COOLDOWN = 1.0f;
    private float lastAttackTime = -10f;

    public MrX(float x, float y) {
        super(x, y, 30); // HP del boss
        this.speed = 30f;
        this.hurtbox = new Rectangle(x - 16, y, 38, 90);
        this.facingRight = false;
    }

    @Override
    public void update(float dt, Player player) {
        // Defensive: trigger death if hp <= 0
        if (hp <= 0 && !isDying && currentState != EnemyState.DEAD) {
            triggerDeath();
        }

        if (isDying || currentState == EnemyState.DEAD) {
            updateDyingPhysics(dt);
            localStateTime += dt;
            return;
        }

        localStateTime += dt;
        stateTime += dt;

        float dist = Math.abs(player.position.x - position.x);

        // Reset attack box each frame
        attackHitbox.set(0, 0, 0, 0);

        // Simple state machine
        switch (currentState) {
            case WAITING:
            case WALKING:
                // Move towards player if far, otherwise attempt to attack
                if (dist > ATTACK_RANGE + 20) {
                    moveTowards(player.position.x, dt);
                } else {
                    // small oscillation
                    position.x += (MathUtils.sin(stateTime * 3) * 10f) * dt;
                    attemptAttack(player);
                }
                break;

            case ATTACKING_HIGH:
                // During attack create hitbox briefly
                if (stateTime > 0.15f && stateTime < 0.45f) {
                    if (facingRight) attackHitbox.set(position.x, position.y + 50, 28, 18);
                    else attackHitbox.set(position.x - 28 + 40, position.y + 60, 28, 18);
                }
                if (stateTime > 0.7f) {
                    currentState = EnemyState.WALKING;
                    stateTime = 0;
                }
                break;

            case ATTACKING_LOW:
                if (stateTime > 0.12f && stateTime < 0.4f) {
                    if (facingRight) attackHitbox.set(position.x, position.y + 30, 28, 16);
                    else attackHitbox.set(position.x - 28 + 40, position.y + 20, 28, 16);
                }
                if (stateTime > 0.8f) {
                    currentState = EnemyState.WALKING;
                    stateTime = 0;
                }
                break;

            case HURT_HIGH:
            case HURT_LOW:
            case HURT_MID:
                if (localStateTime > 0.25f) {
                    currentState = EnemyState.WALKING;
                    localStateTime = 0;
                }
                break;

            default:
                break;
        }

        // Keep MrX within boss area (safeguard)
        if (position.x > LevelConstants.THIRD_FLOOR_BOSS_ICON) {
            position.x = LevelConstants.THIRD_FLOOR_BOSS_ICON - 1;
            facingRight = false;
        }
        if (position.x < LevelConstants.THIRD_FLOOR_LEFT_STAIR) {
            position.x = LevelConstants.THIRD_FLOOR_LEFT_STAIR + 1;
            facingRight = true;
        }

        // update hurtbox based on state
        float bodyHeight = (currentState == EnemyState.ATTACKING_LOW) ? 80 : 90;
        hurtbox.set(position.x - 10, position.y, 25, bodyHeight);
    }

    private void moveTowards(float x, float dt) {
        if (x < position.x) {
            position.x -= speed * dt;
            facingRight = false;
        } else {
            position.x += speed * dt;
            facingRight = true;
        }
    }

    private void attemptAttack(Player player) {
        if (isDying || currentState == EnemyState.DEAD) return;
        float distanceToPlayer = Math.abs(player.position.x - position.x);
        float dynamicCooldown = ATTACK_COOLDOWN;
        if (stateTime - lastAttackTime > dynamicCooldown) {
            if (player.currentState == Player.State.CROUCHING) {
                currentState = EnemyState.ATTACKING_LOW;
            } else if (player.currentState == Player.State.JUMPING) {
                currentState = EnemyState.ATTACKING_HIGH;
            } else {
                currentState = MathUtils.randomBoolean() ? EnemyState.ATTACKING_HIGH : EnemyState.ATTACKING_LOW;
            }
            lastAttackTime = stateTime;
            stateTime = 0;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion frame;
        if (isDying) {
            frame = MrXRes.dieAnim.getKeyFrame(localStateTime);
        } else {
            switch (currentState) {
                case ATTACKING_HIGH:
                    frame = MrXRes.punchAnim.getKeyFrame(stateTime);
                    break;
                case ATTACKING_LOW:
                    frame = MrXRes.kickCrouchAnim.getKeyFrame(stateTime);
                    break;
                case WAITING:
                	
                	frame = MrXRes.guardAnim.getKeyFrame(stateTime, true);
                case WALKING:
                default:
                    frame = MrXRes.walkAnim.getKeyFrame(stateTime, true);
                    break;
            }
        }
        drawHelper(batch, frame);
    }

    @Override
    public void flee() {
        this.active = false;
    }

    @Override
    public Rectangle getHitBox() {
        return this.attackHitbox;
    }

    @Override
    public EnemyState getState() {
        return this.currentState;
    }

    @Override
    public void setState(EnemyState newState) {
        if (this.isDying || this.currentState == EnemyState.DEAD) return;
        this.currentState = newState;
    }

    @Override
    public int getHitScoreValue() {
        return 500;
    }

    @Override
    public int getDieScoreValue() {
        return 10000;
    }

    @Override
    public void hit(Player p, LevelScreen level) {
        super.hit(p, level);
        if (hp <= 0) {
            triggerDeath();
        } else {
            // reaction based on attack type
            if (p.currentState == Player.State.CROUCHING || p.currentState == Player.State.KICKING_CROUCH
                    || p.currentState == Player.State.PUNCHING_CROUCH) {
                currentState = EnemyState.HURT_LOW;
            } else if (p.currentState == Player.State.JUMPING) {
                currentState = EnemyState.HURT_HIGH;
            } else {
                currentState = EnemyState.HURT_MID;
            }
            localStateTime = 0;
            stateTime = 0;
            float push = facingRight ? -30 : 30;
            position.x += push;
        }
    }

    public void triggerDeath() {
        this.currentState = EnemyState.DEAD;
        this.isDying = true;
        this.stateTime = 0;
        this.localStateTime = 0;
        this.velocityY = 500f;
        this.hurtbox.set(0, 0, 0, 0);
        System.out.println("MRX DEFEATED!");
    }
}
