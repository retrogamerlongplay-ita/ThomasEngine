package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.GameInput;
import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.actors.BoomerangThrower;
import it.mpace.thomas.actors.Enemy;
import it.mpace.thomas.actors.Enemy.EnemyState;
import it.mpace.thomas.actors.Giant;
import it.mpace.thomas.actors.GrabbingEnemy;
import it.mpace.thomas.actors.Gripper;
import it.mpace.thomas.actors.KnifeThrower;
import it.mpace.thomas.actors.MrX;
import it.mpace.thomas.actors.Player;
import it.mpace.thomas.actors.Player.State;
import it.mpace.thomas.actors.Sylvia;
import it.mpace.thomas.actors.TomTom;
import it.mpace.thomas.data.LevelInfo;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.res.SylviaRes;
import it.mpace.thomas.sprite.Boomerang;
import it.mpace.thomas.sprite.FloatingScore;
import it.mpace.thomas.sprite.HitEffect;
import it.mpace.thomas.sprite.Knife;
import it.mpace.thomas.sprite.RopeFall;
import it.mpace.thomas.sprite.Trapdoor;

public class Level5Screen extends LevelScreen implements Screen {
	private Array<Enemy> minions; // Solo Gripper e KnifeThrower
	private Array<Boomerang> boomerangs = new Array<Boomerang>(); // Boomerang lanciati dal boss
	private Trapdoor trapdoor; // La botola all'inizio del livello

	private Sylvia sylvia;
	private boolean sylviaFreed = false;
	private boolean hugSequenceDone = false;

	private RopeFall rope;
	private boolean ropesSpawned = false;

	private boolean hideWorld = false;
	private float worldFade = 0f;
	private float hugAlpha = 0f;
	
	private boolean endingDone = false;

	public Level5Screen(ThomasMain g) {
		super(g);
		// Inizializzazione entità
		minions = new Array<>();
		// ... setup camera e batch ...
		background = new Texture("kungfum_5_floor.png");
		this.levelInfo = new LevelInfo();
		this.levelInfo.setFloor(5);
		this.levelInfo.setLevelBeginX(LevelConstants.FIFTH_FLOOR_RIGHT_HOLE);
		this.levelInfo.setRespawnX(LevelConstants.FIFTH_FLOOR_RIGHT_START); // Punto di partenza
		this.levelInfo.setGoalX(LevelConstants.FIFTH_FLOOR_LEFT_STAIR); // Punto di arrivo
		this.levelInfo.setGroundY(LevelConstants.FIFTH_FLOOR_GROUND_Y);
		this.levelInfo.setGripperSpeed(LevelConstants.FIFTH_FLOOR_GRIPPER_SPEED);
		this.levelInfo.setKnifeThrowerSpeed(LevelConstants.FIFTH_FLOOR_KNIFE_THROWER_SPEED);
		this.levelInfo.setKnifeSpeed(LevelConstants.FIFTH_FLOOR_KNIFE_SPEED);
		this.levelInfo.setKnifeInterval(LevelConstants.FIFTH_FLOOR_KNIFE_INTERVAL);
		this.levelInfo.setMaxKnifeThrowers(LevelConstants.MAX_KNIFE_THROWERS_FIFTH_FLOOR);
		this.levelInfo.setMaxEnemies(LevelConstants.MAX_ENEMIES_FIFTH_FLOOR);
		this.levelInfo.setSpawnOffset(LevelConstants.SPAWN_OFFSET_FIFTH_FLOOR);
		this.levelInfo.setCameraY(LevelConstants.FIFTH_FLOOR_Y);
		this.levelInfo.setSpawnInteval(LevelConstants.SPAWN_INTERVAL_FIFTH_FLOOR);
		this.levelInfo.setCeilingY(LevelConstants.FIFTH_FLOOR_CEILING);
		this.levelInfo.setBossDistance(LevelConstants.FIFTH_FLOOR_BOSS_DISTANCE);
		camera.position.x = this.levelInfo.getLevelBeginX();
		camera.position.y = this.levelInfo.getCameraY();
		trapdoor = new Trapdoor(LevelConstants.FIFTH_FLOOR_TRAPDOOR_X, LevelConstants.FIFTH_FLOOR_TRAPDOOR_Y, false);
		player = new Player(levelInfo.getLevelBeginX(), levelInfo.getGroundY(), this, levelInfo);
		System.out.println(levelInfo.toString());
		boss = new MrX(LevelConstants.FIFTH_FLOOR_LEFT_STAIR, LevelConstants.FIFTH_FLOOR_GROUND_Y);
		boss.setActive(false);

		sylvia = new Sylvia(LevelConstants.FIFTH_FLOOR_LEFT_STAIR - 40, levelInfo.getGroundY());
		sylvia.state = Sylvia.State.SITTING;

		// camera.update();
	}

