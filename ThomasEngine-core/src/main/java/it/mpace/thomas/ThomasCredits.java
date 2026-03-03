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
 */

public class ThomasCredits {
	public static String VER = "0.0.17";
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
 * - Nella sequenza di fine stage implementare il salire del Player sugli scalini
 * - Creare la sequenza di introduzione del livello (GET READY, camminata iniziale del Player)
 * - Musica
 * - Effetti sonori
 * - Schermata iniziale
 * - Schermata di Game Over
 * - Schermata di Hi-Score
 * - Sequenza introduttiva (il rapimento di Sylvia)
 * - Miglioramento HUD nuovi font
 * - Miglioramento HUD teste di Thomas come vite
 * - Implementazione di frame di hit 
*/