package it.mpace.thomas.data;

import it.mpace.thomas.LevelConstants;

public class LevelInfo {
	private float groundY;
	private float ceilingY;
	private float goalX;
	private float gripperSpeed;
	private float levelBeginX;
	private float knifeThrowerSpeed;
	private float knifeSpeed;
	private float knifeInterval;
	private int maxKnifeThrowers;
	private int maxEnemies;
	private float spawnOffset;
	private float respawnX;
	private float cameraY;
	private int floor;
	private float spawnInteval;
	private int bossDistance;

	public float getGroundY() {
		return groundY;
	}

	public void setGroundY(float groundY) {
		this.groundY = groundY;
	}

	public float getGoalX() {
		return goalX;
	}

	public void setGoalX(float goalX) {
		this.goalX = goalX;
	}

	public float getGripperSpeed() {
		return gripperSpeed;
	}

	public void setGripperSpeed(float gripperSpeed) {
		this.gripperSpeed = gripperSpeed;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public float getLevelBeginX() {
		return levelBeginX;
	}

	public void setLevelBeginX(float levelBeginX) {
		this.levelBeginX = levelBeginX;
	}

	public float getKnifeThrowerSpeed() {
		return knifeThrowerSpeed;
	}

	public void setKnifeThrowerSpeed(float knifeThrowerSpeed) {
		this.knifeThrowerSpeed = knifeThrowerSpeed;
	}

	public float getKnifeSpeed() {
		return knifeSpeed;
	}

	public void setKnifeSpeed(float knifeSpeed) {
		this.knifeSpeed = knifeSpeed;
	}

	public float getKnifeInterval() {
		return knifeInterval;
	}

	public void setKnifeInterval(float knifeInterval) {
		this.knifeInterval = knifeInterval;
	}

	public int getMaxKnifeThrowers() {
		return maxKnifeThrowers;
	}

	public void setMaxKnifeThrowers(int maxKnifeThrowers) {
		this.maxKnifeThrowers = maxKnifeThrowers;
	}

	public int getMaxEnemies() {
		return maxEnemies;
	}

	public void setMaxEnemies(int maxEnemies) {
		this.maxEnemies = maxEnemies;
	}

	public float getSpawnOffset() {
		return spawnOffset;
	}

	public void setSpawnOffset(float spawnOffset) {
		this.spawnOffset = spawnOffset;
	}

	public float getRespawnX() {
		return respawnX;
	}

	public void setRespawnX(float respawnX) {
		this.respawnX = respawnX;
	}

	public float getCameraY() {
		return cameraY;
	}

	public void setCameraY(float cameraY) {
		this.cameraY = cameraY;
	}

	public float getSpawnInteval() {
		return spawnInteval;
	}

	public void setSpawnInteval(float spawnInteval) {
		this.spawnInteval = spawnInteval;
	}

	public float getCeilingY() {
		return ceilingY;
	}

	public void setCeilingY(float ceilingY) {
		this.ceilingY = ceilingY;
	}

	public int getBossDistance() {
		return bossDistance;
	}

	public void setBossDistance(int bossDistance) {
		this.bossDistance = bossDistance;
	}

	public boolean isExceedingLevelBegin(float playerX) {
		switch (floor) {
		case 1:
			return playerX > LevelConstants.FIRST_FLOOR_RIGHT;
		case 2:
			return playerX < LevelConstants.SECOND_FLOOR_LEFT;
		case 3:
			return playerX > LevelConstants.THIRD_FLOOR_RIGHT;
		}
		return false;
	}

	public boolean isExceedingLevelGoal(float playerX) {
		switch (floor) {
		case 1:
			return playerX < LevelConstants.FIRST_FLOOR_LEFT_STAIR;
		case 2:
			return playerX > LevelConstants.SECOND_FLOOR_RIGHT_STAIR;
		case 3:
			return playerX < LevelConstants.THIRD_FLOOR_LEFT_STAIR;
		}
		return false;
	}

	@Override
	public String toString() {
		return "LevelInfo [groundY=" + groundY + ", ceilingY=" + ceilingY + ", goalX=" + goalX + ", gripperSpeed="
				+ gripperSpeed + ", levelBeginX=" + levelBeginX + ", knifeThrowerSpeed=" + knifeThrowerSpeed
				+ ", knifeSpeed=" + knifeSpeed + ", knifeInterval=" + knifeInterval + ", maxKnifeThrowers="
				+ maxKnifeThrowers + ", maxEnemies=" + maxEnemies + ", spawnOffset=" + spawnOffset + ", respawnX="
				+ respawnX + ", cameraY=" + cameraY + ", floor=" + floor + ", spawnInteval=" + spawnInteval
				+ ", bossDistance=" + bossDistance + "]";
	}

}