	public Enemy getBoss() {
		return this.boss;
	}

	@Override
	public boolean isExceedingBoss(float x) {
		return x < this.boss.position.x;
	}

	protected void handleIntro(float dt) {
		updateCamera();
		this.introTimer += dt;
		// FASE 1: Thomas cammina da solo verso sinistra (entrata in scena)
		if (this.player.position.x > this.levelInfo.getRespawnX()) {
			if (this.player.currentState != Player.State.WALKING) {
				this.player.currentState = Player.State.WALKING;
			}
			this.player.facingRight = false;
			this.player.position.x -= GameControlRes.PLAYER_SPEED * 0.8f * dt;
		} else {
			AudioRes.stopSound(AudioRes.introWalkSound);
			// FASE 2: Thomas si ferma, aspettiamo che il timer scada
			this.player.currentState = Player.State.IDLE;
			this.introTimer += dt;
			if (this.introTimer >= READY_DURATION) {
				introActive = false; // Restituiamo il controllo al giocatore
				AudioRes.stopMusic(AudioRes.bgm_get_ready);
				AudioRes.playMusic(AudioRes.bgm_main_theme);
				// Ensure player is exactly at respawn point and camera is synced
				if (this.levelInfo != null) {
					this.player.position.x = this.levelInfo.getRespawnX();
					this.player.currentState = Player.State.IDLE;
					this.player.autoWalking = false; // make sure no auto-walk flag remains
					this.player.facingRight = false;
					this.camera.position.x = this.player.position.x;
					this.camera.update();
				}
				// Debug output to help tracing unexpected teleports
				if (GameControlRes.debugMode) {
					System.out.println(
							"[IntroEnd] PlayerX=" + this.player.position.x + " CameraX=" + this.camera.position.x
									+ " RespawnX=" + (this.levelInfo != null ? this.levelInfo.getRespawnX() : "null"));
				}
			}
		}
		this.player.updateAnimationOnly(dt); // Un metodo che aggiorna solo i frame senza leggere l'input
		this.trapdoor.update(dt);
	}

	protected void handleKnives(float dt) {
		// 5. LOGICA PROIETTILI (COLTELLI)
		for (int i = knives.size - 1; i >= 0; i--) {
			Knife k = knives.get(i);
			k.update(dt);
			// Collisione Coltello -> Thomas
			if (k.hitbox.overlaps(player.hurtbox) && player.currentState != State.DEAD) {
				// Verifica se Thomas sta schivando
				boolean hit = true;
				// Se il coltello è ALTO e Thomas è abbassato -> Schivato
				if (k.position.y > this.levelInfo.getGroundY() + 30 && player.currentState == State.CROUCHING)
					hit = false;
				// Se il coltello è BASSO e Thomas sta saltando -> Schivato
				if (k.position.y < this.levelInfo.getGroundY() + 25 && player.currentState == State.JUMPING)
					hit = false;
				if (hit) {
					player.takeHit(20f, this, k.position.x, k.position.y); // Danno consistente dai coltelli
					k.active = false; // Il coltello scompare dopo l'impatto
				}
			}
			// Rimozione coltelli fuori schermo
			if (!k.active || Math.abs(k.position.x - camera.position.x) > 200) {
				knives.removeIndex(i);
			}
		}
	}

