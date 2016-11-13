import org.newdawn.slick.Animation;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;

/**
 * The Projectile class
 * @author Oskar Eriksson and Gustave Rousselet
 * @version 0.1
 */
public class Projectile extends Entity {

    private Animation animation;
    private boolean shot;
    private String direction;
    private float speed;
    private int damage;

    /**
     * Constructor for the Projectile class
     * @param rectangle The rectangle of the projectile
     * @param name The name of the projectile
     * @param description The description of the projectile
     * @throws SlickException
     */
    public Projectile(Rectangle rectangle, String name, String description) throws SlickException {
        super(rectangle, name, description);
        animation = Tools.createAnimation("projectile", name).get(0);
        shot = false;
        direction = null;
        speed = 0.35f;
        damage = 10;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void shoot(float centerX, float centerY, String direction) {
        this.direction = direction;
        getRect().setCenterX(centerX);
        getRect().setCenterY(centerY);
        shot = true;
    }

    public boolean isShot() { return shot; }

    private Character checkIntersection(ArrayList<Rectangle> blocks, ArrayList<Character> characters) {
        for (Rectangle rectangle : blocks) {
            if (getRect().intersects(rectangle)) {
                shot = false;
                return null;
            }
        }
        for (Character character : characters) {
            if (getRect().intersects(character.getRect())) {
                shot = false;
                return character;
            }
        }
        return null;
    }

    public Character moveProjectile(
            ArrayList<Rectangle> blocks, ArrayList<Character> characters, int delta) {
        if (shot) {
            switch (direction) {
                case "up":
                    getRect().setY(getRect().getY() - speed*delta);
                    break;
                case "down":
                    getRect().setY(getRect().getY() + speed*delta);
                    break;
                case "left":
                    getRect().setX(getRect().getX() - speed*delta);
                    break;
                case "right":
                    getRect().setX(getRect().getX() + speed*delta);
                    break;
                default:
                    break;
            }
            return checkIntersection(blocks, characters);
        }
        return null;
    }

    public void render() {
        if (shot) {
            getAnimation().draw(getRect().getX(), getRect().getY());
        }
    }

    public int getDamage() { return damage; }
}
