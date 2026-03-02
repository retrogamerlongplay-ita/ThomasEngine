package it.mpace.thomas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.GripperRes;

public class Gripper extends Enemy {

    // RIMOSSE: position, hurtbox, active, facingRight, stateTime, velocityY
    // perché sono già in Enemy!

    public boolean isGrabbedFromRight;
    private final float APPROACH_DISTANCE = 100;

    public enum GripperState {
        WALKING, APPROACHING, GRABBING, DYING, FLEEING
    }

    public GripperState state = GripperState.WALKING;

    public Gripper(float x, float y) {
        super(x, y,1); // Chiama il costruttore di Enemy che inizializza position e hurtbox
        this.speed = GameControlRes.GRIPPER_SPEED;
    }

    @Override
    public void update(float deltaTime, Player player) {
    	// Se lo stato è DYING, esegui solo la fisica di caduta e ESCI
        if (state == GripperState.DYING||this.isDying) {
            updateDyingPhysics(deltaTime);
            return; 
        }

        stateTime += deltaTime;

        if (state == GripperState.GRABBING) {
            // Posizionamento rigido su Thomas
            if (isGrabbedFromRight) {
                position.x = player.position.x + 15;
                facingRight = false;
            } else {
                position.x = player.position.x - 10;
                facingRight = true;
            }
            position.y = player.position.y;
        } else if (state == GripperState.FLEEING) {
            // Scappa nella direzione opposta a Thomas o semplicemente verso l'uscita più vicina
            float fleeDir = (position.x > player.position.x) ? 1 : -1;
            facingRight = (fleeDir > 0);
            position.x += (speed * 1.5f) * fleeDir * deltaTime; // Scappa più veloce del normale!
            
            // Se esce dallo schermo, disattivalo
            if (Math.abs(position.x - player.position.x) > 300) active = false;
            return; // Interrompe il resto della logica di inseguimento
        }else {
            // IA di movimento
            float playerDistance = Math.abs(position.x - player.position.x);
            if (playerDistance < APPROACH_DISTANCE) {
                state = GripperState.APPROACHING;
            } else {
                state = GripperState.WALKING;
            }

            if (position.x < player.position.x) {
                position.x += speed * deltaTime;
                facingRight = true;
            } else {
                position.x -= speed * deltaTime;
                facingRight = false;
            }
        }
        
        // Aggiorna la hurtbox ereditata (centrata su position.x)
        float bodyWidth = 20; 
        hurtbox.set(position.x - (bodyWidth / 2), position.y, bodyWidth, 65);
    }

    @Override
    public void hit(Player p) {
        super.hit(p); // Imposta isDying = true e disabilita hurtbox
        this.state = GripperState.DYING; // <--- Forza lo stato interno
        this.stateTime = 0; // Reset timer per l'animazione di morte
    }

    @Override
    public void draw(SpriteBatch batch) {
        TextureRegion currentFrame;
        if (isDying) {
            currentFrame = GripperRes.dieAnim.getKeyFrame(stateTime);
        } else {
            switch (state) {
                case GRABBING: currentFrame = GripperRes.grabAnim.getKeyFrame(stateTime); break;
                case APPROACHING: currentFrame = GripperRes.approachAnim.getKeyFrame(stateTime); break;
                default: currentFrame = GripperRes.walkAnim.getKeyFrame(stateTime); break;
            }
        }
        
        // Usa l'helper di Enemy per disegnare (gestisce già il flip e il pivot)
        drawHelper(batch, currentFrame);
    }

    @Override
    public boolean canGrabPlayer() {
        return !isDying && state != GripperState.GRABBING;
    }

	@Override
	public void flee() {
		this.state=GripperState.FLEEING;
		
	}
    
    
}
