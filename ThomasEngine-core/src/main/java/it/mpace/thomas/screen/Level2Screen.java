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
import it.mpace.thomas.actors.BoomerangThrower;
import it.mpace.thomas.actors.Dragon;
import it.mpace.thomas.actors.Enemy;
import it.mpace.thomas.actors.Enemy.EnemyState;
import it.mpace.thomas.actors.ExplodingBall;
import it.mpace.thomas.actors.GrabbingEnemy;
import it.mpace.thomas.actors.Gripper;
import it.mpace.thomas.actors.KnifeThrower;
import it.mpace.thomas.actors.Player;
import it.mpace.thomas.actors.Player.State;
import it.mpace.thomas.actors.Snake;
import it.mpace.thomas.actors.TomTom;
import it.mpace.thomas.data.LevelInfo;
import it.mpace.thomas.res.AudioRes;
import it.mpace.thomas.res.GameControlRes;
import it.mpace.thomas.sprite.Boomerang;
import it.mpace.thomas.sprite.FloatingScore;
import it.mpace.thomas.sprite.HitEffect;
import it.mpace.thomas.sprite.Knife;
import it.mpace.thomas.sprite.PotProjectile;
import it.mpace.thomas.sprite.Trapdoor;

public class Level2Screen extends LevelScreen implements Screen {
    private Array<Enemy> minions; // Solo Gripper e KnifeThrower
    private Array<Boomerang> boomerangs = new Array<Boomerang>(); // Boomerang lanciati dal boss
    Trapdoor trapdoor; // La botola all'inizio del livello

    public Level2Screen(ThomasMain g) {
        super(g);
        // Inizializzazione entità
        minions = new Array<>();
        // ... setup camera e batch ...
        background = new Texture("kungfum_2_floor.png");
        this.levelInfo = new LevelInfo();
        this.levelInfo.setFloor(2);
        this.levelInfo.setLevelBeginX(LevelConstants.SECOND_FLOOR_LEFT);
        this.levelInfo.setRespawnX(LevelConstants.SECOND_FLOOR_LEFT_START); // Punto di partenza
        this.levelInfo.setGoalX(LevelConstants.SECOND_FLOOR_RIGHT_STAIR); // Punto di arrivo
        this.levelInfo.setGroundY(LevelConstants.SECOND_FLOOR_GROUND_Y);
        this.levelInfo.setGripperSpeed(LevelConstants.SECOND_FLOOR_GRIPPER_SPEED);
        this.levelInfo.setKnifeThrowerSpeed(LevelConstants.SECOND_FLOOR_KNIFE_THROWER_SPEED);
        this.levelInfo.setKnifeSpeed(LevelConstants.SECOND_FLOOR_KNIFE_SPEED);
        this.levelInfo.setKnifeInterval(LevelConstants.SECOND_FLOOR_KNIFE_INTERVAL);
        this.levelInfo.setMaxKnifeThrowers(LevelConstants.MAX_KNIFE_THROWERS_SECOND_FLOOR);
        this.levelInfo.setMaxEnemies(LevelConstants.MAX_ENEMIES_SECOND_FLOOR);
        this.levelInfo.setSpawnOffset(LevelConstants.SPAWN_OFFSET_SECOND_FLOOR);
        this.levelInfo.setCameraY(LevelConstants.SECOND_FLOOR_Y);
        this.levelInfo.setSpawnInteval(LevelConstants.SPAWN_INTERVAL_SECOND_FLOOR);
        this.levelInfo.setCeilingY(LevelConstants.SECOND_FLOOR_CEILING);
        this.levelInfo.setBossDistance(LevelConstants.SECOND_FLOOR_BOSS_DISTANCE);
        camera.position.x = this.levelInfo.getLevelBeginX();
        camera.position.y = this.levelInfo.getCameraY();
        trapdoor = new Trapdoor(LevelConstants.SECOND_FLOOR_TRAPDOOR_X, LevelConstants.SECOND_FLOOR_TRAPDOOR_Y, true);
        player = new Player(levelInfo.getLevelBeginX(), levelInfo.getGroundY(), this, levelInfo);
        System.out.println(levelInfo.toString());
        boss = new BoomerangThrower(LevelConstants.SECOND_FLOOR_RIGHT_STAIR, LevelConstants.SECOND_FLOOR_GROUND_Y);
        boss.setActive(false);
        // camera.update();
    }

