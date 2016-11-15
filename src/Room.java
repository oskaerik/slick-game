import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.tiled.TiledMap;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The Room class is the current map the player is in
 * @author Oskar Eriksson and Gustave Rousselet
 * @version 0.1
 */
public class Room {
    private TiledMap map;
    private final int spawnX;
    private final int spawnY;
    private ArrayList<Rectangle> blocks;
    private ArrayList<Exit> exits;
    private ArrayList<Item> items;
    private ArrayList<Character> characters;
    private ArrayList<Projectile> projectiles;
    private String name;
    private Projectile fireball;

    private double xOffset;
    private double yOffset;

    /**
     * Constructor for the Room class
     * @param mapDirectory Name of the map .tmx-file in the map-folder
     * @param spawnX Player's spawn x-position
     * @param spawnY Player's spawn y-position
     */
    public Room(String mapDirectory, String mapName, int spawnX, int spawnY) throws SlickException {
        map = new TiledMap(mapDirectory);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        name = mapName;

        blocks = new ArrayList<>();
        exits = new ArrayList<>();
        items = new ArrayList<>();
        characters = new ArrayList<>();

        projectiles = new ArrayList<>();
        fireball = new Projectile(new Rectangle(0, 0, 44, 42), "fireball", "A fireball");
        projectiles.add(fireball);

        generateWorldObjects();
        xOffset = 0;
        yOffset = 0;
        }

    /**
     * Renders the map
     * @param worldX How much the world has moved in the x-direction
     * @param worldY How much the world has moved in the y-direction
     */
    public void render(double worldX, double worldY) {
        map.render((int)worldX-spawnX, (int)worldY-spawnY);
    }

    /**
     * @return Returns an ArrayList of blocks, objects that the player collides with
     */
    public ArrayList<Rectangle> getBlocks() { return blocks; }

    /**
     * @return Returns an ArrayList of exits, objects that the player collides with
     */
    public ArrayList<Exit> getExits() { return exits; }


    public ArrayList<Item> getItems() {return items; }

    public ArrayList<Character> getCharacters() {return characters; }

    public ArrayList<Projectile> getProjectiles() { return projectiles; }

    public String getName() { return name;}

    public void updateRectangles(double xMovement, double yMovement, ArrayList<Rectangle> newBlocks) {
        // Update blocks
        blocks = newBlocks;
        // Update exits
        for (Exit exit : exits) {
            exit.getRect().setX(exit.getRect().getX() + (float)xMovement);
            exit.getRect().setY(exit.getRect().getY() + (float)yMovement);
        }
        // Update items
        for (Item item : items) {
            item.getRect().setX(item.getRect().getX() + (float)xMovement);
            item.getRect().setY(item.getRect().getY() + (float)yMovement);
        }
        // Update characters
        for (Character character : characters) {
            character.getRect().setX(character.getRect().getX() + (float)xMovement);
            character.getRect().setY(character.getRect().getY() + (float)yMovement);
        }
        //Update projectiles
        for (Projectile projectile : projectiles) {
            projectile.getRect().setX(projectile.getRect().getX() + (float)xMovement);
            projectile.getRect().setY(projectile.getRect().getY() + (float)yMovement);
        }

        xOffset += (float)xMovement;
        yOffset += (float)yMovement;
    }

    public void renderEntities(Graphics graphics, Player player, GameState gameState) {
        for (Character character : characters) {
            character.renderCharacter(player, gameState, graphics);
        }
        for (Item item : items) {
            item.getAnimationArray().get(0).draw(item.getRect().getX(), item.getRect().getY());
        }
        for (Projectile projectile : projectiles) {
            projectile.render();
        }
    }

    public void updateEntities(int delta) {
        // Update character positions
        if (characters.size() > 0) {
            for (Character character : characters) {
                character.updateLocation(delta);
            }
        }
        // Update projectiles positions
        for (Projectile projectile : projectiles) {
            Character hitCharacter = projectile.moveProjectile(getBlocks(), getCharacters(), delta);
            if (hitCharacter != null) {
                hitCharacter.takeDamage(projectile.getDamage());
                checkIfAlive();
            }
        }
    }

