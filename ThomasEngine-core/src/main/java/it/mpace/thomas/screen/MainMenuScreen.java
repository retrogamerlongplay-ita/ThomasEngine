package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.res.GameControlRes;

public class MainMenuScreen implements Screen {
	private int hiScore;
	private float attractTimer = 0;
	private final float ATTRACT_LIMIT = 10.0f; // Dopo 10 secondi mostra i record

    private final ThomasMain game;
    private Texture background;
    private float flashTimer = 0;
    private OrthographicCamera menuCam; // Aggiungi questa

    public MainMenuScreen(ThomasMain game) {
        this.game = game;
        // Carica il logo o lo sfondo del menu
        this.background = new Texture("kung_fu_master_logo.png"); 
        
        // Carichiamo l'Hi-Score salvato (o quello attuale della sessione)
        this.hiScore = Gdx.app.getPreferences("ThomasScores").getInteger("score1", 1000);
        
        this.menuCam = new OrthographicCamera();
        this.menuCam.setToOrtho(false, 256, 256);
        this.menuCam.update();
    }

    @Override
    public void render(float delta) {
    	ScreenUtils.clear(new Color(0.678f, 0.847f, 0.902f,1)); // Azzurro chiaro arcade
        flashTimer += delta;
        attractTimer += delta;


        // FONDAMENTALE: Forza il batch a usare la vista 256x256
        game.batch.setProjectionMatrix(menuCam.combined);
        game.batch.begin();
        
        // RESET SCALA FONT (per sicurezza se torni dal gioco/gameover)
        game.font.getData().setScale(0.5f); 
        
        game.font.setColor(Color.YELLOW);
        drawCenteredText("HI-SCORE: " + String.format("%06d", hiScore), 245);
     // 1. DISEGNA IL LOGO (Coordinate fisse 0-256)
        // Centro X = 128. Logo largo 180 -> 128 - 90 = 38
        game.batch.draw(background, 38, 140, 180, 80);

        // 2. TESTI LAMPEGGIANTI
        if (flashTimer % 1.0f < 0.6f) {
        	game.font.setColor(Color.WHITE);
            drawCenteredText("PUSH START BUTTON", 100);
           
        }
        
        game.font.setColor(Color.RED);
        drawCenteredText("PRESS ENTER TO PLAY", 85);
        
        game.font.setColor(Color.WHITE);
        drawCenteredText("COPYRIGHT 1984 IREM / THOMAS CLONE", 20);

        game.batch.end();
        
        // 4. LOGICA DI ATTRACT MODE (Auto-Classifica)
        if (attractTimer > ATTRACT_LIMIT) {
            game.setScreen(new HighScoreScreen(game));
            dispose();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameControlRes.fullReset(); 
            game.setScreen(new IntroLetterScreen(game)); // <--- Cambiato qui
            dispose();
        }

        // Input...
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new Level1Screen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameOverScreen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new Level2Screen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new Level3Screen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new Level4Screen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new Level5Screen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new IntermissionLevel2Screen(game)); 
            dispose();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) {
        	GameControlRes.fullReset(); // <--- FONDAMENTALE: Azzera tutto prima di partire
            game.setScreen(new IntermissionLevel4Screen(game)); 
            dispose();
        }
    }

    // Metodo helper veloce per centrare il testo
    private void drawCenteredText(String text, float y) {
        // Con camera 256x256, la larghezza media di un carattere a scala 0.5 è circa 4-5px
        float approxWidth = text.length() * 5f; 
        game.font.draw(game.batch, text, 128 - (approxWidth / 2f), y);
    }


    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    
    @Override 
    public void dispose() {
        background.dispose();
    }
}
