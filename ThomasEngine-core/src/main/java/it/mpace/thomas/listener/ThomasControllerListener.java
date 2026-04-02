package it.mpace.thomas.listener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

public class ThomasControllerListener implements ControllerListener{
	
	
	    @Override
	    public void connected(Controller controller) {
	        Gdx.app.log("CONTROLLER", "Connesso: " + controller.getName());
	    }

	    @Override
	    public void disconnected(Controller controller) {
	        Gdx.app.log("CONTROLLER", "Disconnesso");
	    }

	    @Override
	    public boolean buttonDown(Controller controller, int buttonCode) {
	        // Esempio: Salto (spesso il tasto 0 o mapping specifico)
	        if (buttonCode == controller.getMapping().buttonA) {
	           // jump();
	        }
	        return true;
	    }
	    
	    

	    @Override
		public boolean buttonUp(Controller controller, int buttonCode) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
	    public boolean axisMoved(Controller controller, int axisCode, float value) {
	        // value va da -1.0 a 1.0. Applica una "deadzone" per evitare movimenti fantasma.
	        if (Math.abs(value) > 0.15f) {
	            // Gestisci movimento analogico
	        }
	        return true;
	    }
	


}
