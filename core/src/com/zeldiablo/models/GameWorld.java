package com.zeldiablo.models;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.zeldiablo.models.enums.State;
import com.zeldiablo.models.monsters.Monster;
import com.zeldiablo.models.monsters.Skeleton;
import com.zeldiablo.models.portals.Portal;
import com.zeldiablo.models.traps.Projectile;
import com.zeldiablo.models.traps.Trap;
import com.zeldiablo.models.traps.TrapDamage;
import com.zeldiablo.views.GameScreen;

import java.util.ArrayList;

public class GameWorld {

    // --- Variables static qui définissent la taille du monde virtuel
    public static final int WIDTH = 80;
    public static final int HEIGHT = 60;

    // --- Eléments du jeu
    private GameScreen screen;
    private GameState gameState;
    private ArrayList<Body> bodiesToDelet;
    private ArrayList<Body> bodies;
    private World world;
    private Player player;
    private Maze maze;

    // --- Données Téleportation
    public boolean isTp;
    public Portal portal;

    public GameWorld(GameScreen s, GameState gameState) {
        this.screen = s;
        this.gameState = gameState;
        this.bodiesToDelet = new ArrayList<>();
        this.bodies = new ArrayList<>();
        this.world = new World(new Vector2(0, 0), true);
        this.player = new Player(this, "Tester");
        this.maze = new Maze(this);
        this.maze.initMonster();
        this.isTp = false;
    }

    /**
     * Demande au modèle Player de s'afficher sur le jeu
     * @param batch ensemble de sprite
     */
    public void draw(SpriteBatch batch) {
        this.maze.draw(batch);
        this.player.draw(batch);
    }

    public World getWorld() {
        return this.world;
    }

    public Player getPlayer() {
        return this.player;
    }

    /***
     * Cette Fonction teleporte le joueur au portail de sortie par rapport au portail selectionner
     * @param p Player
     * @param por Portal
     */
    public void teleport(Player p, Portal por){
        //Si le portail est actif je peux teleporter
        if(por.isActif()) {
            // Je rend le portail de sortie inactif pour eviter de teleporter en boucle
            por.exitPortalDelai();
            por.delai();
            // Je teleporte le joueur a la position du portail de sortie.
            p.getBody().setTransform(por.getPosPortalExit().x ,por.getPosPortalExit().y ,0f);
            // Si le portail de sortie n'est pas dans le meme labyrinthe on teleporte le joueur dans l'autre
            if (!por.exitSameMaze()) {
                loadMaze(por.getExitPortalNumMaze(),por.getPosPortalExit().x + ((p.getBody().getAngle()) * p.getRadius()*2),por.getPosPortalExit().y);
            }
        }
    }

    public void loadMaze(int num, float playerX, float playerY) {
        this.gameState.setState(State.LOADING);
        this.maze.resetMaze();
        this.world.destroyBody(this.player.getBody());
        this.player = new Player(this, "TESTER");
        this.player.setPosition(playerX, playerY);
        this.maze.loadMaze(num);
        this.gameState.setState(State.IN_PROGRESS);
    }

    public void atk(){
        this.player.attack(screen.getAngle());
    }

    public void deleteBodies() {
        if(bodiesToDelet.size() > 0){
            world.destroyBody(bodiesToDelet.get(0));
            bodiesToDelet.clear();
        }
    }

    public void addBodyToDelete(Body body) {
        this.bodiesToDelet.add(body);
    }

    public void addBody(Body body) {
        this.bodies.add(body);
    }

    public ArrayList<Body> getBodies(){
        return bodies;
    }

    /**
     * Retourne une grille de booléen indiquant, pour chaque case, si elle est libre ou non.
     * @return grille de booléen
     */
    public boolean[][] generateGrid() {
        boolean[][] grid = new boolean[GameWorld.HEIGHT][GameWorld.WIDTH];
        for (int j = 0; j < GameWorld.HEIGHT; j++)
            for (int i = 0; i < GameWorld.WIDTH; i++)
                grid[j][i] = true;

        int x, y;
        ArrayList<Vector2> tmp = this.maze.getWallsCoord();
        for (Vector2 vec : tmp) {
            x = (int) vec.x;
            y = (int) vec.y;
            grid[y][x] = false;
        }
        return grid;
    }
}
