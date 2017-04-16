package com.loafy.game.world;

import com.loafy.game.Main;
import com.loafy.game.state.IngameState;
import com.loafy.game.state.MenuState;
import com.loafy.game.state.gui.GuiLoadingBar;
import com.loafy.game.world.block.Block;
import com.loafy.game.world.block.Material;
import org.newdawn.slick.geom.Line;
import util.LineSmoother;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class WorldGenerator {

    private Block[][] blocks;
    private Block[][] walls;

    private int[] topBlocks;
    private boolean[][] lineBlocks;

    public BufferedImage image;
    public int width, height;

    private int spawnX;
    private int spawnY;

    private final float cs = 0.775f;
    private final float ce = 0.505f;
    private final int progressionBlocks = 28;

    private final int dirtAmount = 12;
    private final int stoneAmount = 16;

    private final float iterations = 7;

    private MenuState menuState = Main.menuState;
    private GuiLoadingBar loadingBar;

    public WorldGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        this.blocks = new Block[width][height];
        this.walls = new Block[width][height];
        lineBlocks = new boolean[width][height];
        topBlocks = new int[width];

        loadingBar = menuState.guiGeneratingWorld.getLoadingBar();

        for (int x = 0; x < walls.length; x++) {
            for (int y = 0; y < walls[0].length; y++) {
                this.walls[x][y] = new Block(Material.AIR, x * Material.SIZE, y * Material.SIZE);
            }
        }

        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[0].length; y++) {
                this.blocks[x][y] = new Block(Material.AIR, x * Material.SIZE, y * Material.SIZE);
            }
        }

        generateLines();
        generateCaves();
        generateTerrain();
    }

    public void cleanUp() {
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[0].length; y++) {
                blocks[x][y] = null;
                walls[x][y] = null;
            }
        }
    }

    public void generateLines() {
        loadingBar.setStatus("Generating terrain structure.");

        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Random random = new Random();

            int iterations = 8;
            float threshold = 5F;
            float decay = 0.7F;
            int mh = 100;
            int sh = mh + random.nextInt(50);
            List<javafx.scene.shape.Line> lines = new ArrayList<>();
            lines.add(new javafx.scene.shape.Line(0.0F, sh, width, sh));
            for (int i = 0; i < iterations; i++) {
                ArrayList<javafx.scene.shape.Line> add = new ArrayList<>();
                Iterator<javafx.scene.shape.Line> it = lines.iterator();
                while (it.hasNext()) {
                    javafx.scene.shape.Line line = it.next();
                    it.remove();
                    float sX = (float) (int) line.getStartX();
                    float sY = (float) (int) line.getStartY();
                    float eX = (float) (int) line.getEndX();
                    float eY = (float) (int) line.getEndY();
                    float mX = (sX + eX) / 2.0F;
                    float rY = 29.0F * threshold * (random.nextFloat() - 0.5F);

                    add.add(new javafx.scene.shape.Line(sX, sY, mX, sY + rY));
                    add.add(new javafx.scene.shape.Line(mX, sY + rY, eX, eY));

                    lines = add;
                }
                threshold *= decay;
            }

            lines = LineSmoother.smoothLine(lines);


            Graphics g = image.getGraphics();
            g.setColor(Color.WHITE);

            for (javafx.scene.shape.Line line : lines) {
                g.drawLine((int) line.getStartX(), (int) line.getStartY(), (int) line.getEndX(), (int) line.getEndY());
            }

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    if ((((image.getRGB(x, y) >> 24) & 0xff) > 0)) {
                        lineBlocks[x][y] = true;
                        topBlocks[x] = y;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateTerrain() {
        try {
            this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Random random = new Random();

        loadingBar.setStatus("Generating terrain.");

        int lastTree = 0;
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[0].length; y++) {
                Block block = blocks[x][y];
                if (y == topBlocks[x]) {

                    for (int i = 0; i < y; i++) {
                        blocks[x][i].setMaterial(Material.AIR);
                    }

                    block.setMaterial(Material.GRASS);
                    lastTree = createTree(lastTree, x, y);
                    break;
                }
            }

            //maybe make a method that takes x, y to clean this up more ....

        }
/*
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                if (blocks[x][y].getMaterial() == Material.GRASS) {

                    // TREES

                    int treeHeight = 6 + random.nextInt(16 - 6);
                    if (x - 4 >= lastTree && random.nextInt(5) == 3) {
                        lastTree = x;
                        for (int i = 0; i < treeHeight; i++) {
                            int yy = y - i - 1;
                            this.walls[x][yy] = new Block(Material.WOOD, x * Material.SIZE, yy * Material.SIZE);
                        }

                        int startY = y - treeHeight;

                        for (int xx = -1; xx < 2; xx++) {
                            for (int yy = -1; yy < 2; yy++) {

                                if (!(x + xx < 3 || x + xx > walls.length - 3 || startY + yy < 3 || startY + yy > walls[0].length - 3)) {
                                    this.walls[x + xx][startY + yy] = new Block(Material.LEAF, (x + xx) * Material.SIZE, (startY + yy) * Material.SIZE);
                                }
                            }
                        }
                    }
                }
            }
        }



        state.status = "Replacing above ground caves";
        state.step++;
        // TOP GRASS BLOCKS AND REPLACING ABOVE GROUND CAVES

        state.status = "Placing plant life";
        state.step++;
        int dirtY = 20;
        int lastTree = 0;
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                if (blocks[x][y].getMaterial() == Material.GRASS) {

                    // TREES

                    int treeHeight = 6 + random.nextInt(16 - 6);
                    if (x - 4 >= lastTree && random.nextInt(5) == 3) {
                        lastTree = x;
                        for (int i = 0; i < treeHeight; i++) {
                            int yy = y - i - 1;
                            this.walls[x][yy] = new Block(Material.WOOD, x * Material.SIZE, yy * Material.SIZE);
                        }

                        int startY = y - treeHeight;

                        for (int xx = -1; xx < 2; xx++) {
                            for (int yy = -1; yy < 2; yy++) {

                                if (!(x + xx < 3 || x + xx > walls.length - 3 || startY + yy < 3 || startY + yy > walls[0].length - 3)) {
                                    this.walls[x + xx][startY + yy] = new Block(Material.LEAF, (x + xx) * Material.SIZE, (startY + yy) * Material.SIZE);
                                }
                            }
                        }
                    }

                    // STONE WALLS

                    int caveY = 15;
                    for (int i = dirtY + 1; i < dirtY + caveY; i++) {
                        int yy = y + i;
                        this.blocks[x][yy] = new Block(Material.STONE, x * Material.SIZE, (yy) * Material.SIZE);
                    }

                    for (int i = dirtY; i < blocks[0].length - dirtY - y; i++) {
                        int yy = y + i;
                        this.walls[x][yy] = new Block(Material.STONE_WALL, x * Material.SIZE, (yy) * Material.SIZE);
                    }


                    state.status = "Placing dirt";
                    state.step = 13;
                    // DIRT WALLS + DIRT

                    for (int i = 0; i < dirtY; i++) {
                        int yy = y + i + 1;
                        if (x <= blocks.length && yy < blocks[x].length) {
                            this.blocks[x][yy] = new Block(Material.DIRT, x * Material.SIZE, (yy) * Material.SIZE);
                            this.walls[x][yy] = new Block(Material.DIRT_WALL, x * Material.SIZE, (yy) * Material.SIZE);

                            if (i < 4)
                                this.walls[x][y + i] = new Block(Material.AIR, x * Material.SIZE, (y + i) * Material.SIZE);
                        }
                    }

                }
            }
        }
        }*/
    }

    public int createTree(int lastTree, int x, int y) {
        Random random = new Random();
        int treeHeight = 6 + random.nextInt(16 - 6);
        if (x - 4 >= lastTree && random.nextInt(5) == 3) {
            lastTree = x;
            for (int i = 0; i < treeHeight; i++) {
                int yy = y - i - 1;
                this.walls[x][yy] = new Block(Material.WOOD, x * Material.SIZE, yy * Material.SIZE);  //TODO Check is out bounds
            }

            int startY = y - treeHeight;

            for (int xx = -1; xx < 2; xx++) {
                for (int yy = -1; yy < 2; yy++) {

                    if (!(x + xx < 3 || x + xx > walls.length - 3 || startY + yy < 3 || startY + yy > walls[0].length - 3)) {
                        this.walls[x + xx][startY + yy] = new Block(Material.LEAF, (x + xx) * Material.SIZE, (startY + yy) * Material.SIZE);
                    }
                }
            }
        }

        return lastTree;
    }

    public void generateCaves() {
        loadingBar.setStatus("Generating caves.");

        Random random = new Random();
        float dif = (ce - cs) / progressionBlocks; //increment from each block

        for (int x = 0; x < blocks.length; x++) {
            float csa = cs;
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y].setMaterial(Material.DIRT);
                if (y > topBlocks[x] && y < topBlocks[x] + progressionBlocks) {
                    csa += dif;
                }

                if (random.nextFloat() <= csa)
                    blocks[x][y].setMaterial(Material.STONE);
                else
                    blocks[x][y].setMaterial(Material.AIR);
            }
        }

        for (int i = 0; i < iterations; i++) {
            refine();
            loadingBar.setStatus("Generating caves.");
        }


        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                if (y == topBlocks[x]) {

                    if (x == blocks.length / 2) {
                        spawnX = x;
                        spawnY = y - 3;
                    }

                    for(int i = 2; i < height - y; i++) {
                        walls[x][y + i].setMaterial(Material.STONE_WALL);
                    }

                    for (int i = 0; i < stoneAmount; i++) {
                        blocks[x][y + i].setMaterial(Material.STONE);
                    }

                    for (int i = 0; i < dirtAmount; i++) {
                        blocks[x][y + i].setMaterial(Material.DIRT);
                        walls[x][y + i + 2].setMaterial(Material.DIRT_WALL);
                        System.out.println("setting a wall. wtf");
                    }

                    break;
                }
            }
        }

    }

    public void refine() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int nbs = getNeighbors(x, y);

                if (nbs > 4) {
                    getBlock(x, y).setMaterial(Material.STONE);
                } else if (nbs < 4) {
                    getBlock(x, y).setMaterial(Material.AIR);
                }
            }
        }
    }

    public int getNeighbors(int x, int y) {
        int wallCount = 0;
        for (int nx = x - 1; nx <= x + 1; nx++) {
            for (int ny = y - 1; ny <= y + 1; ny++) {
                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (nx != x || ny != y) {
                        if (getBlock(nx, ny).getMaterial() == Material.STONE) wallCount++;
                    }
                } else {
                    wallCount++;
                }
            }
        }

        return wallCount;
    }

    public int getSpawnX() {
        return this.spawnX;
    }

    public int getSpawnY() {
        return this.spawnY;
    }

    public Block[][] getBlocks() {
        return this.blocks;
    }

    public Block[][] getWalls() {
        return this.walls;
    }

    public void setBlock(Material material, int x, int y) {
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1)
            return;
        blocks[x][y] = new Block(material, x, y);
    }

    public Block getBlock(int x, int y) { // TODO put these other places so everything can access them
        if (x < 0 || x > width - 1 || y < 0 || y > height - 1)
            return null;

        return blocks[x][y];
    }


}