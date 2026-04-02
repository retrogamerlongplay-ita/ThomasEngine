package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.TomTomRes;
import it.mpace.thomas.screen.LevelScreen;

public class TomTom extends GrabbingEnemy {

    private float jumpTimer = 0;
    private final float JUMP_COOLDOWN = 2.0f;
    private final float GRAVITY = 800f;
    private float groundY;
    public EnemyState currentState = EnemyState.WALKING;
    private LevelScreen level;
    private Rectangle hitbox = new Rectangle(0, 0, 0, 0);
   
    public TomTom(float x, float y, LevelScreen screen) {
        super(x, y);
        this.level = screen;
        this.groundY = y;
        this.speed = 70f; // Più veloce del Gripper
        currentState = EnemyState.WALKING;
        // Hurtbox molto bassa e stretta
        this.hurtbox = new Rectangle(x, y, 15, 30);
    }

    @Override
	public int getHitScoreValue() {
		return this.getDieScoreValue();
	}

	@Override
	public int getDieScoreValue() {
		return 200;
	}

	@Override
    public void update(float dt, Player player) {
		//float playerDistance = Math.abs(position.x - player.position.x);
        if (isDying) {
            updateDyingPhysics(dt);
            return;
        }
        stateTime += dt;
        jumpTimer += dt;        
        // 1. LOGICA DI GRABBING (Presa alle gambe)
        if (currentState == EnemyState.GRABBING && player.currentState != Player.State.DEAD) {
            // Segue le gambe del giocatore (offset basso)
            float grabOffset = (position.x > player.position.x) ? 10 : -10;
            position.x = player.position.x + grabOffset;
            position.y = player.position.y;
            facingRight = (position.x < player.position.x);
            
            // Drenaggio energia (leggermente meno dei Gripper perché sono piccoli)
            GameControlRes.decrementEnergy(8f * dt);
            return; // Salta il resto del movimento
        }
        // Se Thomas si libera o muore, il TomTom molla la presa
        if (currentState == EnemyState.GRABBING && player.currentState != Player.State.GRABBED) {
            currentState = EnemyState.WALKING;
            position.x += (facingRight ? -15 : 15); // Spintarella per distacco
        }
        // 2. LOGICA DI MOVIMENTO E SALTO
        if (currentState == EnemyState.WALKING) {
            facingRight = (position.x < player.position.x);
            float dir = facingRight ? 1 : -1;
            position.x += speed * dir * dt;
            float dist = Math.abs(position.x - player.position.x);
            // DECISIONE: Salta o Afferra?
            if (dist < 12f && player.currentState != Player.State.GRABBED && player.currentState != Player.State.DEAD) {
                // AFFERRATO!
                currentState = EnemyState.GRABBING;
                player.currentState = Player.State.GRABBED;
            } else if (dist < 80f && jumpTimer > JUMP_COOLDOWN && player.currentState != Player.State.GRABBED) {
                // SALTO ACROBATICO
                currentState = EnemyState.ATTACKING; 
                velocityY = 250f;
                jumpTimer = 0;
            }
        } 
        
        // 2. FISICA DEL SALTO
        if (currentState == EnemyState.ATTACKING) {
            position.y += velocityY * dt;
            velocityY -= GRAVITY * dt;
            // Spinta orizzontale durante il salto
            float jumpDir = facingRight ? 1 : -1;
            position.x += (speed * 1.2f) * jumpDir * dt;
            if (position.y <= groundY) {
                position.y = groundY;
                velocityY = 0;
                currentState = EnemyState.WALKING;
            }
        }
        // Aggiorna Hurtbox (sempre bassa)
        hurtbox.set(position.x - 7, position.y, 14, 25);
    }
    
    @Override
 	public void setState(EnemyState newState) {
 		this.currentState = newState;
 		
 	}

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion frame;
        if (isDying) {
            frame = TomTomRes.dieAnim.getKeyFrame(stateTime);
        } else if (currentState == EnemyState.ATTACKING) {
            frame = TomTomRes.jumpAnim.getKeyFrame(stateTime);
        }else if (currentState == EnemyState.GRABBING) {
            frame = TomTomRes.grabAnim.getKeyFrame(stateTime);
        } else {
            frame = TomTomRes.walkAnim.getKeyFrame(stateTime, true);
        }
        drawHelper(batch, frame);
    }

    @Override
    public void flee() {
        this.speed *= 1.5f;
        // Scappa via se il boss è attivo
    }

    @Override
    public Rectangle getHitBox() { return this.hitbox; }

    @Override
    public EnemyState getState() { return currentState; }
}