    private void generateWorldObjects() throws SlickException {
        // Loop through all tiles in the map file
        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                int tileID = map.getTileId(i, j, 0);

                // Check for blocked tiles
                String value = map.getTileProperty(tileID, "Blocked", "false");
                if (value.equals("true")) {
                    blocks.add(new Rectangle((float)i * map.getTileWidth(),
                            (float)j * map.getTileHeight(),
                            map.getTileWidth(), map.getTileHeight()));
                }

                // Check for exit tiles
                String destination = map.getTileProperty(tileID, "Exit", "");
                if (!destination.equals("")) {
                    int spawnXPosition = Integer.parseInt(map.getTileProperty(tileID, "SpawnX", ""));
                    int spawnYPosition = Integer.parseInt(map.getTileProperty(tileID, "SpawnY", ""));
                    exits.add(new Exit(new Rectangle((float)i * map.getTileWidth(),
                            (float)j * map.getTileHeight(),
                            map.getTileWidth(), map.getTileHeight()), destination, spawnXPosition, spawnYPosition));
                }

                // Check for objects on object layer
                tileID = map.getTileId(i, j, 1);
                String itemName = map.getTileProperty(tileID, "ItemName", "");
                String itemDescription = map.getTileProperty(tileID, "ItemDescription", "");
                if (!itemName.equals("")) {
                    // Create the character rectangle (SET SIZE ACCORDING TO PROPERTIES)
                    Rectangle itemRectangle = new Rectangle(
                            (float)i * map.getTileWidth(), (float)j * map.getTileHeight(),
                            map.getTileWidth(), map.getTileHeight());
                    items.add(new Item(itemRectangle, itemName, itemDescription));
                }

                // Check for characters on characters layer
                tileID = map.getTileId(i, j, 2);
                String characterName = map.getTileProperty(tileID, "CharacterName", "");
                String characterDescription = map.getTileProperty(tileID, "CharacterDescription", "");
                if (!characterName.equals("")) {
                    // Create the character rectangle (SET SIZE ACCORDING TO PROPERTIES)
                    Rectangle characterRectangle = new Rectangle(
                            (float)i * map.getTileWidth(), (float)j * map.getTileHeight(),
                            map.getTileWidth(), map.getTileHeight());
                    // Add the character to the room's characters
                    characters.add(new Character(
                            characterRectangle, characterName, characterDescription));
                }
            }
        }
    }

    public void removeItem(Item item) {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item nextItem = it.next();
            if (item.equals(nextItem)) {
                it.remove();
                return;
            }
        }
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void checkIfAlive() {
        Iterator<Character> it = characters.iterator();
        while (it.hasNext()) {
            Character character = it.next();
            if (character.getHealth() <= 0) {
                it.remove();
            }
        }
    }

    public void highlightItems(Circle playerRange) {
        for (Item item : items) {
            item.hightlight(playerRange);
        }
    }

    public void freezeEntities(boolean frozen) {
        for (Character character : characters) {
            character.setFrozen(frozen);
        }
        for (Projectile projectile : projectiles) {
            projectile.setFrozen(frozen);
        }
    }

    public void updateSpawnOffset(float spawnX, float spawnY) {
        // Set block locations
        for (Rectangle block : blocks) {
            block.setX(block.getX() - (float)xOffset + spawnX);
            block.setY(block.getY() - (float)yOffset + spawnY);
        }
        // Set exit locations
        for (Exit exit : exits) {
            exit.getRect().setX(exit.getRect().getX() - (float)xOffset + spawnX);
            exit.getRect().setY(exit.getRect().getY() - (float)yOffset + spawnY);
        }
        // Set item locations
        for (Item item : items) {
            item.getRect().setX(item.getRect().getX() - (float)xOffset + spawnX);
            item.getRect().setY(item.getRect().getY() - (float)yOffset + spawnY);
        }
        // Set character locations
        for (Character character : characters) {
            character.getRect().setX(character.getRect().getX() - (float)xOffset + spawnX);
            character.getRect().setY(character.getRect().getY() - (float)yOffset + spawnY);
        }
        // Set projectile locations
        for (Projectile projectile : projectiles) {
            projectile.getRect().setX(projectile.getRect().getX() - (float)xOffset + spawnX);
            projectile.getRect().setY(projectile.getRect().getY() - (float)yOffset + spawnY);
        }
        xOffset = spawnX;
        yOffset = spawnY;
    }

    public double[] getOffset() {
        double[] toReturn = {xOffset, yOffset};
        return toReturn;
    }
}
