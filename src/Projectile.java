import org.newdawn.slick.Animation;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;
import java.util.Date;

/**
 * The Projectile class
 * @author Oskar Eriksson and Gustave Rousselet
 * @version 0.1
 */
public class Projectile extends Entity {
    private ArrayList<Animation> animationArray;
    private boolean shot;
    private String direction;
    private float speed;
    private int damage;
    private String belongsTo;
    private long lastShot;
    private Date date;
    private int frameCounter;

    /**
     * Constructor for the Projectile class
     * @param rectangle The rectangle of the projectile
     * @param name The name of the projectile
     * @param description The description of the projectile
     * @throws SlickException
     */
    public Projectile(Rectangle rectangle, String name, String characterName,
                      int damage, float speed) throws SlickException {
        super(rectangle, name, "projectile");
        animationArray = Tools.createAnimation("projectile", name);
        shot = false;
        direction = null;
        this.speed = speed;
        this.damage = damage;
        belongsTo = characterName;
        date = new Date();
        lastShot = date.getTime();
    }

    public Animation getAnimation() {
        if (!isFrozen()) {
            return animationArray.get(0);
        } else {
            return Tools.getFreezeAnimation(animationArray, "");
        }
    }

    public void shoot(float centerX, float centerY, String direction) {
        this.direction = direction;
        getRect().setCenterX(centerX);
        getRect().setCenterY(centerY);
        shot = true;
    }

    public boolean isShot() { return shot; }

    public void setShot(boolean shot) {
        this.shot = shot;
    }

    private Character checkIntersection(ArrayList<Rectangle> blocks, ArrayList<Character> characters) {
        for (Rectangle rectangle : blocks) {
            if (getRect().intersects(rectangle)) {
                hit();
                return null;
            }
        }
        for (Character character : characters) {
            if (getRect().intersects(character.getRect()) && !character.getName().equals(belongsTo)) {
                hit();
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
        } else {
            hit();
        }
    }

    public int getDamage() { return damage; }

    public String getBelongsTo() {
        return belongsTo;
    }

    public void toggleShotLastTime(long time) {
        lastShot = time;
    }

    public long getLastShot() {
        return lastShot;
    }

    public void hit() {
        // If it's a hit, set shot to false and start hit animation
        if (shot) {
            shot = false;
            frameCounter = getAnimationArray().get(1).getFrameCount();
        }

        // Run hit animation for every frame of the animation
        if (frameCounter >= 0) {
            getAnimationArray().get(1).draw(getRect().getX(), getRect().getY());
            frameCounter--;
        }
    }
}
