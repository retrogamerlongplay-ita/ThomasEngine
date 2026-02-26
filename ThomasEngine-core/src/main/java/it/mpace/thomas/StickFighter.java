package it.mpace.thomas;


import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.StickFighterRes;

public class StickFighter extends Enemy {
    
    // Stati del Boss
    public enum State { WAITING, WALKING, ATTACKING_HIGH, HURT_HIGH, DEAD }
    private State currentState = State.WAITING;

    private float stateTime = 0;
    private int health = 10; // Richiede più colpi dei nemici base
    private float attackRange = 70f; // Il raggio del bastone
    private float attackCooldown = 1.5f;
    private float lastAttackTime = 0;
    private boolean movingRight = false; // Direzione del pattugliamento

    public StickFighter(float x, float y) {
        super(x, y, 10);
        this.speed = GameControlRes.STICK_FIGHTER_SPEED; 
        this.hurtbox = new Rectangle(x, y, 32, 64); // Dimensioni standard libGDX
        currentState = State.WAITING;
    }
    
    public State getState() {
    	return this.currentState;
    }

    public void update(float delta, Vector2 playerPos) {
        stateTime += delta;
        float distanceToPlayer = Math.abs(playerPos.x - position.x);

        switch (currentState) {
        	case WAITING:
        	    
        	    // 1. Logica di inversione marcia ai bordi
        	    if (position.x <= LevelConstants.FIRST_FLOOR_LEFT_STAIR) {
        	        movingRight = true;
        	        facingRight = true;
        	    } else if (position.x >= LevelConstants.FIRST_FLOOR_BOSS_ICON) {
        	        movingRight = false;
        	        facingRight = false;
        	    }

        	    // 2. Movimento basato sulla direzione attuale
        	    if (movingRight) {
        	        position.x += speed * delta;
        	    } else {
        	        position.x -= speed * delta;
        	    }
        	    
        	    // 3. Transizione allo stato WALKING (Combattimento)
        	    // Se Thomas si avvicina troppo, il boss smette di pattugliare e lo punta
        	    if (Math.abs(playerPos.x - position.x) < 150f) {
        	    	currentState = State.WALKING; 
        	    }
            case WALKING:
                // Si avvicina al giocatore ma mantiene la distanza del bastone
                if (distanceToPlayer > attackRange - 10) {
                    moveTowardsPlayer(playerPos.x, delta);
                } else {
                    attemptAttack();
                }
                break;

            case ATTACKING_HIGH:
                if (stateTime > 0.5f) { // Durata animazione attacco
                    currentState = State.WALKING;
                    stateTime = 0;
                }
                break;
        }
        
        hurtbox.setPosition(position.x, position.y);
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

    private void attemptAttack() {
        if (stateTime - lastAttackTime > attackCooldown) {
            currentState = State.ATTACKING_HIGH;
            lastAttackTime = stateTime;
            // Qui andrebbe inserita la logica per danneggiare il giocatore
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) currentState = State.DEAD;
    }
    
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;

        // Selezione dell'animazione in base allo stato
        switch (currentState) {
            case ATTACKING_HIGH:
                currentFrame = (TextureRegion) StickFighterRes.attackHighAnim.getKeyFrame(stateTime, false);
                break;
            case HURT_HIGH:
                currentFrame = (TextureRegion) StickFighterRes.hurtHigh;
                break;
            case DEAD:
                currentFrame = (TextureRegion) StickFighterRes.dieAnim.getKeyFrame(stateTime, false);
                break;
            case WALKING:
            default:
                currentFrame = (TextureRegion) StickFighterRes.walkAnim.getKeyFrame(stateTime, true);
                break;
        }

        // Gestione del flip (orientamento) dello sprite
        // Se lo sprite originale guarda a destra e facingRight è false, flippiamo
        if (!facingRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (facingRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }

        batch.draw(currentFrame, position.x, position.y);
    }

	@Override
	public void flee() {
		System.out.println("I DON'T FLEE I AM THE BOSS.");
		
	}
    
    
}
