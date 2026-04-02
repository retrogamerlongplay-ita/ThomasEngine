package it.mpace.thomas;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;

import it.mpace.thomas.actors.Player;
import it.mpace.thomas.actors.Player.State;
import it.mpace.thomas.res.GameControlRes;

public class GameInput extends InputAdapter {
    private Player player;

    public GameInput(Player player) {
        this.player = player;
    }

    @Override
    public boolean keyDown(int keycode) {
     		if (keycode==Keys.D) {
     			System.out.println("D pressed");
     			//GameControlRes.debugMode = true;
     			GameControlRes.toggleDebug();
     			System.out.println("DEBUG MODE: [" + GameControlRes.debugMode+"]");
     			return true;
     		}
     		if (keycode==Keys.P) {
     			System.out.println("POSITION (" + this.player.position.x + "," + this.player.position.y + ")");
     			return true;
     		}
     		if (keycode==Keys.M) {
     	        GameControlRes.toggleMute();
     	        System.out.println("IS MUTED [" + GameControlRes.isMuted + "]");
     	        return true;
     	    }
     		if (keycode==Keys.K) {
     	        
     	        System.out.println("Kill All");
     	        this.player.killAllEnemies();
     	        return true;
     	    }
     		if (keycode==Keys.F) {
     	        GameControlRes.toggleFullscreen();
     	        return true;
     	    }

        // ATTACCHI (Eventi Singoli per evitare "spam" infinito)
        if (player.currentState != Player.State.GRABBED && player.currentState != Player.State.DEAD) {
            if (keycode == Keys.Z) { // Esempio: Z per Pugno
                player.handlePunch(); // punch
                return true;
            }
            if (keycode == Keys.X) { // Esempio: X per Calcio
                player.handleKick(); // kick
                return true;
            }
        }
        
    	if (keycode==Keys.DOWN && player.currentState != State.JUMPING) {
			player.handleCrouch();
			return true;
		}
    	 if (keycode == Keys.UP) {
    	        player.handleJump();
    	        return true;
    	    }
        return false;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Keys.DOWN) {
            player.handleStandUp();
            return true;
        }
        return false;
    }
}
