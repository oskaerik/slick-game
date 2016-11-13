import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The camera class handles the moving of objects
 * @author Oskar Eriksson and Gustave Rousselet
 * @version 0.1
 */
public class World {
    private Player player;
    private Animation animation;
    private String lastDirection;
    private Rectangle inventoryOutline;
    private Rectangle inventoryItemOutline;
    private int inventorySelectedItemNumber;

    private Room currentRoom;
    private HashMap<String, Room> rooms;

    private GameState gameState;

    /**
     * Constructor for the World class
     * @throws SlickException Generic exception
     */
    public World() throws SlickException {
        // The player object, takes parameters: width, height, speed, radius of range
        player = new Player(16, 16, 0.2, 32);
        animation = player.getStandingPlayer("down");
        lastDirection = "down";
        inventoryOutline = new Rectangle(Core.WIDTH/2 + 100, Core.HEIGHT/2 - 200, 100, 160);
        inventoryItemOutline = new Rectangle(Core.WIDTH/2 + 100, Core.HEIGHT/2 - 200, 16, 16);


        // Create GameState
        gameState = new GameState();

        // Add rooms to HashMap rooms
        rooms = new HashMap<>();
        rooms.put("center", new Room("res/maps/center.tmx", "center", 0, 0));
        rooms.put("north", new Room("res/maps/north.tmx", "north", 0, 0));
        currentRoom = rooms.get("center");

    }

    /**
     * Checks for key presses and executes commands accordingly
     * @param gameContainer GameContainer object handling game loop etc
     * @param delta Amount of time that has passed since last updateGraphics (ms)
     */
    public void checkKeyPresses(GameContainer gameContainer, int delta) throws SlickException {
        animation = player.getStandingPlayer(lastDirection);

        // Depending on whether the inventory is open or not, move player or move the inventory marker
        if (!gameState.isInventoryOpen()) {
            keyMovement(gameContainer, delta);
        } else {
            keyInventory(gameContainer, delta);
        }

        keyActions(gameContainer, delta);
    }


