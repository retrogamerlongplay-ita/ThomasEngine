package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.ThomasMain;

public class HighScoreScreen implements Screen {
	 private final ThomasMain game;
	    private OrthographicCamera cam;
	    private String[] names = new String[20];
	    private int[] scores = new int[20];

	    public HighScoreScreen(ThomasMain game) {
	        this.game = game;
	        this.cam = new OrthographicCamera();
	        this.cam.setToOrtho(false, 256, 256);
	        
	        // Caricamento rapido per la visualizzazione
	        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ThomasScores");
	        for (int i = 0; i < 20; i++) {
	            names[i] = prefs.getString("name" + (i + 1), "IRE");
	            scores[i] = prefs.getInteger("score" + (i + 1), 0);
	        }
	    }

	    @Override
	    public void render(float delta) {
	    	ScreenUtils.clear(new Color(0.678f, 0.847f, 0.902f,1)); // Azzurro chiaro arcade
	        game.batch.setProjectionMatrix(cam.combined);
	        game.batch.begin();

	        game.font.setColor(Color.RED);
	        game.font.draw(game.batch, "TOP 20 KUNGFU MASTERS", 60, 240);

	        for (int i = 0; i < 10; i++) {
	            float y = 220 - (i * 20);
	            game.font.setColor(Color.WHITE);
	            game.font.draw(game.batch, (i + 1) + getOrdinalSuffix(i+1), 20, y);
	            
	            game.font.setColor(Color.YELLOW);
	            game.font.draw(game.batch, String.format("%06d", scores[i]), 50, y);
	            
	            game.font.setColor(Color.ORANGE);
	            game.font.draw(game.batch, names[i], 100, y);
	        }
	        
	        for (int i = 10; i < 20; i++) {
	            float y = 220 - ((i-10) * 20);
	            game.font.setColor(Color.WHITE);
	            game.font.draw(game.batch, (i + 1) + getOrdinalSuffix(i+1), 130, y);
	            
	            game.font.setColor(Color.YELLOW);
	            game.font.draw(game.batch, String.format("%06d", scores[i]), 160, y);
	            
	            game.font.setColor(Color.ORANGE);
	            game.font.draw(game.batch, names[i], 210, y);
	        }

	        game.font.setColor(Color.WHITE);
	        game.font.draw(game.batch, "PRESS ENTER TO MENU", 70, 20);
	        game.batch.end();

	        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
	            game.setScreen(new MainMenuScreen(game));
	        }
	    }
	    
	private String getOrdinalSuffix(int value) {
		if (value >= 11 && value <= 13) {
			return "TH";
		}
		switch (value % 10) {
			case 1: return "ST";
			case 2: return "ND";
			case 3: return "RD";
			default: return "TH";
		}
	}

    // Altri metodi Screen...
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
