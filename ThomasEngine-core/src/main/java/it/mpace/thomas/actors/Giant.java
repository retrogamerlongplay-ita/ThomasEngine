package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.res.GiantRes;
import it.mpace.thomas.screen.LevelScreen;

public class Giant extends Enemy {

    private final float HURT_DURATION = 0.25f;

    private EnemyState currentState = EnemyState.WAITING;

    private float stateTime = 0;
    
    private float attackRange = 90f; // Più lungo
    private float attackCooldown = 1.2f; // Attacchi più lenti
    private float lastAttackTime = -1;
    private boolean movingRight = false;
    public Rectangle attackHitbox = new Rectangle(0, 0, 0, 0);

    public Giant(float x, float y) {
        super(x, y, 15); // l'hp del genitore è sovrascritto localmente
        this.speed = LevelConstants.GIANT_SPEED; // leggermente più lento
        this.hurtbox = new Rectangle(x - 20, y, 60, 96); // hitbox grande
       
        currentState = EnemyState.WAITING;
    }

    @Override
    public EnemyState getState() {
        return this.currentState;
    }

    @Override
    public void hit(Player player, LevelScreen level) {
        // First apply base logic (damage, effects). super.hit may set isDying and reduce hp.
        super.hit(player, level);

        // If the base class reduced HP to zero, ensure we trigger death sequence which sets currentState=DEAD
        if (hp <= 0) {
            triggerDeath();
            return;
        }

        // If already dying or dead (but hp > 0) bail out
        if (isDying || currentState == EnemyState.DEAD) return;

        // Regular hurt reaction
        stateTime = 0;
        if (player.currentState == Player.State.PUNCHING_CROUCH || player.currentState == Player.State.KICKING_CROUCH) {
            currentState = EnemyState.HURT_LOW;
        } else {
            currentState = EnemyState.HURT_HIGH;
        }
 
        float pushDir = facingRight ? -20 : 20;
        position.x += pushDir;
    }

    @Override
    public int getHitScoreValue() {
        return 100;
    }

    @Override
    public int getDieScoreValue() {
        return 2500;
    }

    @Override
    public Rectangle getHitBox() {
        return this.attackHitbox;
    }

    public void triggerDeath() {
        this.currentState = EnemyState.DEAD;
        this.isDying = true;
        this.stateTime = 0;
        this.velocityY = 400f;
        this.hurtbox.set(0, 0, 0, 0);
        System.out.println("GIANT DEFEATED!");
    }

    @Override
    public void setState(EnemyState newState) {
        // Avoid changing state if the Giant is already dying/dead
        if (this.isDying || this.currentState == EnemyState.DEAD) {
            return;
        }
        this.currentState = newState;
    }

