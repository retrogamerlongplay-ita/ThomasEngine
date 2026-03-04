package it.mpace.thomas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GripperRes;
import it.mpace.thomas.res.KnifeThrowerRes;
import it.mpace.thomas.res.PlayerRes;
import it.mpace.thomas.res.StickFighterRes;

public class ThomasMain extends Game {

	public SpriteBatch batch;
	public BitmapFont font;

	@Override
	public void create() {
		ThomasCredits.printout();
		batch = new SpriteBatch();
		font = new BitmapFont(); // Carica il font di default
		PlayerRes.load();
		GripperRes.load();
		KnifeThrowerRes.load();
		StickFighterRes.load();
		AudioRes.load();
		setScreen(new MainMenuScreen(this));
		// setScreen(new Level1Screen());

	}
	
	@Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}