    public Enemy getBoss() {
        return this.boss;
    }
    
    @Override
    public boolean isExceedingBoss(float x) {
        return x > this.boss.position.x;
    }

    protected void handleIntro(float dt) {
        updateCamera();
        this.introTimer += dt;
        // FASE 1: Thomas cammina da solo verso sinistra (entrata in scena)
        if (this.player.position.x < this.levelInfo.getRespawnX()) {
            if (this.player.currentState != Player.State.WALKING) {
                this.player.currentState = Player.State.WALKING;
            }
            this.player.facingRight = true; // Thomas guarda verso destra mentre cammina
            this.player.position.x += GameControlRes.PLAYER_SPEED * 0.8f * dt;
        } else {
            // FASE 2: Thomas si ferma, aspettiamo che il timer scada
        	AudioRes.stopSound(AudioRes.introWalkSound);
            this.player.currentState = Player.State.IDLE;
            this.introTimer += dt;
            if (this.introTimer >= READY_DURATION) {
                introActive = false; // Restituiamo il controllo al giocatore
                AudioRes.playMusic(AudioRes.bgm_main_theme);
                if (this.levelInfo != null) {
					this.player.position.x = this.levelInfo.getRespawnX();
					this.player.currentState = Player.State.IDLE;
					this.player.autoWalking = false; // make sure no auto-walk flag remains
					//this.player.facingRight = false;
					this.camera.position.x = this.player.position.x;
					this.camera.update();
				}
            }
        }
        this.player.updateAnimationOnly(dt); // Un metodo che aggiorna solo i frame senza leggere l'input
        this.trapdoor.update(dt);
        return; // Salta il resto dell'update (nemici, timer di gioco, ecc.)
    }