	protected void handleGrabbingEnemies(float dt) {
		for (Enemy e : enemies) {
			if (e instanceof GrabbingEnemy) {
				// GrabbingEnemy g = (GrabbingEnemy) e;
				if (e.state == EnemyState.GRABBING && player.currentState != Player.State.DEAD) {
					e.hit(player, this); // Applichiamo il danno se è in stato di grabbing
				}
			}
		}
	}

	protected void handleBoss(float dt) {
		float dist = Math.abs(player.position.x - boss.position.x);

		// RISVEGLIO DEL BOSS
		if (!boss.isActive() && dist < 220) { // Raggio di attivazione
			boss.setActive(true);
			boss.setState(EnemyState.WALKING); // Forza l'uscita da WAITING
			// System.out.println("BOSS SECONDO PIANO SVEGLIATO!");
		}
		if (boss.isActive()) {
			if (boss instanceof Giant) {
				((Giant) boss).update(dt, player);
			}
		}
		// Logica specifica: se il boss attacca, i minions scappano
		boolean bossActive = (boss.getState() == EnemyState.WALKING);
		if (boss.getState() == EnemyState.ATTACKING_HIGH || boss.getState() == EnemyState.ATTACKING_LOW
				|| boss.getState() == EnemyState.ATTACKING_MID) {
			// Se la hitbox del bastone tocca Thomas e lui non è già morto o appena colpito
			Rectangle bHit = (boss != null) ? boss.getHitBox() : null;
			if (bHit != null && bHit.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
				float contactX = bHit.x + (bHit.width / 2);
				float contactY = bHit.y + (bHit.height / 2);
				player.takeHit(15f, this, contactX, contactY); // Danno consistente dal boss

				// Opzionale: aggiungi un piccolo rinculo a Thomas
				player.position.x += (boss.position.x > player.position.x) ? -10 : 10;
			}
		}
		if (boss.getState() != EnemyState.DEAD && !boss.isDying) {
			// Se Thomas prova ad andare troppo verso le scale (a sinistra del boss),
			// blocchiamolo
			// (Coerente con Level1Screen: manteniamo il player a destra del boss)
			if (player.position.x < boss.position.x + 5) {
				player.position.x = boss.position.x + 5;
			}
		}
		if (!boss.isDying && player.isAttacking()) { // isAttacking() controlla se Thomas è in PUNCH o KICK
			if (player.hitbox.overlaps(boss.hurtbox)) {
				// Applichiamo il danno solo se il boss non è già in animazione di "Hurt"
				// per evitare che un singolo pugno tolga 10 HP in un colpo solo
				if (boss.getState() != EnemyState.HURT_HIGH && boss.getState() != EnemyState.HURT_LOW) {
					boss.hit(player, this);
				}
			}
		}

		for (int i = minions.size - 1; i >= 0; i--) {
			Enemy m = minions.get(i);
			if (bossActive)
				m.flee();
			if (!m.isActive())
				minions.removeIndex(i);
		}
		if (boss.isActive()) {
			if (boss instanceof BoomerangThrower) {
				// CAST FONDAMENTALE: passiamo l'array dei boomerang del livello
				((BoomerangThrower) boss).update(dt, player, boomerangs);
			} else {
				// Fallback per altri tipi di boss (es. StickFighter)
				boss.update(dt, player);
			}
		}
		if (!boss.isActive() && boss.getState() == EnemyState.DEAD) {

			if (!sylviaFreed) {
				sylviaFreed = true;

				// 1) STANDING (= si alza dalla sedia)
				sylvia.x += 10;
				sylvia.state = Sylvia.State.STANDING;
				sylvia.stateTime = 0;

				// 2) Puoi aggiungere animazione corde che cadono
				// se vuoi, è qui che la disegni come sprite
			}

			if (!ropesSpawned) {
				ropesSpawned = true;

				// posizione corde vicino alla sedia (tua scelta)
				float rx = sylvia.x;
				float ry = sylvia.y + 40;

				rope = new RopeFall(rx, ry);

			}

			this.startLevelTransition(dt);
		}
	}

