package it.mpace.thomas.res;

import com.badlogic.gdx.Gdx;

import it.mpace.thomas.data.ScoreEntry;

public class GameControlRes {

	public final static float MAX_ENERGY = 100;
	public final static float MAX_TIME = 2000;

	public static int score = 0;
	public static float energy = MAX_ENERGY;
	public static int lives = 3;
	public static int hiScore = 1000;
	public static float gameTime = MAX_TIME;
	public static final float TIME_SPEED = 10f; // Quanto velocemente cala (es. 50 unità al secondo)
	public static final float PLAYER_SPEED = 40f;

	public static boolean isMuted = false;

	public static boolean debugMode = true;

	public static void printGameStatus() {
		System.out.println("[Score=" + score + "] energy[" + energy + "] lives[" + lives + "] HISCORE=[" + hiScore
				+ "] time[" + gameTime + "]");
	}

	public static int getGlobalHiScore() {
		return Gdx.app.getPreferences("ThomasScores").getInteger("score1", 1000); // Decidi qui il default unico (es.
																					// 5000)
	}

	public static void fullReset() {
		score = 0;
		energy = MAX_ENERGY; // O il valore massimo che hai stabilito
		lives = 3; // Vite iniziali arcade
		gameTime = MAX_TIME; // Reset del timer di gioco (es. 2000 unità)
		isMuted = false; // Opzionale: se vuoi resettare anche l'audio
		// Carichiamo l'Hi-Score aggiornato per la nuova sessione
		hiScore = Gdx.app.getPreferences("ThomasScores").getInteger("score1", 5000);
	}

	public static void resetForRespawn() {
		energy = MAX_ENERGY;
		gameTime = MAX_TIME;
//  // Eventuali reset di timer di livello o bonus qui
	}

	public static void incrementScore(int increment) {
		GameControlRes.score += increment;
		if (GameControlRes.hiScore < GameControlRes.score) {
			GameControlRes.hiScore = GameControlRes.score;
		}
		printGameStatus();
	}

	public static void incrementLives() {
		GameControlRes.lives++;
	}

	public static void decrementLives() {
		GameControlRes.lives--;
		if (GameControlRes.lives <= 0) {
			System.out.println("GAME OVER");
		}
	}

	public static void incrementEnergy(float increment) {
		GameControlRes.energy += increment;
		if (GameControlRes.energy > GameControlRes.MAX_ENERGY) {
			GameControlRes.energy = GameControlRes.MAX_ENERGY;
		}
	}

	public static void decrementEnergy(float decrement) {
		GameControlRes.energy -= decrement;
	}

	public static void decrementTime() {
		GameControlRes.gameTime--;
	}

	public static void toggleMute() {
		isMuted = !isMuted;

		// Se mutiamo, fermiamo la musica corrente immediatamente
		if (isMuted) {
			AudioRes.bgm_main_theme.pause();
			AudioRes.bgm_get_ready.pause();
		} else {
			// Se riattiviamo, facciamo ripartire la musica appropriata (opzionale)
			AudioRes.bgm_main_theme.play();
		}
	}

	public static void toggleDebug() {
		debugMode = !debugMode;
	}

	public static void checkAndSaveScore(int score, String name) {
		com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ThomasScores");

		// Carichiamo la classifica attuale (Top 20)
		java.util.List<ScoreEntry> scores = new java.util.ArrayList<>();
		for (int i = 1; i <= 20; i++) {
			int s = prefs.getInteger("score" + i, 20000 - (i * 1000)); // Default calante
			String n = prefs.getString("name" + i, "IRE");
			scores.add(new ScoreEntry(n, s));
		}

		// Aggiungiamo il nuovo e ordiniamo
		scores.add(new ScoreEntry(name, score));
		scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore())); // Ordine decrescente

		// Salviamo i primi 5
		for (int i = 0; i < 20; i++) {
			prefs.putInteger("score" + (i + 1), scores.get(i).getScore());
			prefs.putString("name" + (i + 1), scores.get(i).getPlayerName());
		}
		prefs.flush();
	}

	// In GameControlRes.java o in una nuova classe dedicata
	public static void saveHighScore(int score) {
		com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ThomasScores");
		int hiScore = prefs.getInteger("highscore", 0);
		if (score > hiScore) {
			prefs.putInteger("highscore", score);
			prefs.flush(); // Scrive effettivamente su disco
		}
	}

	public static int loadHighScore() {
		return Gdx.app.getPreferences("ThomasScores").getInteger("highscore", 0);
	}

	public static boolean isHighScore(int score) {
		com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("ThomasScores");
		int lowestScore = prefs.getInteger("score20", 0);
		return score > lowestScore;
	}

}