    public void keyMovement(GameContainer gameContainer, int delta) throws SlickException {
        if (gameContainer.getInput().isKeyDown(Input.KEY_UP)) {
            movement("up", delta);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_DOWN)) {
            movement("down", delta);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_LEFT)) {
            movement("left", delta);
        }
        if (gameContainer.getInput().isKeyDown(Input.KEY_RIGHT)) {
            movement("right", delta);
        }
    }

    private void keyInventory(GameContainer gameContainer, int delta) {
        if (gameContainer.getInput().isKeyPressed(Input.KEY_UP) && inventorySelectedItemNumber > 0) {
            inventorySelectedItemNumber--;
        }
        if (gameContainer.getInput().isKeyPressed(Input.KEY_DOWN)) {
            if (inventorySelectedItemNumber < player.getInventory().getNumberOfItems()-1) {
                inventorySelectedItemNumber++;
            }
        }
        // Drops items from inventory
        if (gameContainer.getInput().isKeyPressed(Input.KEY_A)) {
            Item drop = player.removeFromInventory(inventorySelectedItemNumber);
            if (drop != null) {
                drop.getRect().setX(player.getRect().getX());
                drop.getRect().setY(player.getRect().getY());
                currentRoom.addItem(drop);
            }
            // If the inventory contains no items, the marker should stay on the starting position
            if (inventorySelectedItemNumber > 0) {
                inventorySelectedItemNumber--;
            }
        }
    }

    private void keyActions(GameContainer gameContainer, int delta) throws SlickException {
        // Adds items in range to inventory if player is carrying less than allowed amount of items
        if (gameContainer.getInput().isKeyPressed(Input.KEY_SPACE)) {
            for (Item item : player.getIntersectedItems(currentRoom.getItems())) {
                if (player.addToInventory(item)) {
                    currentRoom.removeItem(item);
                }
            }
        }

        // Opens/closes inventory
        if (gameContainer.getInput().isKeyPressed(Input.KEY_I)) {
            // Resets the inventory marker
            inventoryItemOutline.setY(Core.HEIGHT/2 - 200);
            inventoryItemOutline.setX(Core.WIDTH/2 + 100);
            inventorySelectedItemNumber = 0;

            gameState.toggleInventory();
        }

        // Shoots fireball
        if (gameContainer.getInput().isKeyPressed(Input.KEY_M)) {
            Rectangle fireballRectangle = new Rectangle(0, 0, 44, 42);
            fireballRectangle.setCenterX(player.getRect().getCenterX());
            fireballRectangle.setCenterY(player.getRect().getCenterY());
            Fireball fireball = new Fireball(fireballRectangle, "Fireball", "A fireball");
            fireball.setDirection(lastDirection);
            currentRoom.getFireballs().add(fireball);
        }
    }


    /**
     * Handles the player movement, or rather the movement of the world itself
     * @param direction The direction the player should be moving
     */
    private void movement(String direction, int delta) throws SlickException {
        double xMovement = 0;
        double yMovement = 0;
        switch (direction) {
            case "up":
                yMovement = player.getSpeed() * delta;
                lastDirection = "up";
                break;
            case "down":
                yMovement = -player.getSpeed() * delta;
                lastDirection = "down";
                break;
            case "left":
                xMovement = player.getSpeed() * delta;
                lastDirection = "left";
                break;
            case "right":
                xMovement = -player.getSpeed() * delta;
                lastDirection = "right";
                break;
            default:
                break;
        }
        // Check if the movement will create any intersections with blocks
        ArrayList<Rectangle> newBlocks = isBlocked(xMovement, yMovement);
        if (newBlocks != null) {
            animation = player.getAnimation(lastDirection);
            currentRoom.updateRectangles(xMovement, yMovement, newBlocks);
        }
    }

    private ArrayList<Rectangle> isBlocked(double xMovement, double yMovement) {
        // Create a copy of the Blocks ArrayList
        ArrayList<Rectangle> newBlocks = new ArrayList<>();
        for (Rectangle block : currentRoom.getBlocks()) {
            newBlocks.add(new Rectangle(block.getX(), block.getY(), block.getWidth(), block.getHeight()));
        }

        // Updates blocks in newBlocks and checks if it intersects with player
        for (Rectangle block : newBlocks) {
            block.setX(block.getX() + (float)xMovement);
            block.setY(block.getY() + (float)yMovement);
            if (player.getRect().intersects(block)) {
                return null;
            }
        }
        return newBlocks;
    }

    /**
     * Checks if the player has intersected any exits and changes the room if that is the case
     */
    public void checkIntersectedExit() {
        Exit exit = player.getIntersectedExit(currentRoom.getExits());
        if (exit != null) {
            currentRoom = rooms.get(exit.getDestination());
        }
    }

    public void checkIntersectedFireballs() {
        Iterator<Fireball> iterator = currentRoom.getFireballs().listIterator();
        while (iterator.hasNext()) {
            Fireball fireball = iterator.next();
            if (fireball.checkIfintersects(currentRoom.getBlocks())) {
                iterator.remove();
            }
        }
    }

    /**
     * Draws the inventory outline and the objects in the inventory on the screen
     * @param graphics Graphics component used to draw
     */
    public void drawInventory(Graphics graphics) {
        graphics.draw(inventoryOutline);
        graphics.draw(inventoryItemOutline);
        inventoryItemOutline.setY(Core.HEIGHT/2-200 + inventorySelectedItemNumber*16);
        for (int i = 0; i < player.getInventory().getItems().size(); i ++) {
            Item itemDisplayed = player.getInventory().getItems().get(i);
            graphics.drawImage(itemDisplayed.getItemImage(), Core
                    .WIDTH/2 + 100, Core.HEIGHT/2 - 200 + 16*i);
            itemDisplayed.getItemFont().drawString(Core.WIDTH/2 + 100 + 16, Core
                    .HEIGHT/2 - 200 + 16*i, itemDisplayed.getName(), Color
                    .blue);
        }
    }

    /**
     * Draws the items on screen
     * @param graphics Graphics component used to draw
     */
    public void drawItems(Graphics graphics) {
        for (Item item : currentRoom.getItems()) {
            graphics.drawImage(item.getItemImage(), item.getRect().getX(), item.getRect().getY());
        }
    }

    public void drawFireBalls(Graphics graphics) {
        for (Fireball fireball : currentRoom.getFireballs()) {
            if (fireball.getDirection().equals("up")) {
                fireball.getRect().setY(fireball.getRect().getY()-2);
                fireball.getAnimation().draw(fireball.getRect().getX(), fireball.getRect().getY());
            }
            if (fireball.getDirection().equals("down")) {
                fireball.getRect().setY(fireball.getRect().getY()+2);
                fireball.getAnimation().draw(fireball.getRect().getX(), fireball.getRect().getY());
            }
            if (fireball.getDirection().equals("right")) {
                fireball.getRect().setX(fireball.getRect().getX()+2);
                fireball.getAnimation().draw(fireball.getRect().getX(), fireball.getRect().getY());
            }
            if (fireball.getDirection().equals("left")) {
                fireball.getRect().setX(fireball.getRect().getX()-2);
                fireball.getAnimation().draw(fireball.getRect().getX(), fireball.getRect().getY());
            }
        }
    }

    /**
     * Draws highlighting on items that intersects with player's range
     */
    public void drawItemHighlighting() {
        for (Item item : player.getIntersectedItems(currentRoom.getItems())) {
            item.getItemFont().drawString(
                    item.getRect().getX(), item.getRect().getY(), item.getName(), Color.blue
            );
        }
    }

    /**
     * Checks for events, such as key presses and intersected exits
     * @param gameContainer
     * @param delta
     * @throws SlickException
     */
    public void checkEvents(GameContainer gameContainer, int delta) throws SlickException {
        checkKeyPresses(gameContainer, delta);
        checkIntersectedExit();
        checkIntersectedFireballs();
    }

    /**
     * Updates the graphics of the game world
     * @param graphics Graphics component used to draw
     */
    public void updateGraphics(Graphics graphics) {
        // Draw the world
        currentRoom.render(currentRoom.getBlocks().get(0).getX(), currentRoom.getBlocks().get(0).getY());

        // Draw the player animation (NOT CORRECT WITH 16)
        animation.draw(player.getRect().getX()-16, player.getRect().getY()-16);

        // Render the enemy
        if (currentRoom.getCharacters().size() > 0) {
            Character enemy = currentRoom.getCharacters().get(0);
            enemy.getAnimation().draw(enemy.getRect().getX() - 16, enemy.getRect().getY() - 16);
        }

        //Render fireballs, items, and such
        drawFireBalls(graphics);
        drawItems(graphics);
        drawItemHighlighting();
        if (gameState.isInventoryOpen()) {
            drawInventory(graphics);
        }
    }

    public Room getCurrentRoom() { return currentRoom; }
    public Player getPlayer() { return player; }

    public void updateCharacters(GameContainer gameContainer, int delta) {
        ArrayList<Character> characters = currentRoom.getCharacters();
        if (characters.size() > 0) {
            for (Character character : characters) {
                character.updateLocation();
            }
        }
    }
}
