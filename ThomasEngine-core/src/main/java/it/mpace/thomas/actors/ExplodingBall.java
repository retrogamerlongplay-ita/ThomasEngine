package it.mpace.thomas.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.ExplodingBallRes;
import it.mpace.thomas.screen.LevelScreen;
import it.mpace.thomas.sprite.PotProjectile; // NEW

public class ExplodingBall extends FallingHazard {

    private float shakeTimer = 0;
    private final float SHAKE_DURATION = 3.0f; // Tempo di "preavviso" prima dello scoppio
    private boolean hasExploded = false;
    private Rectangle explosionHitbox = new Rectangle(0, 0, 0, 0);
    private LevelScreen level;
    
 // Altezza specifica per la palla (altezza testa/salto)
    private float stopY; 

    public ExplodingBall(float x, float y, float groundY,LevelScreen level) {
        super(x, y, groundY, ExplodingBallRes.idleFrame, level);
        this.stopY = groundY + 40f; 
        this.hp = 1; // Può essere distrutta prima che esploda
        this.level = level;
    }
    
    
    
    @Override
	public int getHitScoreValue() {
		return this.getDieScoreValue();
	}
    
    @Override
	protected Animation<TextureRegion> getExplodingAnimation() {
	    return ExplodingBallRes.explodingAnim;
	}



	@Override
	public int getDieScoreValue() {
		return 1000;
	}



	@Override
	public void setState(EnemyState newState) {
		//empty only for enemies, hazards don't have states like this
		
	}

    @Override
    protected void handleImpact() {
        // Invece di rompersi come un vaso, inizia a scuotersi (shaking)
        this.hazardState = HazardState.IMPACT;
        this.stateTime = 0;
        this.shakeTimer = 0;
    }

    @Override
    protected boolean isImpactAnimationFinished() {
        // IMPORTANTE: Dobbiamo incrementare il timer qui perché FallingHazard 
        // non chiama updateActiveLogic durante lo stato IMPACT
        shakeTimer += Gdx.graphics.getDeltaTime(); 
        // Ritorna true solo quando il timer raggiunge la durata stabilita
        return shakeTimer >= SHAKE_DURATION;
    }

    @Override
    protected void spawnInnerEntity() {
        hasExploded = true;
        AudioRes.playSound(AudioRes.potCrushSound);
        stateTime = 0;
        // Hitbox esplosione aerea
        explosionHitbox.set(position.x - 25, position.y - 25, 50, 50);

        // Spawn TWO diagonal fragments: left and right
        if (this.level != null) {
            // velocities chosen for an arcade diagonal downward spread
            float leftVx = -120f;
            float rightVx = 120f;
            float vy = -180f; // initial downward velocity (negative Y for downward)

            PotProjectile left = new PotProjectile(position.x, position.y, leftVx, vy, ExplodingBallRes.leftProjectileFrame);
            PotProjectile right = new PotProjectile(position.x, position.y, rightVx, vy, ExplodingBallRes.rightProjectileFrame);

            this.level.potProjectiles.add(left);
            this.level.potProjectiles.add(right);
        }
    }

    @Override
    protected void updateActiveLogic(float dt, Player player) {
        if (hazardState == HazardState.IMPACT) {
            shakeTimer += dt;
        }

        if (hasExploded) {
            // Se Thomas è nel raggio dell'esplosione
            if (explosionHitbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
                player.takeHit(30f, this.level, position.x, position.y + 20);
            }
            
            // Una volta finita l'animazione dell'esplosione, l'oggetto scompare
            if (ExplodingBallRes.explodingAnim.isAnimationFinished(stateTime)) {
                this.active = false;
            }
        }
    }
    
    
    @Override
    public void update(float dt, Player player) {
    	super.update(dt, player);
    	//System.out.println("Updating ExplodingBall - State: " + hazardState + ", Position: (" + position.x + ", " + position.y + "), VelocityY: " + velocityY);
    	
    	 if (this.isDying) {
    	        updateDyingPhysics(dt);
    	        return; // BLOCCA il resto della logica
    	    }
        stateTime += dt;

        if (hazardState == HazardState.FALLING) {
            velocityY -= gravity * dt;
            position.y += velocityY * dt;

            // La palla si ferma all'altezza stopY invece che a terra
            if (position.y <= stopY) {
                position.y = stopY;
                velocityY = 0;
                handleImpact();
            }
            
            // Collisione con Thomas mentre cade (se lo colpisce in testa)
            if (this.hurtbox.overlaps(player.hurtbox)) {
                player.takeHit(20f, (LevelScreen)com.badlogic.gdx.Gdx.app.getApplicationListener(), position.x, position.y);
                handleImpact();
            }
        } else {
            updateActiveLogic(dt, player);
        }
        updateHurtbox();
    }


    @Override
    protected void updateHurtbox() {
        if (!hasExploded) {
            // Thomas può distruggere la palla colpendola prima che scoppi
            hurtbox.set(position.x - 10, position.y, 20, 20);
        } else {
            // Una volta esplosa, non è più un bersaglio (è solo un effetto di danno)
            hurtbox.set(0, 0, 0, 0);
        }
    }

    @Override
    protected boolean handleFallingPlayerCollision(Player player) {
        // ExplodingBall already handles collision during FALLING in its update method.
        // Signal that the collision was handled so FallingHazard doesn't apply its default behavior.
        if (this.hurtbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
            // Contact explosion: do not spawn projectiles, only create explosion and damage
            this.hasExploded = true;
            this.stateTime = 0;
            // create explosion hitbox centered on the ball
            this.explosionHitbox.set(position.x - 25, position.y - 25, 50, 50);
            player.takeHit(20f, this.level, position.x, position.y);
            // Ensure we don't later go through impact->spawning path
            this.hazardState = HazardState.EXPLODING;
            this.velocityY = 0;
        }
        return true;
    }

    @Override
    protected void drawSpecificHazard(SpriteBatch batch) {
        TextureRegion frame;

        if (hasExploded) {
            frame = ExplodingBallRes.explodingAnim.getKeyFrame(stateTime, false);
        } else if (hazardState == HazardState.IMPACT) {
            frame = ExplodingBallRes.shakingAnim.getKeyFrame(stateTime, true);
        } else {
            frame = ExplodingBallRes.idleFrame;
        }

        drawHelper(batch, frame);
    }

    @Override public void flee() { this.active = false; }
    @Override public Rectangle getHitBox() { return explosionHitbox; }
    @Override public EnemyState getState() { return EnemyState.ATTACKING; }
}