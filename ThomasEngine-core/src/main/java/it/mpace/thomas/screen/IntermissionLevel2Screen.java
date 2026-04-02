package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.PlayerRes;
import it.mpace.thomas.res.SylviaRes;

/**
 * Intermezzo tra Level2 e Level3: Sylvia seduta e Thomas che cammina.
 */
public class IntermissionLevel2Screen implements Screen {

	private ThomasMain game;
	private SpriteBatch batch;
	private OrthographicCamera camera;

	private float timer = 0f;
	private final float DURATION = 5.0f; // durata dell'intermezzo

	// Thomas position/animation
	private float thomasX = 200f;
	private float thomasY = 60f;
	private float thomasSpeed = -40f; // pixels per second logical
	private float thomasStateTime = 0f;

	// Sylvia
	private float sylviaX = 60f;
	private float sylviaY = 65f;
	private float sylviaStateTime = 0f;
	
	private float chairX = 45f;
	private float chairY = 63f;

	public IntermissionLevel2Screen(ThomasMain game) {
		this.game = game;
		this.batch = game.batch;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false, 256, 256);
		this.thomasX = -40f;
	}

	@Override
	public void show() {
		// Assicuriamoci che le risorse siano caricate (ThomasMain dovrebbe già chiamare
		// SylviaRes.load())
//		if (PlayerRes.atlas == null)
//			PlayerRes.load();
//		if (SylviaRes.atlas == null)
//			SylviaRes.load();

		AudioRes.loopSound(AudioRes.evilLaughSound);
	}

	@Override
	public void render(float delta) {
		float dt = Math.min(delta, 1 / 60f);
		timer += dt;
		thomasStateTime += dt;
		sylviaStateTime += dt;

		// Movimento Thomas: cammina verso destra
		thomasX += thomasSpeed * dt;
		if (thomasX < 200)
			thomasX = 200; // clamp

		// Gdx.gl.glClearColor(0, 0, 0, 1);
		// Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ScreenUtils.clear(new Color(0.678f, 0.847f, 0.902f, 1)); // Azzurro chiaro arcade
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		TextureRegion chairFrame = SylviaRes.chairFrame;
		batch.draw(chairFrame, chairX, chairY, chairFrame.getRegionWidth(), chairFrame.getRegionHeight());
		
		// Disegna Sylvia seduta
		TextureRegion sylFrame = SylviaRes.sitAnim.getKeyFrame(sylviaStateTime, true);
		float sw = sylFrame.getRegionWidth();
		float sh = sylFrame.getRegionHeight();
		float sylDrawX = sylviaX - (sw / 2);
		batch.draw(sylFrame, sylDrawX, sylviaY, sw, sh);
		
		

		// Disegna Thomas che cammina
		TextureRegion thFrame = PlayerRes.walkAnim.getKeyFrame(thomasStateTime, true);
		float tw = thFrame.getRegionWidth();
		float th = thFrame.getRegionHeight();
		float thDrawX = thomasX - (tw / 2);
		// Thomas guarda verso destra: PlayerRes frames are oriented to right by default
		// if (!thFrame.isFlipX()) {
		// batch.draw(thFrame, thDrawX, thomasY, tw, th);
		// } else {
		batch.draw(thFrame, thDrawX + tw, thomasY, -tw, th);
		// }
		game.font.setColor(Color.WHITE);
		game.font.draw(batch, "LET'S TRY NEXT FLOOR", 90, 200);
		
		game.font.setColor(Color.YELLOW);

		game.font.draw(batch, "HELP ME,", 40, 145);
		game.font.draw(batch, "THOMAS!", 40, 130);

		//game.font.setColor(Color.YELLOW);

		//game.font.draw(batch, "I'M ARRIVING", 180, 200);
		// Small UI: press ENTER to continue
		game.font.setColor(Color.WHITE);
		game.font.draw(batch, "I'M COMING", 170, 165);
		game.font.draw(batch, "RIGHT AWAY,", 170, 150);
		game.font.draw(batch, "SYLVIA !", 170, 135);
		game.font.draw(batch, "Press ENTER to continue", 80, 16);

		batch.end();

		// Avanzamento automatico
		if (timer >= DURATION || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			// Vai al livello successivo
			AudioRes.stopSound(AudioRes.evilLaughSound);
			// AudioRes.playMusic(AudioRes.bgm_main_theme);
			game.setScreen(new Level3Screen(game));
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		// nulla da liberare qui: le risorse sono condivise e gestite da ThomasMain
	}
}