    private void handleBoomerangs(float dt) {
        for (int i = boomerangs.size - 1; i >= 0; i--) {
            Boomerang b = boomerangs.get(i);
            b.update(dt);

            // --- NUOVA LOGICA DI COLLISIONE ---
            if (b.hitbox.overlaps(player.hurtbox) && player.currentState != Player.State.DEAD) {
                // Verifica schivata (simile ai coltelli)
                boolean hit = true;
                // Boomerang ALTO: schivato se accovacciato
                if (b.position.y > levelInfo.getGroundY() + 30 && player.currentState == Player.State.CROUCHING) hit = false;
                // Boomerang BASSO: schivato se in salto
                if (b.position.y < levelInfo.getGroundY() + 25 && player.currentState == Player.State.JUMPING) hit = false;

                if (hit) {
                    player.takeHit(15f, this, b.position.x, b.position.y + 10);
                    b.active = false; // Il boomerang sparisce all'impatto (o continua se preferisci)
                }
            }

            if (!b.active || b.position.x > levelInfo.getGoalX() + 50 || b.position.x < levelInfo.getLevelBeginX() - 50) {
                boomerangs.removeIndex(i);
            }
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

    protected void handleGrabbingEnemies(float dt) {
        for (Enemy e : enemies) {
            if (e instanceof GrabbingEnemy) {
                //GrabbingEnemy g = (GrabbingEnemy) e;
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
            //System.out.println("BOSS SECONDO PIANO SVEGLIATO!");
        }
        if (boss.isActive()) {
            if (boss instanceof BoomerangThrower) {
                ((BoomerangThrower) boss).update(dt, player, boomerangs);
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
            // Se Thomas prova ad andare a SINISTRA del Boss (verso le scale)
            if (player.position.x > boss.position.x - 5) {
                player.position.x = boss.position.x - 5;
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
            // Il Boss è sparito: le scale sono ora accessibili
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
            if (e instanceof Gripper|| e instanceof TomTom) {
                
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
        // Previously we checked boss.isActive() which can remain true while the boss is in dying physics
        // (flying off-screen). Use boss.getState() == DEAD to trigger the transition as soon as the boss
        // has been defeated and the player reached the goal.
        if (!isLevelComplete && !boss.isActive() && player.position.x >= levelInfo.getGoalX() - 10) {
            isLevelComplete = true;
            player.autoWalking = true;
            player.facingRight = true; // Guarda a destra mentre sale
            AudioRes.stopMusic(AudioRes.bgm_main_theme);
            AudioRes.playMusic(AudioRes.bgm_level_completed);
        }
        if (isLevelComplete) {
            transitionTimer += dt;
            float climbProgress = player.position.y - levelInfo.getGroundY();
            // Fail-safe: se non sale fisicamente, il timer a 3 secondi forza il cambio
            if (climbProgress > 80 || transitionTimer > 3.0f) {
                goToNextFloor();
            }
        }
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

            // FASE 1: Trappole (Draghi, Serpenti, Palle)
            if (player.position.x < LevelConstants.SECOND_FLOOR_HALF) {
                //System.out.println("Spawning trap enemy at X: " + spawnX + "," + levelInfo.getCeilingY()
                //      + ", Player X: " + player.position.x + "," + player.position.y);
                float rand = MathUtils.random();
                if (rand < 0.33f) {
                    newEnemy = new Snake(spawnX, levelInfo.getCeilingY(), levelInfo.getGroundY(), this); // Striscia
                                                                    // basso
                } else if (rand > 0.33f && rand < 0.66f) {
                    newEnemy = new Dragon(spawnX, levelInfo.getCeilingY(), levelInfo.getGroundY(), this); // Sputa fuoco
                } else {
                    newEnemy = new ExplodingBall(spawnX, levelInfo.getCeilingY(), levelInfo.getGroundY() + 40, this); // Cade
                                                                                                                        // o
                                                                                                                        // rimbalza
                }
            }
            // FASE 2: Umani (Gripper, KnifeThrower, TomTom/TomTom)
            else {
                float humanOffset = this.levelInfo.getSpawnOffset(); // 160f
                boolean spawnOnRight = MathUtils.randomBoolean();
                spawnX = spawnOnRight ? camera.position.x + humanOffset : camera.position.x - humanOffset;

                float rand = MathUtils.random();
                if (rand>0.2f && rand < 0.6f) {
                    newEnemy = new Gripper(spawnX, levelInfo.getGroundY());
                } else if (rand < 0.2f) {
                    newEnemy = new KnifeThrower(spawnX, levelInfo.getGroundY());
                } else {
                    newEnemy = new TomTom(spawnX, levelInfo.getGroundY(), this); // Il nemico basso/nano
                }
            }

            newEnemy.facingRight = (newEnemy.position.x < player.position.x);
            enemies.add(newEnemy);
            spawnTimer = 0;
        }
    }

    @Override
    public void update(float dt) {

        super.update(dt);
        this.handleBoomerangs(dt);
       
    }

    private void goToNextFloor() {
        player.autoWalking = false;
        // Instead of jumping directly to Level3, show the intermezzo
        game.setScreen(new IntermissionLevel2Screen(this.game));
    }

    public void draw(float delta) {
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

        // In draw(float dt) del Level2Screen
        for (Boomerang b : boomerangs) {
            b.draw(batch);
        }

        // Disegna i frammenti del vaso (esplosione) solo in Level2
        for (int i = potProjectiles.size - 1; i >= 0; i--) {
            PotProjectile p = potProjectiles.get(i);
            if (!p.active) { potProjectiles.removeIndex(i); continue; }
            p.draw(batch);
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

    }

    @Override
    public void dispose() {
        //batch.dispose();
        background.dispose();
        shapeRenderer.dispose();
        this.whitePixel.dispose();
//        KnifeThrowerRes.dispose();
//        GripperRes.dispose();
//        PlayerRes.dispose();
    }

    @Override
    public void show() {
    	trapdoor.position.x = LevelConstants.SECOND_FLOOR_TRAPDOOR_X;
        player.position.x = LevelConstants.SECOND_FLOOR_LEFT_HOLE;
        player.facingRight = true;
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
        if (this.minions != null) this.minions.clear();
        if (this.boomerangs != null) this.boomerangs.clear();
    }
}
