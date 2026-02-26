package it.mpace.thomas.res;

public class GameControlRes {

	public final static float MAX_ENERGY = 100;
	public final static float MAX_TIME = 2000;

	public static int score = 0;
	public static float energy = MAX_ENERGY;
	public static int lives = 3;
	public static int hiScore = 1000;
	public static float gameTime = MAX_TIME;
	public static final float TIME_SPEED = 10f; // Quanto velocemente cala (es. 50 unità al secondo)
	public static final float PLAYER_SPEED=50f;
	public static final float GRIPPER_SPEED=40f;
	public static final float KNIFE_THROWER_SPEED=40f;
	public static final float STICK_FIGHTER_SPEED=50f;
	public static final float KNIFE_SPEED=60f;
	public static final float KNIFE_INTERVAL=5f;
	
	public static boolean debugMode=true;

	public static void printGameStatus() {
		System.out
				.println("[Score=" + score + "] energy[" + energy + "] lives[" + lives + "] HISCORE=[" + hiScore + "] time["+gameTime+"]");
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
		/*if (GameControlRes.energy <= 0) {
			GameControlRes.decrementLives();;
			GameControlRes.energy = MAX_ENERGY;
			System.out.println("LIFE LOST");
			printGameStatus();
		}*/
	}
	
	public static void decrementTime() {
		GameControlRes.gameTime--;
	}
	
	public static void resetForRespawn() {
        energy = MAX_ENERGY;
        gameTime = MAX_TIME;
        // Eventuali reset di timer di livello o bonus qui
    }

    public static void fullReset() {
        lives = 3;
        score = 0;
        resetForRespawn();
    }

}