	protected void handleEnemies(float dt) {
		boolean isBossActive = (boss.getState() != EnemyState.WAITING && boss.getState() != EnemyState.DEAD);
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
					e.hit(player, this);
				}
			}
			// --- COLLISIONE: Nemico afferra Thomas (Solo Gripper e TomTom) ---
			if (e instanceof Gripper || e instanceof TomTom) {

				// 1. Se Thomas è MORTO, il Gripper deve mollarlo e non può più afferrarlo!
				if (player.currentState == State.DEAD) {
					if (e.state == EnemyState.GRABBING) {
						e.state = EnemyState.WALKING;
						e.position.x += (e.position.x > player.position.x) ? 15 : -15; // Spinta di distacco
					}
					// IMPORTANTE: Continua il loop senza toccare player.currentState
					continue;
				}
				if (e.state == EnemyState.GRABBING && player.currentState != Player.State.GRABBED) {
					e.state = EnemyState.WALKING; // Il Gripper lo molla
					e.stateTime = 0;
					// Spingiamo leggermente il Gripper lontano per evitare che lo riprenda
					// istantaneamente
					e.position.x += (e.position.x > player.position.x) ? 10 : -10;
				}

				if (!e.isDying && player.currentState != State.GRABBED && player.currentState != State.PUNCHING
						&& player.currentState != State.KICKING && player.currentState != State.DEAD) {
					float distanceX = Math.abs(player.position.x - e.position.x);
					if (distanceX < 12f) { // Distanza per l'abbraccio
						player.currentState = State.GRABBED;
						e.state = EnemyState.GRABBING;
						((GrabbingEnemy) e).isGrabbedFromRight = (e.position.x > player.position.x);
					}
				} else if (player.currentState == State.GRABBED && e instanceof GrabbingEnemy
						&& e.state == EnemyState.GRABBING) {
					GameControlRes.decrementEnergy(15f * dt);
				}
			}
			// Rimozione nemici morti/usciti di scena
			if (!e.active)
				enemies.removeIndex(i);
		}
	}

	@Override
	public void startLevelTransition(float dt) {
		// System.out.println("StartLevelTransition called: isLevelComplete=" +
		// isLevelComplete + " BossActive=" + boss.isActive()
		// + " PlayerX=" + player.position.x + " GoalX=" + levelInfo.getGoalX());
		if (!isLevelComplete && !boss.isActive() && player.position.x <= levelInfo.getGoalX() - 10) {
			isLevelComplete = true;
			player.autoWalking = true;
			player.facingRight = false;
			AudioRes.stopMusic(AudioRes.bgm_main_theme);
			AudioRes.playMusic(AudioRes.bgm_level_completed);
		}

//		if (isLevelComplete) {
//			transitionTimer += dt;
//			float climbProgress = player.position.y - levelInfo.getGroundY();
//			// Fail-safe: se non sale fisicamente, il timer a 3 secondi forza il cambio
//			if (climbProgress > 80 || transitionTimer > 4.0f) {
//				goToNextFloor();
//			}
//		}
	}

	@Override
	protected void handleSpawning(float dt) {
		if (enemies.size >= this.levelInfo.getMaxEnemies())
			return;
		if (boss.getState() != EnemyState.WAITING)
			return;

		spawnTimer += dt;
		if (spawnTimer >= this.levelInfo.getSpawnInteval()) {

			float trapOffset = MathUtils.random(20, 80);
			boolean ahead = MathUtils.randomBoolean();
			float spawnX = ahead ? player.position.x + trapOffset : player.position.x - trapOffset;

			Enemy newEnemy;

			float humanOffset = this.levelInfo.getSpawnOffset(); // 160f
			boolean spawnOnRight = MathUtils.randomBoolean();
			spawnX = spawnOnRight ? camera.position.x + humanOffset : camera.position.x - humanOffset;

			float rand = MathUtils.random();
			if (rand > 0.33f && rand < 0.6f) {
				newEnemy = new Gripper(spawnX, levelInfo.getGroundY());
			} else if (rand > 0.33f && rand < 0.66f) {
				newEnemy = new KnifeThrower(spawnX, levelInfo.getGroundY());
			} else {
				newEnemy = new TomTom(spawnX, levelInfo.getGroundY(), this); // Il nemico basso/nano
			}

			newEnemy.facingRight = (newEnemy.position.x < player.position.x);
			enemies.add(newEnemy);
			spawnTimer = 0;
		}
	}

	@Override
	public void update(float dt) {
		super.update(dt);

		if (sylviaFreed && sylvia.state == Sylvia.State.STANDING && rope.getLanded()) {

			if (Math.abs(player.position.x - sylvia.x) < 120f) {
				sylvia.state = Sylvia.State.RUNNING;
			}
		}

		sylvia.update(dt, player.position.x);

		if (sylvia.state == Sylvia.State.HUGGING && !hugSequenceDone) {
			hugAlpha = 0f; // parte trasparente
			hugSequenceDone = true;

			hideWorld = true;
			worldFade = 0f;

			player.currentState = Player.State.IDLE;
			player.position.x = sylvia.x - 10;
			player.facingRight = true;

			// musica finale
			AudioRes.stopMusic(AudioRes.bgm_main_theme);
			AudioRes.playMusic(AudioRes.bgm_game_completed);

			// puoi iniziare un fadeout
			isLevelComplete = true;
			GameControlRes.timeElapsing=false;
			this.player.setPlayerVisible(false);
			this.player.hurtbox = new Rectangle(0,0,0,0);
		}

		

		if (sylvia.state == Sylvia.State.HUGGING) {
			hugAlpha += dt * 0.09f; // velocità fade: 1.2 secondi
			if (hugAlpha > 1f)
				hugAlpha = 1f;
		}

		if (rope != null) {
			rope.update(dt, LevelConstants.FIFTH_FLOOR_GROUND_Y);
//			if (!rope.getLanded())
//				rope = null;
		}

		if (hideWorld) {
			worldFade += dt * 0.09f; // velocità fade-out
			if (worldFade > 1f)
				worldFade = 1f;
		}
		
		// Quando il frame dell’abbraccio è completamente visibile
		if (sylvia.state == Sylvia.State.HUGGING && hugAlpha >= 1f && !endingDone) {

		    endingDone = true;  // aggiungi questa variabile in Level5Screen

		    // SALVA IL RECORD SE È ALTO
		    GameControlRes.saveHighScore((int)GameControlRes.score);

		    // Se è un nuovo High Score → vai alla schermata di inserimento nome
		    if (GameControlRes.isHighScore((int)GameControlRes.score)) {
		        game.setScreen(new NameEntryScreen(game, (int)GameControlRes.score));
		        return;
		    }

		    // Altrimenti → normalissimo Game Over screen
		    game.setScreen(new GameOverScreen(game));
		    return;
		}


	}