    @Override
    public void update(float delta, Player player) {
        // Defensive: if HP dropped to <= 0 by any code path, ensure we trigger death state
        if (hp <= 0 && !isDying && currentState != EnemyState.DEAD) {
            triggerDeath();
        }
        if (currentState == EnemyState.DEAD || isDying) {
            updateDyingPhysics(delta);
            return;
        }

        stateTime += delta;
        float distanceToPlayer = Math.abs(player.position.x - position.x);
        attackHitbox.set(0, 0, 0, 0);

        if (currentState == EnemyState.HURT_HIGH || currentState == EnemyState.HURT_LOW || currentState == EnemyState.HURT_MID) {
            if (stateTime >= HURT_DURATION) {
                currentState = EnemyState.WALKING;
                stateTime = 0;
                lastAttackTime = 0;
            }
            hurtbox.set(position.x - 30, position.y, 80, 96);
            return;
        }

        // Limitazioni di posizione (esempio generico: non oltrepassare il bordo sinistro) - usa costanti del livello
        if (position.x < LevelConstants.THIRD_FLOOR_LEFT_STAIR + 20) {
            position.x = LevelConstants.THIRD_FLOOR_LEFT_STAIR + 20;
            if (currentState == EnemyState.WAITING) {
                movingRight = true;
                facingRight = true;
            }
        }

        switch (currentState) {
            case WAITING:
                if (position.x <= LevelConstants.THIRD_FLOOR_LEFT_STAIR) {
                    movingRight = true;
                    facingRight = true;
                } else if (position.x >= LevelConstants.THIRD_FLOOR_BOSS_ICON) {
                    movingRight = false;
                    facingRight = false;
                }

                if (movingRight) position.x += speed * delta; else position.x -= speed * delta;

                if (distanceToPlayer < attackRange) {
                    currentState = EnemyState.WALKING;
                }
                break;

            case WALKING:
                float dist = Math.abs(player.position.x - position.x);
                float idealDist = attackRange + 10;

                if (dist > idealDist + 20) {
                    moveTowardsPlayer(player.position.x, delta);
                } else if (dist < idealDist - 10) {
                    float retreatDir = (position.x > player.position.x) ? 1 : -1;
                    position.x += speed * 0.9f * retreatDir * delta;
                    facingRight = (player.position.x > position.x);
                } else {
                    attemptAttack(player);
                    position.x += (MathUtils.sin(stateTime * 2) * 5f) * delta;
                }

                if (distanceToPlayer < attackRange + 30) attemptAttack(player);
                break;

            case ATTACKING_HIGH:
            case ATTACKING_MID:
                if (stateTime > 0.2f && stateTime < 0.5f) {
                    float w = 30f;
                    float h = 18f;
                    if (facingRight) {
                        attackHitbox.set(position.x, position.y + 50, w, h);
                    } else {
                        attackHitbox.set(position.x - w + 40, position.y + 60, w, h);
                    }
                }
                if (stateTime > 0.7f) {
                    currentState = EnemyState.WALKING;
                    stateTime = 0;
                }
                break;
            case ATTACKING_LOW:
                if (stateTime > 0.15f && stateTime < 0.45f) {
                    float w = 30f;
                    float h = 18f;
                    if (facingRight) {
                        attackHitbox.set(position.x, position.y + 30, w, h);
                    } else {
                        attackHitbox.set(position.x - w + 40, position.y + 20, w, h);
                    }
                }
                if (stateTime > 0.8f) {
                    currentState = EnemyState.WALKING;
                    stateTime = 0;
                }
                break;

            default:
                break;
        }

        float bodyHeight = (currentState == EnemyState.ATTACKING_LOW) ? 80 : 96;
        hurtbox.set(position.x - 10, position.y, 25, bodyHeight);
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
        if (isDying || currentState == EnemyState.DEAD || currentState == EnemyState.HURT_HIGH || currentState == EnemyState.HURT_LOW) {
            return;
        }

        float distanceToWall = position.x - (LevelConstants.FIRST_FLOOR_LEFT_STAIR + 20);
        float dynamicCooldown = (distanceToWall < 40f) ? attackCooldown / 2f : attackCooldown;

        if (stateTime - lastAttackTime > dynamicCooldown) {
            if (player.currentState == Player.State.CROUCHING) {
                currentState = EnemyState.ATTACKING_LOW;
            } else if (player.currentState == Player.State.JUMPING) {
                currentState = EnemyState.ATTACKING_HIGH;
            } else {
                currentState = (MathUtils.randomBoolean()) ? EnemyState.ATTACKING_MID : EnemyState.ATTACKING_HIGH;
            }

            if (distanceToWall < 30f && MathUtils.random() > 0.7f) {
                currentState = EnemyState.ATTACKING_MID;
            }

            lastAttackTime = Math.min(dynamicCooldown / 2, stateTime);
            stateTime = 0;
        }
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;

        // Defensive: if we're in dying animation, prefer dieAnim even if the state wasn't updated yet
        if (this.isDying || this.currentState == EnemyState.DEAD) {
            currentFrame = (TextureRegion) GiantRes.dieAnim.getKeyFrame(stateTime, false);
        } else {
            switch (currentState) {
                case ATTACKING_HIGH:
                    currentFrame = (TextureRegion) GiantRes.punchAnim.getKeyFrame(stateTime, false);
                    break;
                case ATTACKING_MID:
                    currentFrame = (TextureRegion) GiantRes.punchAnim.getKeyFrame(stateTime, false);
                    break;
                case ATTACKING_LOW:
                    currentFrame = (TextureRegion) GiantRes.kickAnim.getKeyFrame(stateTime, false);
                    break;
                case HURT_HIGH:
                    currentFrame = (TextureRegion) GiantRes.punchAnim.getKeyFrame(0, false);
                    break;
                case DEAD:
                    currentFrame = (TextureRegion) GiantRes.dieAnim.getKeyFrame(stateTime, false);
                    break;
                case WAITING:
                    currentFrame = (TextureRegion) GiantRes.walkAnim.getKeyFrame(0, false);
                case WALKING:
                default:
                    currentFrame = (TextureRegion) GiantRes.walkAnim.getKeyFrame(stateTime, true);
                    break;
            }
        }

        // Gestione flip
        if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        // Usa drawHelper per centrare
        drawHelper(batch, currentFrame);
    }

    @Override
    public void flee() {
        // Il Giant può scappare: implementazione semplice
       System.out.println("GIANT NOT FLEEING!");
    }
}