package it.mpace.thomas.actors;

import it.mpace.thomas.LevelConstants;

public abstract class GrabbingEnemy extends Enemy {
	
	public boolean isGrabbedFromRight; // Indica se è stato afferrato da destra o sinistra
	
	public GrabbingEnemy(float x, float y) {
		super(x, y, 1); // Chiama il costruttore di Enemy che inizializza position e hurtbox
		this.speed = LevelConstants.FIRST_FLOOR_GRIPPER_SPEED;
	}
}
