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

import it.mpace.thomas.Enemy.EnemyState;
import it.mpace.thomas.Player.State;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.GripperRes;
import it.mpace.thomas.res.KnifeThrowerRes;
import it.mpace.thomas.res.PlayerRes;

public class Level1Screen extends LevelScreen implements Screen {
	private Array<Enemy> minions; // Solo Gripper e KnifeThrower

	private float spawnTimer;
	private float deathTimer;
	private float spawnInterval = 2.5f; // Un nemico ogni 2.5 secondi


	public static boolean debugMode = true; // Flag per attivare/disattivare il debug



	private int knifeThrowersSpawned = 0;
	private final int MAX_KNIFE_THROWERS_LEVEL_1 = 2;
	private ThomasMain game=null;
	



	public Level1Screen(ThomasMain g) {
		this.game=g;
		// Inizializzazione entità
		minions = new Array<>();
		// ... setup camera e batch ...
		batch = new SpriteBatch();
		background = new Texture("kungfum_1_floor.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 256, 256);
		camera.position.x = LevelConstants.FIRST_FLOOR_RIGHT_START;
		camera.position.y = LevelConstants.FIRST_FLOOR_Y;
		player = new Player(LevelConstants.FIRST_FLOOR_RIGHT_START, LevelConstants.FIRST_FLOOR_GROUND_Y, this);
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
	
	public Enemy getBoss() {
		return this.boss;
	}

	
	public void update(float dt) {
		if (introActive) {
			updateCamera();
	        introTimer += dt;
	       
	        
	        // FASE 1: Thomas cammina da solo verso destra (entrata in scena)
	        if (player.position.x > LevelConstants.FIRST_FLOOR_RIGHT_START) {
	            player.currentState = Player.State.WALKING;
	            player.facingRight = false;
	            player.position.x -= GameControlRes.PLAYER_SPEED * 0.8f * dt;
	        } else {
	            // FASE 2: Thomas si ferma, aspettiamo che il timer scada
	            player.currentState = Player.State.IDLE;
	            introTimer += dt;
	            if (introTimer >= READY_DURATION) {
	                introActive = false; // Restituiamo il controllo al giocatore
	                AudioRes.stopMusic( AudioRes.bgm_get_ready);
	                AudioRes.playMusic(AudioRes.bgm_main_theme);
	            }
	        }
	        
	        player.updateAnimationOnly(dt); // Un metodo che aggiorna solo i frame senza leggere l'input
	        return; // Salta il resto dell'update (nemici, timer di gioco, ecc.)
	    }
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
				if (e instanceof Gripper && ((Gripper) e).state == EnemyState.GRABBING) {
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

		boolean isBossActive = (boss.getState() != EnemyState.WAITING
				&& boss.getState() != EnemyState.DEAD);

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
				if (player.currentState == State.DEAD && g.state == EnemyState.GRABBING) {
					g.state = EnemyState.WALKING; // O FLEEING se preferisci
					// Opzionale: dai una piccola spinta all'indietro al nemico per "distacco"
					g.position.x += (g.position.x > player.position.x) ? 10 : -10;
				} else if (!e.isDying && player.currentState != State.GRABBED && player.currentState != State.PUNCHING
						&& player.currentState != State.KICKING && player.currentState != State.DEAD) {
					float distanceX = Math.abs(player.position.x - e.position.x);
					if (distanceX < 12f) { // Distanza per l'abbraccio
						player.currentState = State.GRABBED;
						((Gripper) e).state = EnemyState.GRABBING;
						((Gripper) e).isGrabbedFromRight = (e.position.x > player.position.x);
					}
				} else if (player.currentState == State.GRABBED && e instanceof Gripper
						&& ((Gripper) e).state == EnemyState.GRABBING) {
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
		boolean bossActive = (boss.getState() == EnemyState.WALKING);

		if (boss.getState() == EnemyState.ATTACKING_HIGH||boss.getState() == EnemyState.ATTACKING_LOW||boss.getState() == EnemyState.ATTACKING_MID) {
			// Se la hitbox del bastone tocca Thomas e lui non è già morto o appena colpito
			if (boss.getHitBox().overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
				player.takeHit(15f); // Danno consistente dal boss
				// Opzionale: aggiungi un piccolo rinculo a Thomas
				player.position.x += (boss.position.x > player.position.x) ? -10 : 10;
			}
		}
		
		if (boss.getState() != EnemyState.DEAD && !boss.isDying) {
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
		        if (boss.getState() != EnemyState.HURT_HIGH && 
		            boss.getState() != EnemyState.HURT_LOW) {
		            
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
		
		if (boss.isActive()) {
		    boss.update(dt, player);
		}
		
		if (!boss.isActive() && boss.getState() == EnemyState.DEAD) {
		    // Il Boss è sparito: le scale sono ora accessibili
		    this.startLevelTransition(dt);
		}

		//boss.update(dt, player);
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

	private void handleGameOver() {
		AudioRes.stopMusic(AudioRes.bgm_main_theme);
		AudioRes.playMusic(AudioRes.bgm_game_over);
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
				 game.setScreen(new GameOverScreen(game));
				// Qui potresti cambiare Screen se usi la classe Game di libGDX
			}
		}
	}

	private void handleSpawning(float dt) {
		// 1. Limite massimo di nemici a schermo
	    if (enemies.size >= 5) return; 
		spawnTimer += dt;
		if (boss.getState() != EnemyState.WAITING)
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

	public void draw(float delta) {
		// Disegna sfondo, Thomas, Minions e infine il Boss
		float dt = Math.min(delta,1 / 60f); // Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f);

		// LOGICA DI STATO
		if (GameControlRes.lives <= 0) {
			handleGameOver();
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
		
		if (isLevelComplete) {
		    batch.begin();
		    batch.setProjectionMatrix(hudCamera.combined);
		    // Calcola l'opacità in base al timer (da 0 a 1 in 2 secondi)
		    float alpha = Math.min(transitionTimer / 2f, 1f);
		    batch.setColor(0, 0, 0, alpha); // Nero con trasparenza variabile
		    batch.draw(whitePixel, 0, 0, 256, 256);
		    batch.setColor(Color.WHITE);
		    batch.end();
		}
		
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

	public void startLevelTransition(float dt) {
	    // Se il boss è morto e Thomas raggiunge il bordo sinistro
		AudioRes.stopMusic(AudioRes.bgm_main_theme);
		AudioRes.playMusic(AudioRes.bgm_level_completed);
	    if (!boss.isActive() && player.position.x <= LevelConstants.FIRST_FLOOR_LEFT_STAIR + 10) {
	        if (!isLevelComplete) {
	            isLevelComplete = true;
	            player.autoWalking = true; // Thomas prende il controllo automatico
	            player.facingRight = false;
	        }
	    }
	    
	 // Calcoliamo quanto Thomas è salito rispetto al punto di inizio scala
	    float climbProgress = player.position.y - LevelConstants.FIRST_FLOOR_GROUND_Y;
	    
	    // Iniziamo il Fade Out solo dopo che è salito di almeno 30 pixel
	    if (climbProgress > 30) {
	        transitionTimer += dt; // Incrementiamo il timer per il fade
	    }

	    // Il cambio schermo avviene solo quando Thomas è "uscito" o il fade è totale
	    if (climbProgress > 80 || transitionTimer > 2.5f) {
	        // Logica di cambio piano (es. setScreen(new Level2Screen()))
	        System.out.println("PIANO COMPLETATO!");
	        goToNextFloor();
	    }

	    
	}
	
	private void goToNextFloor() {
	    // Reset dello stato player per il prossimo livello
	    player.autoWalking = false;
	    
	    // Passaggio al secondo piano (Floor 2)
	    // Assicurati che ThomasMain sia accessibile tramite Gdx.app.getApplicationListener()
	    ThomasMain game = (ThomasMain) Gdx.app.getApplicationListener();
	    game.setScreen(new Level2Screen()); // Dovrai creare questa classe
	}
	

	@Override
	public void show() {
		 player.position.x = 1775; //per la intro
		 AudioRes.playMusic(AudioRes.bgm_get_ready);
		 GameInput inputProcessor = new GameInput(player);
		 Gdx.input.setInputProcessor(inputProcessor);
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
