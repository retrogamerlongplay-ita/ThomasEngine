package it.mpace.thomas;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GripperRes;
import it.mpace.thomas.res.KnifeThrowerRes;
import it.mpace.thomas.res.PlayerRes;
import it.mpace.thomas.res.StickFighterRes;
import it.mpace.thomas.screen.MainMenuScreen;

public class ThomasMain extends Game {

	public SpriteBatch batch;
	public BitmapFont font;

	@Override
	public void create() {
		ThomasCredits.printout();
		batch = new SpriteBatch();
		
		PlayerRes.load();
		GripperRes.load();
		KnifeThrowerRes.load();
		StickFighterRes.load();
		AudioRes.load();
		loadFonts();
		setScreen(new MainMenuScreen(this));
	}
	
	private void loadFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/irem-font.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        
        parameter.size = 8; // Dimensione base arcade (multipli di 8)
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1f; // Contorno nero tipico dei brawler
        parameter.borderColor = Color.BLACK;
        
        // Rende il font nitido (senza antialiasing) per la pixel art
        parameter.magFilter = Texture.TextureFilter.Nearest;
        parameter.minFilter = Texture.TextureFilter.Nearest;

        font = generator.generateFont(parameter);
        generator.dispose(); 
    }
	
	@Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}