package it.mpace.thomas;


public final class ThomasCredits {
	// prevent instantiation
	private ThomasCredits() {}
	
	public static final String VER = "0.0.30";
	public static final String AUTHOR = "Matteo Pace";
	public static final String GAME_ENGINE_TITLE = "THOMAS";
	public static final String TITLE = "Kung Fu Master";
	
	
	public static void printout() {
		System.out.println("..:: "+ TITLE+" ::..");
		System.out.println("Realized by ["+ AUTHOR+"]");
		System.out.println("With ["+ GAME_ENGINE_TITLE+"] game engine created by ["+AUTHOR+"]");
		System.out.println("Version ["+ VER+"]");
	}
}

/***
 * TODO:
 * - Sequenza introduttiva attract mode (rapimento Silvia)
 * - Movimento verticale/rotatorio delle Butterfly (da rivedere sull'originale)
 * - Sequenza introduttiva al floor 3 o 4 ( da rivedere sull'originale)
 * - Bilanciamento dello spawning dei nemici
 * - Miglioramento HUD rappresentazione piani
 * - Hunchback se colpito in alto (calcio o pugno perde la testa scompare e riappare
*/