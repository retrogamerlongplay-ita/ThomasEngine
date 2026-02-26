package it.mpace.thomas;

import com.badlogic.gdx.Game;

import it.mpace.thomas.res.GripperRes;
import it.mpace.thomas.res.KnifeThrowerRes;
import it.mpace.thomas.res.PlayerRes;
import it.mpace.thomas.res.StickFighterRes;

public class ThomasMain extends Game {
	@Override
	public void create() {
		ThomasCredits.printout();
		PlayerRes.load();
		GripperRes.load();
		KnifeThrowerRes.load();
		StickFighterRes.load();
		setScreen(new LevelScreen());
		
	}
}