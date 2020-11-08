package com.zeldiablo.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.Timer;
import com.zeldiablo.controllers.ai.Node;
import com.zeldiablo.controllers.ai.PathFinding;

import java.awt.*;
import java.util.List;
import java.util.Stack;

public abstract class Monster implements Entity {

    private Entity target;
    private Body body;
    private float speed;

    private Timer.Task step;
    private Timer.Task recalculate;

    private PathFinding finding;

    public Monster(World world, float x, float y, Entity tar) {
        this.target = tar;
        this.speed = 0.1f;

        // --- Création du body --- //
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(x, y);
        body = world.createBody(bd);

        FixtureDef fixture = new FixtureDef();
        Shape shape = new CircleShape();
        shape.setRadius(SIZE);
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.restitution = 0.25f;
        fixture.friction = 0f;
        fixture.isSensor = true;

        body.setUserData(this);
        body.createFixture(fixture);
        shape.dispose();

        // Tout ce joue la dessus. Il faut faire une vérification afin de mettre false si la case est occupé par un obstacle.
        // Je pense que la partie la sera faite dans GameWorld et sera donné au monstre dans le constructeur
        final boolean[][] grid = new boolean[GameWorld.HEIGHT][GameWorld.WIDTH];
        for (int j = 0; j < GameWorld.HEIGHT; j++)
            for (int i = 0; i < GameWorld.WIDTH; i++)
                //Todo: Vérification à faire ici
                grid[j][i] = true;

        this.finding = new PathFinding(grid, this.getX(), this.getY(), this.target.getX(), this.target.getY());

        // Création de la tâche qui s'occupe de déplacer le monstre
        this.step = new Timer.Task() {
            @Override
            public void run() {
                move();
            }
        };

        // Création de la tâche qui recalcule le chemin
        this.recalculate = new Timer.Task() {
            @Override
            public void run() {
                finding = new PathFinding(grid, getX(), getY(), target.getX(), target.getY());
            }
        };

        Timer.schedule(this.step, 0, this.speed);
        Timer.schedule(this.recalculate, 3, 3);
    }

    /**
     * Effectue un déplacement du monstre en suivant le chemin calculé
     */
    public void move() {
        List<Node> path = this.finding.getPath();
        Node node;
        if (!path.isEmpty())
            node = path.remove(path.size()-1);
        else
            node = new Node(getX(), getY());

        // Calcule de la distance entre les deux
        float x = this.target.getX() - this.getX();
        float y = this.target.getY() - this.getY();

        // Un peu de magie (et de la trigo) et on obtient l'angle
        float angle = (float) Math.atan(y/x);
        if (x < 0)
            angle += Math.PI;

        this.body.setTransform(node.x, node.y, angle);
    }

    /**
     * Methode seravnt a récupérer la position de l'entité
     * @return Vector2 de la position
     */
    public Vector2 getPosition() {
        return this.body.getPosition();
    }

    /**
     * Méthode servant a récupérer la position en X de l'entité
     *
     * @return int coordonée X
     */
    @Override
    public int getX() {
        return (int) this.getPosition().x;
    }

    /**
     * Méthode servant a récupérer la position en Y de l'entité
     *
     * @return int coordonée Y
     */
    @Override
    public int getY() {
        return (int) this.getPosition().y;
    }
}
