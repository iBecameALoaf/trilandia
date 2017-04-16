package com.loafy.game.entity;

import com.loafy.game.Main;
import com.loafy.game.entity.player.EntityPlayer;
import com.loafy.game.gfx.Animation;
import com.loafy.game.world.World;
import com.loafy.game.world.block.Block;
import com.loafy.game.world.block.Material;
import org.newdawn.slick.geom.Rectangle;

public class Entity {

    public World world;
    public Animation animation;

    // CONSTANTS

    public final float GRAVITY = 0.6F;
    public final float VELOCITY_DECREASE = 0.35F;
    public float MAX_FALLING_SPEED = 9F;
    public float JUMP_START;

    public int PADDING_LEFT;
    public int PADDING_RIGHT;

    // LOCATION

    public float x, y;
    public float dx, dy;

    // ATTRIBUTES

    public float width;
    public float height;
    public float speed;

    // MOVEMENT

    public boolean left;
    public boolean right;
    public boolean falling;

    // COLLISIONS

    public Rectangle box;
    public boolean topLeft;
    public boolean topRight;
    public boolean midLeft;
    public boolean midRight;
    public boolean bottomLeft;
    public boolean bottomRight;

    public float blockFriction;
    public float airFriction = 1.0f;

    public float startY, endY;

    public int immunity = 240 * (Main.FPS / Main.UPS);
    public int time;

    public Entity(World world, float x, float y) {
        this.world = world;
        this.x = x;
        this.y = y;

        this.box = new Rectangle(x + 4, y + 4, width - 8, height - 8);
    }

    public void render(float xOffset, float yOffset) {
        animation.render(x - xOffset, y - yOffset);
    }

    public void update(int delta) {
        calculateMovement(delta);
        calculateCollisions(delta);
        move(delta);
        this.box = new Rectangle(x + 4, y + 4, width - 8, height - 8);

        time++;
    }

    public void calculateMovement(int delta) {
        if (falling) {
            dy += GRAVITY * ((float)Main.UPS / (float)Main.FPS);
            if (dy > MAX_FALLING_SPEED) dy = MAX_FALLING_SPEED;
        }
    }

    public void calculateCollisions(int delta) {
        float tox = x + (dx * delta) / 1000 * Main.UPS;
        float toy = y + (dy * delta) / 1000 * Main.UPS;

        calculateCorners(tox, y - 1);
        if (dx < 0) {
            if (topLeft || midLeft || bottomLeft) {
                dx = 0;
            }
        }

        if (dx > 0) {
            if (topRight || midRight || bottomRight) {
                dx = 0;
            }
        }

        calculateCorners(x, toy);

        if (topLeft || topRight) {
            dy = 0;
            falling = true;
            int pr = world.getBlockY((int) toy);
            float ya = (pr + 1) * Material.SIZE;

            if(this instanceof EntityPlayer)
            world.yOffset -= y - ya;

            y = ya;

        }

        if (bottomLeft || bottomRight && falling) {
            falling = false;
            dy = 0;

            int add = 0;
            if(this instanceof EntityItem)
                add = 16;

            int pr = world.getBlockY((int) toy);
            float ya = (pr * Material.SIZE) + add;

            if(this instanceof EntityPlayer)
            world.yOffset -= y - ya;

            y = ya;

            land();
        }

        if(!falling) {
            startY = y;
        }

        if (!bottomLeft && !bottomRight) {
            falling = true;
        }
    }

    public void calculateCorners(float x, float y) {
        int leftTile = (int) x + PADDING_LEFT - 4;
        int rightTile = (int) x + (int) width - PADDING_RIGHT;
        int topTile = (int) y + 2;
        int midTile = (int) (y + height / 2);
        int bottomTile = (int) y + (int) width;

        try {
            topLeft = world.getBlock(leftTile, topTile).getMaterial().isSolid();
            topRight = world.getBlock(rightTile, topTile).getMaterial().isSolid();
            midLeft = world.getBlock(leftTile, midTile).getMaterial().isSolid();
            midRight = world.getBlock(rightTile, midTile).getMaterial().isSolid();
            bottomLeft = world.getBlock(leftTile, bottomTile).getMaterial().isSolid();
            bottomRight = world.getBlock(rightTile, bottomTile).getMaterial().isSolid();

            Block bottomLeftBlock = world.getBlock(leftTile, bottomTile);
            Block bottomRightBlock = world.getBlock(rightTile, bottomTile);

            if (bottomLeftBlock.getMaterial().getID() == Material.AIR.getID() || bottomRightBlock.getMaterial().getID() == Material.AIR.getID()) {
                blockFriction = airFriction;
            } else {
                blockFriction = (bottomLeftBlock.getFriction() + bottomRightBlock.getFriction()) / 2;
            }

        } catch (Exception e) {

        }
    }

    public void move(int delta) {
        x += (dx * delta) / 1000 * Main.UPS;
        y += (dy * delta) / 1000 * Main.UPS;

        if(this instanceof EntityPlayer) {
            world.xOffset += (dx * delta) / 1000 * Main.UPS;
            world.yOffset += (dy * delta) / 1000 * Main.UPS;
        }


        if(dx > 0) {
            dx -= VELOCITY_DECREASE * blockFriction;
            if(dx < 0) dx = 0;
        }

        if(dx < 0) {
            dx += VELOCITY_DECREASE * blockFriction;
            if(dx > 0) dx = 0;
        }
    }

    public void land() {

    }

    public void setVelocity(float dx, float dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public World getWorld() {
        return world;
    }

}