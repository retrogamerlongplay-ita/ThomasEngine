package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.res.AudioRes;

public class IntroLetterScreen implements Screen {
    private final ThomasMain game;
    private OrthographicCamera cam;
    private Texture letterTexture;
    private float timer = 0;
    private final float DURATION = 7.0f; // Tempo di lettura prima dello switch automatico

    public IntroLetterScreen(ThomasMain game) {
        this.game = game;
        this.cam = new OrthographicCamera();
        this.cam.setToOrtho(false, 256, 256); // Risoluzione coerente con il gioco
        
        // Carichiamo l'immagine della lettera (es. 256x256 o proporzionale)
        this.letterTexture = new Texture("intro_letter.png"); 
        
        // Audio: Fermi il tema del menu e fai partire il "Get Ready" o un jingle intro
        AudioRes.stopMusic(AudioRes.bgm_main_theme);
        AudioRes.playMusic(AudioRes.bgm_get_ready);
    }

    @Override
    public void render(float delta) {
        // Sfondo nero per i bordi se l'immagine non copre tutto
        ScreenUtils.clear(0, 0, 0, 1);
        timer += delta;

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        // Disegniamo la lettera a tutto schermo (0,0,256,256)
        game.batch.draw(letterTexture, 0, 0, 256, 256);

        // Feedback opzionale "PUSH START" lampeggiante in basso
        if (timer > 2.0f && (timer % 1.0f < 0.5f)) {
            game.font.setColor(Color.YELLOW);
            game.font.getData().setScale(0.5f);
            // Posiziona il testo in un punto dove non copre il messaggio della lettera
            game.font.draw(game.batch, "PUSH START", 95, 30);
        }

        game.batch.end();

        // Transizione al Livello 1
        if (timer >= DURATION || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            game.setScreen(new Level1Screen(game));
            dispose();
        }
    }

    @Override public void dispose() {
        if (letterTexture != null) letterTexture.dispose();
        AudioRes.stopMusic(AudioRes.bgm_get_ready);
    }

    // Metodi vuoti obbligatori per Screen
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
