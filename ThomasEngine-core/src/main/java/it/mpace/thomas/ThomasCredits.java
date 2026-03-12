package it.mpace.thomas;

/**
 * 
 * @version 0.0.1
 * <p>Simple Hello World app</p>
 * @version 0.0.2
 * <p>Simple Hello World app with libGDX</p>
 * @version 0.0.3 - 0.0.10
 * <ol>
 * <li>Implementation of the Background visualization and scrolling</li>
 * <li>Implementation of the Player Sprite</li>
 * <li>Implementation of the basic input (LEFT, RIGHT, PUNCH, KICK)</li>
 * <li>Implementation of the basic score, life, energy, hi-score system (only the logic, not visible)</li>
 * <li>Implementation of the Gripper Sprite</li>
 * </ol>
 * 
 * @version 0.0.11
 * <ol>
 * <li>Implementation of the basic collision system</li>
 * <li>Added gripping action and basic animation for the Gripper</li>
 * <li>Added HUD</li>
 * <li>Added basic HUD management</li>
 * </ol>
 * 
 *  @version 0.0.12
 * <ol>
 * <li>Advanced hitbox for collision</li>
 * <li>Added gripping action and basic animation for the Gripper</li>
 * <li>Remade sprite of Thomas with TexturePacker and atlas</li>
 * <li>Remade sprite of Gripper with TexturePacker and atlas</li>
 * <li>Reviewed HUD (not definitive)</li>
 * </ol>
 * 
 * @version 0.0.13
 * <ol>
 * <li>Thomas die animation and respawn</li>
 * <li>Game Over handling</li>
 * <li>Gripper approaching status</li>
 * <li>Game time management</li>
 * </ol>
 * 
 * @version 0.0.14
 * <ol>
 * <li>KnifeThrower implementation</li>
 * </ol>
 * 
 * @version 0.0.15
 * <ol>
 * <li>Optimized HUD</li>
 * <li>Low kick implementation</li>
 * <li>Low punch implementation</li>
 * <li>Optimized Gripper liberation</li>
 * </ol>
 * 
 * @version 0.0.16
 * <ol>
 * <li>Boss1 StickFighter introduction</li>
 * <li>Optimizaion in hiscore view in HUD</li>
 * <li>Implemented Level limits</li>
 * <li>Implemented LevelScreen logic</li>
 * </ol>
 * 
 *  @version 0.0.17
 * <ol>
 * <li>Player and enemy speed adjustment</li>
 * <li>Boss1 death implementation</li>
 * <li>Boss1 space limitation</li>
 * <li>Abstract implementation of the class LevelScreen that is specialized in Level1Screen and Level2Screen</li>
 * <li>Basic change of Level</li>
 * </ol>
 * 
 * 
 * @version 0.0.18
 * <ol>
 * <li>Stairs climbing animation</li>
 * <li>Music</li>
 * <li>Sound FX</li>
 * <li>Initial Main menu</li>
 * </ol>
 * 
 * @version 0.0.19
 * <ol>
 * <li>Main menu optimization</li>
 * <li>Capability to mute sound</li>
 * <li>GemeInput implementation (a class to separate input management)</li>
 * <li>Flying kick optimized</li>
 * <li>Flying punch optimized</li>
 * <li>Basic Game Over Screen</li>
 * </ol>
 * 
 * @version 0.0.20
 * <ol>
 * <li>Improvements in Game Over Screen</li>
 * <li>Added version in windows title</li>
 * <li>Optimized enemy spawning routine</li>
 * <li>Bug of hurtbox in crouch-kicking and crouch-punching resolved</li>
 * <li>Resolved warning in method handleAttackAnimation of Player class</li>
 * </ol>
 * 
 * 
 * @version 0.0.21
 * <ol>
 * <li>Corrcted redraw bug</li>
 * <li>Now two Gripper can grab the player</li>
 * <li>Corrected bug on death</li>
 * <li>Created the hurt status for player</li>
 * </ol>
 *  
 *  
 *  
 * @version 0.0.22
 * <ol>
 * <li>Implementation toggle logic for debug mode as for mute</li>
 * <li>Optimized IA of StickFighter</li>
 * <li>Optimized IA of KnifeThrower (to be optimized more)</li>
 * </ol>
 * 
 *  @version 0.0.23
 * <ol>
 * <li>Implemented dispose of sounds in AudioRes</li>
 * <li>Implemented LevelInfo class</li>
 * <li>Restructured LevelScreen, Level1Screen and Level2Screen based on LevelInfo class</li>
 * <li>Basic Level2Screen intro without background variation</li>
 * <li>Background color for menus and Game Over Screen</li>
 * </ol>
 * 
 * 
 * @version 0.0.24
 * <ol>
 * <li>Resolved bug of invisible cage at Floor 1 begin</li>
 * <li>Updated KnifeThrower IA</li>
 * <li>Retro style fonts implemented</li>
 * <li>Update Grabbed state sprite</li>
 * <li>Hi score screen implementation</li>
 * <li>Hi score entry screen implementation</li>
 * <li>Resolved bug of Grabbed and hit Player</li>
 * <li>Implemented yellow hit on enemies</li>
 * <li>Implemented basic red hit on player</li>
 * </ol>
 * 
 * @version 0.0.25
 * <ol>
 * <li>Added all sprites needed for floor 2</li>
 * <li></li>
 * </ol>
 */

public class ThomasCredits {
	public static String VER = "0.0.25x";
	public static String AUTHOR= "Matteo Pace";
	public static String GAME_ENGINE_TITLE = "THOMAS";
	public static String TITLE="Kung Fu Master";
	
	
	public static void printout() {
		System.out.println("..:: "+ TITLE+" ::..");
		System.out.println("Realized by ["+ AUTHOR+"]");
		System.out.println("With ["+ GAME_ENGINE_TITLE+"] game engine created by ["+AUTHOR+"]");
		System.out.println("Version ["+ VER+"]");
	}
}

/***
 * TODO:
 * - Sequenza introduttiva (il rapimento di Sylvia)
 * - Miglioramento HUD teste di Thomas come vite
 * - Miglioramento HUD rappresentazione piani
 * - Miglioramento implementazione di frame di hit rosso a seconda di dove si viene colpiti (testa, corpo, gambe)
 * - Sequenza introduttiva floor 2 
*/