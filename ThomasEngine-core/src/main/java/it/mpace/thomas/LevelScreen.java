package it.mpace.thomas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.Player.State;
import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.GripperRes;
import it.mpace.thomas.res.KnifeThrowerRes;
import it.mpace.thomas.res.PlayerRes;

public class LevelScreen implements Screen {
	private Array<Enemy> minions; // Solo Gripper e KnifeThrower

	private SpriteBatch batch;
	private Texture background;
	private OrthographicCamera camera;

	private Player player;
	private StickFighter boss;
	private Array<Enemy> enemies = new Array<Enemy>();
	private float spawnTimer;
	private float deathTimer;
	private float spawnInterval = 2.5f; // Un nemico ogni 2.5 secondi

	private ShapeRenderer shapeRenderer;
	public static boolean debugMode = true; // Flag per attivare/disattivare il debug

	private OrthographicCamera hudCamera;

	// Aggiungi come variabile di istanza
	private BitmapFont font;

	private Array<Knife> knives = new Array<>();

	private Texture whitePixel = null;

	private int knifeThrowersSpawned = 0;
	private final int MAX_KNIFE_THROWERS_LEVEL_1 = 2;

	public LevelScreen() {
		// Inizializzazione entità
		minions = new Array<>();
		// ... setup camera e batch ...
		batch = new SpriteBatch();
		background = new Texture("kungfum_1_floor.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 256, 256);
		camera.position.x = LevelConstants.FIRST_FLOOR_RIGHT_START;
		camera.position.y = LevelConstants.FIRST_FLOOR_Y;
		player = new Player(LevelConstants.FIRST_FLOOR_RIGHT_START, LevelConstants.FIRST_FLOOR_GROUND_Y);
		boss = new StickFighter(LevelConstants.FIRST_FLOOR_LEFT_STAIR, LevelConstants.FIRST_FLOOR_GROUND_Y);
		// camera.update();
		shapeRenderer = new ShapeRenderer();
		font = new BitmapFont();
		font.getData().setScale(0.5f); // Rimpicciolisci per la tua camera 256x256
		// Inizializzazione dell'engine
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, 256, 256); // Stessa risoluzione logica, ma fissa

		// Crea una texture 1x1 pixel bianca programmata
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		whitePixel = new Texture(pixmap);
		pixmap.dispose();
	}

	@Override
	public void render(float delta) {
		update(delta);
		draw();
	}

