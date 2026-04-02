package it.mpace.thomas.actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.SnakeRes;
import it.mpace.thomas.screen.LevelScreen;

public class Snake extends FallingHazard {

    private float moveSpeed = 100f; // Più veloce del Gripper medio
   
    
    private float moveDirection = 0; // Direzione fissa decisa allo spawn
    private boolean hasHitPlayer = false; // Impedisce danni multipli dallo stesso serpente
    
    private Rectangle hitbox = new Rectangle(0, 0, 0, 0);

    public Snake(float x, float y, float groundY,LevelScreen level) {
        // Usiamo il frame del vaso che cade definito in SnakeRes
        super(x, y, groundY, SnakeRes.potFallingFrame, level);
        this.hp = 1; // Un colpo solo e muore
    }
    
    @Override
    public void hit(Player p, LevelScreen level) {
        super.hit(p, level); // Sottrae HP, imposta isDying = true, genera score
        this.stateTime = 0;  // Resetta il tempo per far partire l'animazione di esplosione
    }

    @Override
    protected void handleImpact() {
        // Quando il vaso tocca terra, passiamo allo stato IMPACT
        // e iniziamo l'animazione dei cocci che si rompono
        this.hazardState = HazardState.IMPACT;
        AudioRes.playSound(AudioRes.potCrushSound);
        this.stateTime = 0;
    }

    @Override
    protected boolean isImpactAnimationFinished() {
        // Controlliamo se l'animazione del vaso rotto è finita
        return SnakeRes.potCrashingAnim.isAnimationFinished(stateTime);
    }
    
    

    @Override
	public int getHitScoreValue() {
		return 200;
	}

	@Override
	public int getDieScoreValue() {
		return 200;
	}

	@Override
    protected void spawnInnerEntity() {
        // DECISIONE DIREZIONE: Solo in questo istante puntiamo il Player
        // Usiamo lo screen per recuperare la posizione di Thomas
        float playerX = this.level.player.position.x;
        
        facingRight = (position.x < playerX);
        moveDirection = facingRight ? 1 : -1;
        
        this.hazardState = HazardState.ACTIVE;
        this.stateTime = 0;
    }

    @Override
    protected void updateActiveLogic(float dt, Player player) {
        // PROSEGUE SEMPRE DRITTO (Non si ferma mai, non si volta mai)
        position.x += moveSpeed * moveDirection * dt;

        // COLLISIONE CON THOMAS
        if (!hasHitPlayer && this.hurtbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
            // Infligge il danno
            player.takeHit(10f, this.level, position.x, position.y);
            AudioRes.playSound(AudioRes.snakeHitSound);
            // SEGNA COME COLPITO: Lo Snake ora ignorerà Thomas e continuerà a strisciare
            hasHitPlayer = true;
            
            // Opzionale: azzeriamo la hurtbox così Thomas non può più colpirlo (o viceversa)
            // hurtbox.set(0,0,0,0); 
        }

        // DISATTIVAZIONE: Solo quando è lontano (fuori schermo)
        if (Math.abs(position.x - player.position.x) > 250) {
            this.active = false;
        }
    }
    
    



    @Override
	public void setState(EnemyState newState) {
		//empty only for enemies, hazards don't have states like this
		
	}

	@Override
    protected void updateHurtbox() {
        if (hazardState == HazardState.FALLING) {
            hurtbox.set(position.x - 8, position.y, 16, 16);
        } else if (hazardState == HazardState.ACTIVE) {
            // Se ha già colpito, possiamo rimpicciolire la hurtbox per farlo sfilare via
            if (hasHitPlayer) {
                hurtbox.set(0, 0, 0, 0); 
            } else {
                hurtbox.set(position.x - 8, position.y, 16, 10);
            }
        }
    }
	
	
	@Override
	public void update(float dt, Player player) {
		super.update(dt, player);
	    if (this.isDying) {
	        stateTime += dt;
	        // Opzionale: blocca la caduta mentre esplode per un effetto più arcade
	        velocityY = 0; 
	       
	        // Se l'animazione di esplosione è finita, rimuovi l'oggetto
	        if (SnakeRes.explodingAnim.isAnimationFinished(stateTime)) {
	            this.active = false;
	        }
	        return; 
	    }
	}
	
	@Override
	protected Animation<TextureRegion> getExplodingAnimation() {
	    return SnakeRes.explodingAnim;
	}

    @Override
    protected boolean handleFallingPlayerCollision(Player player) {
        // If the falling pot hits the player before spawning the snake, explode and damage player
        if (this.hurtbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
            // Apply damage
            player.takeHit(15f, this.level, position.x, position.y);
            // Trigger explosion visual and removal (don't spawn inner snake)
            this.isDying = true;
            this.hazardState = HazardState.EXPLODING;
            this.stateTime = 0;
            this.velocityY = 0;
        }
        return true; // handled
    }

    @Override
    protected void drawSpecificHazard(SpriteBatch batch) {
        TextureRegion frame;
        if (isDying) {
            // Se Thomas ha colpito il vaso, usiamo l'esplosione
        	System.out.println("Disegnando esplosione frame: ["+stateTime+"]" + SnakeRes.explodingAnim.getKeyFrameIndex(stateTime));
            frame = SnakeRes.explodingAnim.getKeyFrame(stateTime, false);
        }else if (hazardState == HazardState.IMPACT) {
            frame = SnakeRes.potCrashingAnim.getKeyFrame(stateTime, false);
        } else if(hazardState == HazardState.ACTIVE) {
            // Stato ACTIVE: il serpente striscia
            frame = SnakeRes.snakeWallkingAnim.getKeyFrame(stateTime, true);
        }else {
        				// Stato FALLING: il vaso che cade
			frame = SnakeRes.potFallingFrame;
        }
        
        drawHelper(batch, frame);
    }

    @Override
    public void flee() {
        // I serpenti non scappano, continuano a strisciare!
    	this.active = false;
    }

    @Override
    public Rectangle getHitBox() {
        return this.hitbox; // Lo Snake usa la hurtbox per il danno al contatto
    }

    @Override
    public EnemyState getState() {
        return EnemyState.WALKING;
    }
}