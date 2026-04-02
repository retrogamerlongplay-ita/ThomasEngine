package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioRes {
	public static Music bgm_main_theme;
	public static Music bgm_get_ready;
	public static Music bgm_level_completed;
	public static Music bgm_game_completed;
	public static Music bgm_game_over;
	public static Music bgm_intro_walk;

	public static Sound punchSound;
	public static Sound kickSound;
	public static Sound dieSound;
	public static Sound playerHurtSound;
	public static Sound potCrushSound;
	public static Sound hazardHitSound;
	public static Sound evilLaughSound;
	public static Sound introWalkSound;
	public static Sound flameSound;
	public static Sound snakeHitSound;

	public static void load() {
		bgm_main_theme = Gdx.audio.newMusic(Gdx.files.internal("audio/02_main_theme.wav"));
		bgm_main_theme.setLooping(true);
		bgm_main_theme.setVolume(0.5f);

		bgm_get_ready = Gdx.audio.newMusic(Gdx.files.internal("audio/01_get_ready.wav"));
		bgm_get_ready.setLooping(false);
		bgm_get_ready.setVolume(0.5f);

		bgm_level_completed = Gdx.audio.newMusic(Gdx.files.internal("audio/03_level_completed.wav"));
		bgm_level_completed.setLooping(false);
		bgm_level_completed.setVolume(0.5f);

		bgm_game_completed = Gdx.audio.newMusic(Gdx.files.internal("audio/04_game_completed.wav"));
		bgm_game_completed.setLooping(false);
		bgm_game_completed.setVolume(0.5f);

		bgm_game_over = Gdx.audio.newMusic(Gdx.files.internal("audio/05_game_over.wav"));
		bgm_game_over.setLooping(false);
		bgm_game_over.setVolume(0.5f);
		
		bgm_intro_walk = Gdx.audio.newMusic(Gdx.files.internal("audio/00_intro_walk.wav"));
		bgm_intro_walk.setLooping(true);
		bgm_intro_walk.setVolume(0.5f);

		punchSound = Gdx.audio.newSound(Gdx.files.internal("audio/punch.wav"));
		kickSound = Gdx.audio.newSound(Gdx.files.internal("audio/kick.wav"));
		dieSound = Gdx.audio.newSound(Gdx.files.internal("audio/die.wav"));
		playerHurtSound = Gdx.audio.newSound(Gdx.files.internal("audio/hit.wav"));
		potCrushSound = Gdx.audio.newSound(Gdx.files.internal("audio/pot_crush.wav"));
		hazardHitSound = Gdx.audio.newSound(Gdx.files.internal("audio/hazard_hit.wav"));
		snakeHitSound = Gdx.audio.newSound(Gdx.files.internal("audio/snake_hit.wav"));
		evilLaughSound= Gdx.audio.newSound(Gdx.files.internal("audio/evil_laugh_2.wav"));
		introWalkSound= Gdx.audio.newSound(Gdx.files.internal("audio/00_intro_walk.wav"));
		flameSound= Gdx.audio.newSound(Gdx.files.internal("audio/flame.wav"));
	}

	// Metodo helper per i SUONI (effetti brevi)
	public static void playSound(Sound sound) {
		if (!GameControlRes.isMuted) {
			sound.play();
		}
	}
	
	public static void loopSound(Sound sound) {
		if (!GameControlRes.isMuted) {
			sound.loop();
		}
	}

	// Metodo helper per la MUSICA (temi lunghi)
	public static void playMusic(Music music) {
		if (!GameControlRes.isMuted) {
			music.play();
		}
	}

	public static void stopSound(Sound sound) {
		sound.stop();
	}

	// Metodo helper per la MUSICA (temi lunghi)
	public static void stopMusic(Music music) {
		music.stop();
	}
	
	public static void pauseSound(Sound sound) {
		sound.pause();
	}

	// Metodo helper per la MUSICA (temi lunghi)
	public static void pauseMusic(Music music) {
		music.pause();
	}
	
	public static void dispose() {
	    bgm_main_theme.dispose();
	    bgm_get_ready.dispose();
	    bgm_level_completed.dispose();
	    bgm_game_completed.dispose();
	    bgm_game_over.dispose();
	    punchSound.dispose();
	    kickSound.dispose();
	    dieSound.dispose();
	    playerHurtSound.dispose();
	    potCrushSound.dispose();
	    hazardHitSound.dispose();
	    evilLaughSound.dispose();
	    introWalkSound.dispose();
	    // ... tutti gli altri music e sound
	}
}