	private void update(float dt) {
		// 1. GESTIONE TEMPO DI GIOCO
		GameControlRes.gameTime -= GameControlRes.TIME_SPEED * dt;
		if (GameControlRes.gameTime <= 0) {
			GameControlRes.gameTime = 0;
			if (player.currentState != Player.State.DEAD) {
				player.triggerDeath();
			}
		}

		// 2. UPDATE DEL GIOCATORE
		player.update(dt);

		// 3. GESTIONE LIBERAZIONE (Se Thomas si libera, tutti i Gripper che lo tengono
		// muoiono)
		if (player.checkAndResetLiberation()) {
			for (Enemy e : enemies) {
				if (e instanceof Gripper && ((Gripper) e).state == Gripper.GripperState.GRABBING) {
					Gripper g = (Gripper) e;
					g.hit(player); // Il nemico muore e vola via
					// Opzionale: aggiungi punti extra per la liberazione
					g.position.x += (g.position.x > player.position.x) ? 20 : -20;
					GameControlRes.incrementScore(50);
				}
			}
			// Feedback visivo: Thomas "scuote" lo schermo o fa un piccolo salto
			camera.position.y += 2; // Un piccolo sussulto della camera
		}

		boolean isBossActive = (boss.getState() != StickFighter.State.WAITING
				&& boss.getState() != StickFighter.State.DEAD);

		// 4. LOGICA NEMICI (Loop a ritroso per sicurezza)
		for (int i = enemies.size - 1; i >= 0; i--) {
			Enemy e = enemies.get(i);
			if (isBossActive) {
				e.flee();
			}

			// Update specifico in base al tipo di nemico
			if (e instanceof KnifeThrower) {
				((KnifeThrower) e).update(dt, player, knives); // Passiamo l'array dei coltelli
			} else {
				e.update(dt, player);
			}

			// --- COLLISIONE: Thomas colpisce Nemico ---
			if ((player.currentState == State.PUNCHING || player.currentState == State.KICKING
					|| player.currentState == State.PUNCHING_CROUCH || player.currentState == State.KICKING_CROUCH)
					&& !e.isDying) {
				if (player.hitbox.overlaps(e.hurtbox)) {
					e.hit(player);
					if (e.isDying) {
						GameControlRes.incrementScore(e instanceof KnifeThrower ? 500 : 100);
					} else {
						GameControlRes.incrementScore(50);
					}
				}
			}

			// --- COLLISIONE: Nemico afferra Thomas (Solo Gripper) ---
			if (e instanceof Gripper) {
				Gripper g = (Gripper) e;

				// FIX: Se Thomas muore, il Gripper deve mollarlo subito!
				if (player.currentState == State.DEAD && g.state == Gripper.GripperState.GRABBING) {
					g.state = Gripper.GripperState.WALKING; // O FLEEING se preferisci
					// Opzionale: dai una piccola spinta all'indietro al nemico per "distacco"
					g.position.x += (g.position.x > player.position.x) ? 10 : -10;
				} else if (!e.isDying && player.currentState != State.GRABBED && player.currentState != State.PUNCHING
						&& player.currentState != State.KICKING && player.currentState != State.DEAD) {
					float distanceX = Math.abs(player.position.x - e.position.x);
					if (distanceX < 12f) { // Distanza per l'abbraccio
						player.currentState = State.GRABBED;
						((Gripper) e).state = Gripper.GripperState.GRABBING;
						((Gripper) e).isGrabbedFromRight = (e.position.x > player.position.x);
					}
				} else if (player.currentState == State.GRABBED && e instanceof Gripper
						&& ((Gripper) e).state == Gripper.GripperState.GRABBING) {
					GameControlRes.decrementEnergy(15f * dt);
				}
			}

			// Rimozione nemici morti/usciti di scena
			if (!e.active)
				enemies.removeIndex(i);
		}

		// 5. LOGICA PROIETTILI (COLTELLI)
		for (int i = knives.size - 1; i >= 0; i--) {
			Knife k = knives.get(i);
			k.update(dt);
			// Collisione Coltello -> Thomas
			if (k.hitbox.overlaps(player.hurtbox) && player.currentState != State.DEAD) {
				// Verifica se Thomas sta schivando
				boolean hit = true;
				// Se il coltello è ALTO e Thomas è abbassato -> Schivato
				if (k.position.y > LevelConstants.FIRST_FLOOR_GROUND_Y + 30 && player.currentState == State.CROUCHING)
					hit = false;
				// Se il coltello è BASSO e Thomas sta saltando -> Schivato
				if (k.position.y < LevelConstants.FIRST_FLOOR_GROUND_Y + 25 && player.currentState == State.JUMPING)
					hit = false;

				if (hit) {
//							 GameControlRes.decrementEnergy(30f); // Sottrai un blocco di energia
//							 player.currentState = State.GRABBED; // Usa provvisoriamente lo stato grabbed per l'animazione hurt
//							 player.resetStateTime();
					player.takeHit(20f);
					k.active = false; // Il coltello scompare dopo l'impatto
				}
			}
			// Rimozione coltelli fuori schermo
			if (!k.active || Math.abs(k.position.x - camera.position.x) > 200) {
				knives.removeIndex(i);
			}
		}

		// Logica specifica: se il boss attacca, i minions scappano
		boolean bossActive = (boss.getState() == StickFighter.State.WALKING);

		if (boss.getState() == StickFighter.State.ATTACKING_HIGH||boss.getState() == StickFighter.State.ATTACKING_LOW||boss.getState() == StickFighter.State.ATTACKING_MID) {
			// Se la hitbox del bastone tocca Thomas e lui non è già morto o appena colpito
			if (boss.stickHitbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
				player.takeHit(15f); // Danno consistente dal boss
				// Opzionale: aggiungi un piccolo rinculo a Thomas
				player.position.x += (boss.position.x > player.position.x) ? -10 : 10;
			}
		}
		
		if (boss.getState() != StickFighter.State.DEAD && !boss.isDying) {
		    // Se Thomas prova ad andare a SINISTRA del Boss (verso le scale)
		    if (player.position.x < boss.position.x + 3) { 
		        player.position.x = boss.position.x + 3;
		        // Opzionale: se Thomas insiste a spingere, il Boss lo colpisce subito
		    }
		}
		
		if (!boss.isDying && player.isAttacking()) { // isAttacking() controlla se Thomas è in PUNCH o KICK
		    if (player.hitbox.overlaps(boss.hurtbox)) {
		        // Applichiamo il danno solo se il boss non è già in animazione di "Hurt" 
		        // per evitare che un singolo pugno tolga 10 HP in un colpo solo
		        if (boss.getState() != StickFighter.State.HURT_HIGH && 
		            boss.getState() != StickFighter.State.HURT_LOW) {
		            
		            boss.hit(player);
		            GameControlRes.incrementScore(200); // Punti per aver colpito il boss
		            
		            // Effetto "Hit Stop": potresti fermare il tempo per 0.05s per dare impatto
		        }
		    }
		}

		for (int i = minions.size - 1; i >= 0; i--) {
			Enemy m = minions.get(i);
			if (bossActive)
				m.flee();
			// m.update(dt, player.getPosition());

			// Rimuovi nemici fuori schermo o morti
			if (!m.isActive())
				minions.removeIndex(i);
		}

		boss.update(dt, player);
		// 6. CAMERA E SPAWN
		updateCamera();
		handleSpawning(dt);
	}

