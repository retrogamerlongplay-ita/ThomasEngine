package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GameControlRes;

public class GameOverScreen implements Screen {
    private final ThomasMain game;
    private OrthographicCamera cam;
    private float screenTimer = 0;

    public GameOverScreen(ThomasMain game) {
        this.game = game;
        this.cam = new OrthographicCamera();
        // Impostiamo la camera esattamente come l'HUD del livello
        this.cam.setToOrtho(false, 256, 256);
        this.cam.update();
        
        AudioRes.stopMusic(AudioRes.bgm_main_theme);
        AudioRes.playMusic(AudioRes.bgm_game_over);
    }

    @Override
    public void render(float delta) {
        // 1. Pulisce lo schermo (Nero Arcade)
        //ScreenUtils.clear(0, 0, 0, 1);
    	ScreenUtils.clear(new Color(0.678f, 0.847f, 0.902f,1)); // Azzurro chiaro arcade
        screenTimer += delta;

        // 2. Resettiamo la proiezione del batch sulla camera fissa 256x256
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        // 3. RESET SCALA FONT (Fondamentale se arrivi dal Level1Screen)
        game.font.getData().setScale(0.5f); 
        
        // 4. DISEGNO TESTI (Coordinate fisse 0-256)
        game.font.setColor(Color.RED);
        // Scritta GAME OVER centrata (circa 128 - metà larghezza testo)
        game.font.draw(game.batch, "GAME OVER", 90, 160);
        
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(0.5f); // Rimpiccioliamo solo per il punteggio
        game.font.draw(game.batch, "FINAL SCORE: " + String.format("%06d", (int) GameControlRes.score), 75, 130);

        if (screenTimer > 2.0f) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "PRESS ENTER TO MENU", 80, 100);
        }

        game.batch.end();

        // Input per tornare al menu
        if (screenTimer > 2.0f && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameControlRes.fullReset();
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
