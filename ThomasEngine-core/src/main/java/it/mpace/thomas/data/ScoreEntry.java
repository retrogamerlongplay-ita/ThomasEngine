package it.mpace.thomas.data;

public class ScoreEntry {
	private String playerName;
	private int score;

	public ScoreEntry(String playerName, int score) {
		this.playerName = playerName;
		this.score = score;
	}

	public String getPlayerName() {
		return playerName;
	}

	public int getScore() {
		return score;
	}

}