	private void updateCamera() {
		// La camera segue la posizione X del giocatore
		if (player.currentState == Player.State.DEAD) {
			return; // Esci subito: la camera resta ferma dov'era al momento del colpo ferale
		}
		camera.position.x = player.position.x;

		// Vincoli della camera per non mostrare il "vuoto" oltre i limiti del piano
		if (camera.position.x < LevelConstants.FIRST_FLOOR_LEFT_STAIR) {
			camera.position.x = LevelConstants.FIRST_FLOOR_LEFT_STAIR;
		}
		if (camera.position.x > LevelConstants.FIRST_FLOOR_RIGHT_START) {
			camera.position.x = LevelConstants.FIRST_FLOOR_RIGHT_START;
		}

		camera.update();
	}

	private void handleGameOverInput() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			GameControlRes.fullReset(); // Metodo che azzera vite, score ed energia
			knives.clear();
			enemies.clear();
			player.respawn(LevelConstants.FIRST_FLOOR_RIGHT_START, LevelConstants.FIRST_FLOOR_GROUND_Y);
			camera.position.x = player.position.x;

		}
	}

	private void handleDeath(float dt) {
		if (player.position.y > LevelConstants.FIRST_FLOOR_GROUND_Y + 1)
			return;
		deathTimer += dt;

		// In Kung Fu Master Thomas lampeggia o resta a terra
		if (deathTimer >= 3.0f) {
			if (GameControlRes.lives > 1) { // Nota: controlliamo se ha ancora vite
				GameControlRes.lives--;
				GameControlRes.resetForRespawn();
				knives.clear();
				enemies.clear();
				player.respawn(LevelConstants.FIRST_FLOOR_RIGHT_START, LevelConstants.FIRST_FLOOR_GROUND_Y);
				boss = new StickFighter(LevelConstants.FIRST_FLOOR_LEFT_STAIR, LevelConstants.FIRST_FLOOR_GROUND_Y);
				// enemies.add(boss);
				camera.position.x = player.position.x;

				deathTimer = 0;
			} else {
				// Logica Game Over definitiva
				GameControlRes.lives = 0;
				System.out.println("GAME OVER - Ritorno al Titolo");
				// Qui potresti cambiare Screen se usi la classe Game di libGDX
			}
		}
	}

	private void handleSpawning(float dt) {
		spawnTimer += dt;
		if (boss.getState() != StickFighter.State.WAITING)
			return;

		if (spawnTimer >= spawnInterval) {
			// 1. Calcoliamo l'offset fuori dallo schermo (metà camera 128 + margine 32 =
			// 160)
			float spawnOffset = 160f;

			// 2. Scegliamo casualmente se spawnare a sinistra o a destra
			boolean spawnOnRight = MathUtils.randomBoolean();
			float spawnX = spawnOnRight ? camera.position.x + spawnOffset : camera.position.x - spawnOffset;

			// 3. Limiti del Livello: Impediamo lo spawn se siamo ai confini della mappa
			// Se spawnX è fuori dai limiti FIRST_FLOOR_LEFT/RIGHT, annulliamo o correggiamo
			if (spawnX < LevelConstants.FIRST_FLOOR_RIGHT_START)
				spawnX = 10; // Evita coordinate negative
			if (spawnX > LevelConstants.FIRST_FLOOR_LEFT_STAIR)
				spawnX = 1990; // Limite ipotetico del livello

			// 4. Creazione Nemico (25% KnifeThrower, 75% Gripper)
			Enemy newEnemy;
			if (MathUtils.random() < 0.25f) {
				if (knifeThrowersSpawned < MAX_KNIFE_THROWERS_LEVEL_1) {
					newEnemy = new KnifeThrower(spawnX, LevelConstants.FIRST_FLOOR_GROUND_Y);
					knifeThrowersSpawned++;
				} else {
					// Se i KnifeThrower sono finiti, spawna un Gripper normale
					newEnemy = new Gripper(spawnX, LevelConstants.FIRST_FLOOR_GROUND_Y);
				}

			} else {
				newEnemy = new Gripper(spawnX, LevelConstants.FIRST_FLOOR_GROUND_Y);
			}

			// Importante: orientiamo il nemico verso il giocatore subito
			newEnemy.facingRight = (newEnemy.position.x < player.position.x);

			enemies.add(newEnemy);
			spawnTimer = 0;
		}
	}

	private void draw() {
		// Disegna sfondo, Thomas, Minions e infine il Boss
		float dt = Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f);

		// LOGICA DI STATO
		if (GameControlRes.lives <= 0) {
			handleGameOverInput();
		} else if (player.currentState == State.DEAD) {
			player.update(dt);
			handleDeath(dt);
		} else {
			// Unica chiamata necessaria: gestisce update, collisioni e SPAWN
			update(dt);
			// Controllo morte per energia
			if (GameControlRes.energy <= 0)
				player.triggerDeath();
		}

		// DISEGNO
		ScreenUtils.clear(0, 0, 0, 1);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(background, 0, 0);
		player.draw(batch);
		boss.draw(batch);
		for (Enemy e : enemies) {
			e.draw(batch);
		}

		// AGGIUNGI QUESTO SE MANCA:
		for (Knife k : knives) {
			k.draw(batch);
		}

		batch.end();
		if (debugMode)
			drawDebugShapes();
		hudCamera.update();
		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		drawUI();
		batch.end();

