package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.res.GameControlRes;

public class NameEntryScreen implements Screen {
    private final ThomasMain game;
    private OrthographicCamera cam;
    private char[] initials = {'A', 'A', 'A'};
    private int charIndex = 0;
    private int finalScore;
    private float blinkTimer = 0;

    public NameEntryScreen(ThomasMain game, int score) {
        this.game = game;
        this.finalScore = score;
        this.cam = new OrthographicCamera();
        this.cam.setToOrtho(false, 256, 256);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        blinkTimer += delta;
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();

        game.font.setColor(Color.RED);
        drawCenteredText("NEW RECORD!", 200);
        
        game.font.setColor(Color.YELLOW);
        drawCenteredText("SCORE: " + String.format("%06d", finalScore), 170);

        game.font.setColor(Color.WHITE);
        drawCenteredText("ENTER YOUR INITIALS", 130);

        // Disegno delle 3 lettere
        for (int i = 0; i < 3; i++) {
            if (i == charIndex && blinkTimer % 0.5f < 0.25f) {
                game.font.setColor(Color.RED); // Lettera selezionata lampeggia
            } else {
                game.font.setColor(Color.CYAN);
            }
            game.font.draw(game.batch, "" + initials[i], 100 + (i * 25), 100);
        }

        game.batch.end();

        handleInput();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            initials[charIndex]++;
            if (initials[charIndex] > 'Z') initials[charIndex] = 'A';
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            initials[charIndex]--;
            if (initials[charIndex] < 'A') initials[charIndex] = 'Z';
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            if (charIndex < 2) {
                charIndex++;
            } else {
                // SALVATAGGIO DEFINITIVO
                String finalName = new String(initials);
                GameControlRes.checkAndSaveScore(finalScore, finalName);
                game.setScreen(new HighScoreScreen(game));
            }
        }
    }

    private void drawCenteredText(String text, float y) {
        float approxWidth = text.length() * 8f; 
        game.font.draw(game.batch, text, 128 - (approxWidth / 2f), y);
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
