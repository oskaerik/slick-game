import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;

/**
 * The Player class, the Player object is a child of the Character object. The player class holds
 * information about the objects the player rectangle and range intersect with.
 *
 * @author Oskar Eriksson and Gustave Rousselet
 * @version 0.1
 */
public class Player extends Character {

    // The range of the player, how far the player can reach when picking up items
    // and engaging in dialogue
    private Circle range;

    /**
     * Constructor of the Player class.
     *
     * @param rectangle The player's rectangle
     * @param speed     The player's speed, how fast the world objects move
     * @param radius    The radius of the range of the player
     * @throws SlickException Generic exception
     */
    public Player(Rectangle rectangle, float speed, float radius) throws SlickException {
        super(rectangle, "player", "player", speed);

        // Creates the range circle and places it in the middle of the screen
        range = new Circle(0, 0, radius);
        range.setCenterX(getRect().getCenterX());
        range.setCenterY(getRect().getCenterY());
    }

    /**
     * Method used to get the cirlce representing the players range.
     *
     * @return Circle representing the player's range.
     */
    public Circle getRange() {
        return range;
    }

    /**
     * Method used to try to add an item to the players inventory, returns true of possible, returns
     * false if not. If true, adds item to inventory. Player can hold max 10 items.
     *
     * @param item The item that should be added.
     * @return A boolean indicating whether it was possible to add the item to the player's inventory.
     */
    public boolean tryAddToInventory(Item item) {
        if (getInventory().getItems().size() < 10) {
            getInventory().addItem(item);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method used to remove an item from the player's inventory, based on the index, and returns it.
     *
     * @param itemIndex The index of the item to be removed from the inventory.
     * @return The item that was removed from the inventory.
     */
    public Item removeFromInventory(int itemIndex) {
        if (getInventory().getItems().size() > 0 && getInventory().getItems().size() > itemIndex) {
            return getInventory().getItems().remove(itemIndex);
        } else {
            return null;
        }
    }

    /**
     * Method used to return an ArrayList of the items that the player's range intersects.
     *
     * @param items An ArrayList containing the items to be checked.
     * @return An ArrayList with the intersected items.
     */
    public ArrayList<Item> getItemsInRange(ArrayList<Item> items) {
        ArrayList<Item> intersectedItems = new ArrayList<>();
        for (Item item : items) {
            if (range.intersects(item.getRect())) {
                intersectedItems.add(item);
            }
        }
        return intersectedItems;
    }

    /**
     * Returns the first character in an ArrayList that the player range intersects.
     *
     * @param characters An ArrayList containing the characters to be checked.
     * @return The first character found.
     */
    public Character getCharacterInRange(ArrayList<Character> characters) {
        for (Character character : characters) {
            if (range.intersects(character.getRect())) {
                return character;
            }
        }
        return null;
    }

    /**
     * Returns the first intersected exit in an ArrayList of exits that the player intersects.
     *
     * @param exits An ArrayList containing the exits to be checked.
     * @return The first exit found.
     */
    public Exit getIntersectedExit(ArrayList<Exit> exits) {
        for (Exit exit : exits) {
            if (getRect().intersects(exit.getRect())) {
                return exit;
            }
        }
        return null;
    }
}
