package it.mpace.thomas.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import it.mpace.thomas.GameInput;
import it.mpace.thomas.LevelConstants;
import it.mpace.thomas.ThomasMain;
import it.mpace.thomas.actors.Enemy;
import it.mpace.thomas.actors.Enemy.EnemyState;
import it.mpace.thomas.actors.Gripper;
import it.mpace.thomas.actors.KnifeThrower;
import it.mpace.thomas.actors.Player;
import it.mpace.thomas.actors.Player.State;
import it.mpace.thomas.actors.StickFighter;
import it.mpace.thomas.data.LevelInfo;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.sprite.FloatingScore;
import it.mpace.thomas.sprite.HitEffect;
import it.mpace.thomas.sprite.Knife;

public class Level1Screen extends LevelScreen implements Screen {
	private Array<Enemy> minions; // Solo Gripper e KnifeThrower

	public Level1Screen(ThomasMain g) {
		super(g);
		minions = new Array<>();
		background = new Texture("kungfum_1_floor.png");
		this.levelInfo = new LevelInfo();
		levelInfo.setFloor(1);
		levelInfo.setGroundY(LevelConstants.FIRST_FLOOR_GROUND_Y);
		levelInfo.setGoalX(LevelConstants.FIRST_FLOOR_LEFT_STAIR);
		levelInfo.setGripperSpeed(LevelConstants.FIRST_FLOOR_GRIPPER_SPEED);
		levelInfo.setLevelBeginX(LevelConstants.FIRST_FLOOR_RIGHT);
		levelInfo.setRespawnX(LevelConstants.FIRST_FLOOR_RIGHT_START);
		levelInfo.setKnifeThrowerSpeed(LevelConstants.FIRST_FLOOR_KNIFE_THROWER_SPEED);
		levelInfo.setKnifeSpeed(LevelConstants.FIRST_FLOOR_KNIFE_SPEED);
		levelInfo.setKnifeInterval(LevelConstants.FIRST_FLOOR_KNIFE_INTERVAL);
		levelInfo.setMaxKnifeThrowers(LevelConstants.MAX_KNIFE_THROWERS_FIRST_FLOOR);
		levelInfo.setMaxEnemies(LevelConstants.MAX_ENEMIES_FIRST_FLOOR);
		levelInfo.setSpawnOffset(LevelConstants.SPAWN_OFFSET_FIRST_FLOOR);
		levelInfo.setCameraY(LevelConstants.FIRST_FLOOR_Y);
		levelInfo.setSpawnInteval(LevelConstants.SPAWN_INTERVAL_FIRST_FLOOR);
		levelInfo.setBossDistance(LevelConstants.FIRST_FLOOR_BOSS_DISTANCE);
		camera.position.x = this.levelInfo.getRespawnX();
		camera.position.y = this.levelInfo.getCameraY();
		player = new Player(levelInfo.getLevelBeginX(), levelInfo.getGroundY(), this, levelInfo);
		boss = new StickFighter(LevelConstants.FIRST_FLOOR_LEFT_STAIR, LevelConstants.FIRST_FLOOR_GROUND_Y);
		font.getData().setScale(0.5f); // Rimpicciolisci per la tua camera 256x256
	}

	public Enemy getBoss() {
		return this.boss;
	}
	
	

	@Override
	public boolean isExceedingBoss(float x) {
		return x < this.boss.position.x + 10;
	}

	protected void handleIntro(float dt) {
		updateCamera();
		this.introTimer += dt;
		// FASE 1: Thomas cammina da solo verso sinistra (entrata in scena)
		if (this.player.position.x > this.levelInfo.getRespawnX()) {
			if (this.player.currentState != Player.State.WALKING) {
				this.player.currentState = Player.State.WALKING;
				//AudioRes.playSound(AudioRes.walk);
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
				//AudioRes.stopMusic(AudioRes.bgm_get_ready);
				AudioRes.playMusic(AudioRes.bgm_main_theme);
			}
		}
		this.player.updateAnimationOnly(dt); // Un metodo che aggiorna solo i frame senza leggere l'input

	}