//	private void goToNextFloor() {
//		player.autoWalking = false;
//		game.setScreen(new Level4Screen(this.game)); // Dovrai creare questa classe
//	}

	public void draw(float delta) {

		if (hideWorld) {

			// 1. Disegna comunque il mondo, MA con alpha scalata
			float fadeAlpha = 1f - worldFade;
			if (fadeAlpha < 0f)
				fadeAlpha = 0f;

			batch.setColor(1f, 1f, 1f, fadeAlpha);

			// → qui NON interrompiamo il draw, perché vogliamo che
			// il mondo venga disegnato con trasparenza
		}

		// Disegna sfondo, Thomas, Minions e infine il Boss
		float dt = Math.min(delta, 1 / 60f); // Math.min(Gdx.graphics.getDeltaTime(), 1 / 60f);

		// LOGICA DI STATO
		if (GameControlRes.lives <= 0) {
			handleGameOver();
		} else if (player.currentState == State.DEAD) {
			for (int i = enemies.size - 1; i >= 0; i--) {
				Enemy e = enemies.get(i);
				if (e instanceof GrabbingEnemy) {
					((GrabbingEnemy) e).state = EnemyState.FLEEING;
				}
			}
			player.update(dt);
			handleDeath(dt);
		} else {
			// Unica chiamata necessaria: gestisce update, collisioni e SPAWN
			if (!introActive) {
				player.update(dt);
			}
			// Controllo morte per energia
			if (GameControlRes.energy <= 0)
				player.triggerDeath();
		}
		// DISEGNO
		ScreenUtils.clear(0, 0, 0, 1);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(background, 0, 0);
		// Draw the trapdoor (so it's visible during intro)
		if (trapdoor != null) {
			trapdoor.draw(batch);
		}
		player.draw(batch);
		boss.draw(batch);
		for (Enemy e : enemies) {
			e.draw(batch);
		}

		// AGGIUNGI QUESTO SE MANCA:
		for (Knife k : knives) {
			k.draw(batch);
		}

		for (int i = hitEffects.size - 1; i >= 0; i--) {
			HitEffect he = hitEffects.get(i);
			he.update(dt);
			if (!he.active)
				hitEffects.removeIndex(i);
			else
				he.draw(batch);
		}

		for (int i = floatingScores.size - 1; i >= 0; i--) {
			FloatingScore fs = floatingScores.get(i);
			fs.update(dt);
			if (!fs.active)
				floatingScores.removeIndex(i);
			else
				fs.draw(batch, font);
		}
		batch.setColor(1f, 1f, 1f, 1f);

		sylvia.draw(batch);

		if (rope != null)
			rope.draw(batch);

		batch.end();
		if (GameControlRes.debugMode)
			drawDebugShapes();
		hudCamera.update();
		batch.setProjectionMatrix(hudCamera.combined);

		batch.begin();
		drawUI();
		batch.end();

		// 3. OVERLAY TESTUALE
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

		if (hideWorld && worldFade >= 1f) {

			// Cancella eventuale residuo
			ScreenUtils.clear(0, 0, 0, 1);

			batch.setProjectionMatrix(hudCamera.combined);
			batch.begin();

			// Calcola centro
			TextureRegion hug = SylviaRes.hugframe;
			float w = hug.getRegionWidth();
			float h = hug.getRegionHeight();

			float cx = 128 - w / 2;
			float cy = 128 - h / 2;

			// fade-in dell’abbraccio (hugAlpha lo hai già)
			batch.setColor(1f, 1f, 1f, hugAlpha);
			batch.draw(hug, cx, cy, w, h);
			batch.setColor(1f, 1f, 1f, 1f);

			batch.end();
			return; // IMPORTANTE: impedisce al resto di disegnarsi
		}

	}

	@Override
	public void dispose() {
		background.dispose();
		shapeRenderer.dispose();
		this.whitePixel.dispose();
	}

	@Override
	public void show() {
		trapdoor.position.x = LevelConstants.THIRD_FLOOR_TRAPDOOR_X;
		player.position.x = (this.levelInfo != null) ? this.levelInfo.getLevelBeginX()
				: LevelConstants.THIRD_FLOOR_RIGHT;
		// During the intro Thomas will walk left toward the respawn X, so set
		// facingRight = false
		player.facingRight = false;
		camera.position.x = player.position.x;
		AudioRes.loopSound(AudioRes.introWalkSound);
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

	@Override
	public void killAllEnemies() {
		super.killAllEnemies();
		if (this.minions != null)
			this.minions.clear();
		if (this.boomerangs != null)
			this.boomerangs.clear();
	}
}