//		// 3. OVERLAY TESTUALE
		batch.begin();
		if (GameControlRes.lives <= 0) {
			font.getData().setScale(1.5f); // Scritta grande
			font.setColor(Color.RED);
			// Centra rispetto alla camera
			font.draw(batch, "GAME OVER", camera.position.x - 70, camera.position.y + 60);

			font.getData().setScale(0.5f);
			font.setColor(Color.YELLOW);
			font.draw(batch, "PRESS START (ENTER) TO RESTART", camera.position.x - 70, camera.position.y + 30);
		}
		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		background.dispose();
		shapeRenderer.dispose();
		KnifeThrowerRes.dispose();
		GripperRes.dispose();
		PlayerRes.dispose();
	}

	private void drawEnergyBars() {
		// Barra Thomas (Gialla/Rossa)
		batch.setColor(Color.RED);
		float playerEnergyWidth = (GameControlRes.energy / 100f) * 60; // Max 60px
		batch.draw(whitePixel, 10, 35, playerEnergyWidth, 8);

		// Barra Nemico (Bianca - fissa o legata al Boss)
		batch.setColor(Color.WHITE);
		batch.draw(whitePixel, 160, 35, 60, 8);// TODO legata al boss
		batch.setColor(Color.WHITE); // Reset finale
	}

	private void drawDebugShapes() {
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		// Player Hitbox (Rossa) e Hurtbox (Verde)
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(player.hitbox.x, player.hitbox.y, player.hitbox.width, player.hitbox.height);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(player.hurtbox.x, player.hurtbox.y, player.hurtbox.width, player.hurtbox.height);
		shapeRenderer.setColor(Color.RED);
	    shapeRenderer.rect(boss.stickHitbox.x, boss.stickHitbox.y, boss.stickHitbox.width, boss.stickHitbox.height);

		// Nemici (Gialla)
		shapeRenderer.setColor(Color.YELLOW);
		for (Enemy e : enemies) {
			shapeRenderer.rect(e.hurtbox.x, e.hurtbox.y, e.hurtbox.width, e.hurtbox.height);
		}
		// AGGIUNGI QUESTA RIGA: Disegna esplicitamente quella del Boss
		if (boss != null) {
		    shapeRenderer.rect(boss.hurtbox.x, boss.hurtbox.y, boss.hurtbox.width, boss.hurtbox.height);
		}

		// Coltelli (Ciano)
		shapeRenderer.setColor(Color.CYAN);
		for (Knife k : knives) {
			shapeRenderer.rect(k.hitbox.x, k.hitbox.y, k.hitbox.width, k.hitbox.height);
		}
		shapeRenderer.end();
	}

	private void drawUI() {
		// 1. FASCIA NERA SUPERIORE
		// Disegniamo da y=200 a y=256 (altezza 56 pixel) su tutta la larghezza (256)
		batch.setColor(Color.BLACK);
		batch.draw(whitePixel, 0, 0, 256, 56);
		batch.setColor(Color.WHITE); // Resetta il colore per i testi

		// 2. TESTI (Ora saranno sempre su sfondo nero)
		font.setColor(Color.RED); // Colore classico 1P/ENEMY
		font.draw(batch, "PLAYER", 10, 54);
		font.draw(batch, "ENEMY", 160, 54);
		font.draw(batch, "LIVES", 110, 20);

		font.setColor(Color.WHITE);
		font.draw(batch, String.valueOf((int) GameControlRes.lives), 115, 10);
		// Score Thomas
		font.draw(batch, String.format("%06d", (int) GameControlRes.score), 45, 54);
		// Score Massimo (High Score)
		font.draw(batch, "HI-SCORE: " + String.format("%06d", (int) GameControlRes.hiScore), 80, 50);

		// Timer al centro
		font.setColor(Color.YELLOW);
		font.draw(batch, "TIME: " + (int) GameControlRes.gameTime, 100, 30);

		// 3. BARRE ENERGIA (Metodo semplice a caratteri o rettangoli)
		drawEnergyBars();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}
}