	protected void handleGrabbingEnemies(float dt) {
		for (Enemy e : enemies) {
			if (e instanceof Gripper) {
				Gripper g = (Gripper) e;

				if (g.state == EnemyState.GRABBING && player.currentState != Player.State.DEAD) {
					g.hit(player, this); // Applichiamo il danno se è in stato di grabbing
				}
			}
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
			// --- COLLISIONE: Nemico afferra Thomas (Solo Gripper) ---
			if (e instanceof Gripper) {
				Gripper g = (Gripper) e;
				// 1. Se Thomas è MORTO, il Gripper deve mollarlo e non può più afferrarlo!
				if (player.currentState == State.DEAD) {
					if (g.state == EnemyState.GRABBING) {
						g.state = EnemyState.WALKING;
						g.position.x += (g.position.x > player.position.x) ? 15 : -15; // Spinta di distacco
					}
					// IMPORTANTE: Continua il loop senza toccare player.currentState
					continue;
				}
				if (g.state == EnemyState.GRABBING && player.currentState != State.GRABBED) {
					g.state = EnemyState.WALKING; // Il Gripper lo molla
					g.stateTime = 0;
					// Spingiamo leggermente il Gripper lontano per evitare che lo riprenda
					// istantaneamente
					g.position.x += (g.position.x > player.position.x) ? 10 : -10;
				}
				if (!e.isDying && player.currentState != State.GRABBED && player.currentState != State.PUNCHING
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

	protected void handleBoss(float dt) {
//		if (boss.getState() == EnemyState.DEAD || boss.isDying) {
//			return;
//		}
		// Logica specifica: se il boss attacca, i minions scappano
		boolean bossActive = (boss.getState() == EnemyState.WALKING);
		float dist = Math.abs(player.position.x - boss.position.x);
		if (!boss.isActive() && dist < 200 && boss.getState() != EnemyState.DEAD && !boss.isDying) { // 200 è il raggio
																										// visivo
			boss.setActive(true);
			boss.setState(EnemyState.WALKING); // Forza lo stato di movimento
		}
		if (boss.getState() == EnemyState.ATTACKING_HIGH || boss.getState() == EnemyState.ATTACKING_LOW
				|| boss.getState() == EnemyState.ATTACKING_MID) {
			// Se la hitbox del bastone tocca Thomas e lui non è già morto o appena colpito
			Rectangle bHit = boss.getHitBox();
			if (bHit != null && bHit.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
				float contactX = bHit.x + (bHit.width / 2);
				float contactY = bHit.y + (bHit.height / 2);
				player.takeHit(15f, this, contactX, contactY); // Danno consistente dal boss

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
			boss.update(dt, player);
		}
		if (!boss.isActive() && boss.getState() == EnemyState.DEAD) {
			// Il Boss è sparito: le scale sono ora accessibili
			this.startLevelTransition(dt);
		}
	}

	public void draw(float delta) {
		// Disegna sfondo, Thomas, Minions e infine il Boss
		float dt = Math.min(delta, 1 / 60f);

		// LOGICA DI STATO
		if (GameControlRes.lives <= 0) {
			handleGameOver();
		} else if (player.currentState == State.DEAD) {
			GameControlRes.energy = 0;
			for (int i = enemies.size - 1; i >= 0; i--) {
				Enemy e = enemies.get(i);
				if (e instanceof Gripper) {
					((Gripper) e).state = EnemyState.FLEEING; // I Gripper scappano via
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
		player.draw(batch);
		boss.draw(batch);
		for (Enemy e : enemies) {
			e.draw(batch);
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

		// AGGIUNGI QUESTO SE MANCA:
		for (Knife k : knives) {
			k.draw(batch);
		}

		batch.end();
		if (GameControlRes.debugMode)
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

	protected void handleSpawning(float dt) {
		// 1. Limite massimo di nemici a schermo
		if (enemies.size >= this.levelInfo.getMaxEnemies())
			return;
		spawnTimer += dt;
		if (boss.getState() != EnemyState.WAITING)
			return;

		if (spawnTimer >= this.levelInfo.getSpawnInteval()) {
			float spawnOffset = this.levelInfo.getSpawnOffset(); // 160 pixel fuori dallo schermo

			// 2. Scegliamo casualmente se spawnare a sinistra o a destra
			boolean spawnOnRight = MathUtils.randomBoolean();
			float spawnX = spawnOnRight ? camera.position.x + spawnOffset : camera.position.x - spawnOffset;

			// 3. Limiti del Livello: Impediamo lo spawn se siamo ai confini della mappa
			// Se spawnX è fuori dai limiti FIRST_FLOOR_LEFT/RIGHT, annulliamo o correggiamo
			if (this.levelInfo.isExceedingLevelBegin(spawnX)) // Se spawnX è troppo a destra
				spawnX = this.levelInfo.getLevelBeginX(); // Evita coordinate negative
			if (this.levelInfo.isExceedingLevelGoal(spawnX))
				spawnX = this.levelInfo.getGoalX(); // Limite ipotetico del livello

			// 4. Creazione Nemico (25% KnifeThrower, 75% Gripper)
			Enemy newEnemy;
			if (MathUtils.random() < 0.25f) {
				if (knifeThrowersSpawned < this.levelInfo.getMaxKnifeThrowers()) {
					newEnemy = new KnifeThrower(spawnX, this.levelInfo.getGroundY());
					knifeThrowersSpawned++;
				} else {
					// Se i KnifeThrower sono finiti, spawna un Gripper normale
					newEnemy = new Gripper(spawnX, this.levelInfo.getGroundY());
				}

			} else {
				newEnemy = new Gripper(spawnX, this.levelInfo.getGroundY());
			}

			// Importante: orientiamo il nemico verso il giocatore subito
			newEnemy.facingRight = (newEnemy.position.x < player.position.x);
			enemies.add(newEnemy);
			spawnTimer = 0;
		}
	}

	@Override
	public void dispose() {
		//batch.dispose();
		background.dispose();
		shapeRenderer.dispose();
		this.whitePixel.dispose();
//		KnifeThrowerRes.dispose();
//		GripperRes.dispose();
//		PlayerRes.dispose();
	}

	public void startLevelTransition(float dt) {
		// 1. Innesco della transizione (Boss morto e Thomas alle scale)
		if (!isLevelComplete && !boss.isActive() && player.position.x <= LevelConstants.FIRST_FLOOR_LEFT_STAIR + 10) {
			isLevelComplete = true;
			player.autoWalking = true;
			player.facingRight = false;

			// Audio eseguito UNA SOLA VOLTA
			AudioRes.stopMusic(AudioRes.bgm_main_theme);
			AudioRes.playMusic(AudioRes.bgm_level_completed);
		}

		// 2. Gestione progressione
		if (isLevelComplete) {
			// Il timer deve avanzare SEMPRE una volta che il livello è completo
			transitionTimer += dt;

			float climbProgress = player.position.y - this.levelInfo.getGroundY();

			// Usciamo se Thomas è salito abbastanza O se il tempo scade (sicurezza)
			if (climbProgress > 80 || transitionTimer > 4.0f) {
				System.out.println("PIANO COMPLETATO!");
				goToNextFloor();
			}
		}
	}

	private void goToNextFloor() {
		// Reset dello stato player per il prossimo livello
		player.autoWalking = false;
		// Passaggio al secondo piano (Floor 2)
		ThomasMain game = (ThomasMain) Gdx.app.getApplicationListener();
		game.setScreen(new Level2Screen(this.game)); // Dovrai creare questa classe
	}

	@Override
	public void show() {
		player.position.x = this.levelInfo.getLevelBeginX(); // per la intro
		//AudioRes.playMusic(AudioRes.bgm_get_ready);
		//AudioRes.playMusic(AudioRes.bgm_intro_walk);
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
		if (this.minions != null) this.minions.clear();
	}
}
