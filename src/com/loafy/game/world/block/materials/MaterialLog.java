package com.loafy.game.world.block.materials;

import com.loafy.game.item.ItemStack;
import com.loafy.game.item.material.ItemWood;
import com.loafy.game.world.World;
import com.loafy.game.world.block.Block;
import com.loafy.game.world.block.Material;
import com.loafy.game.world.block.MaterialType;
import com.loafy.game.world.lighting.LightMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MaterialLog extends Material {

    public MaterialLog() {
        super("log",4, 2000, LightMap.DEFAULT, true, true, MaterialType.BLOCK, "Log", new Color(72, 49, 35));
    }

    public void destroy(World world, float x, float y) {
        int blockX = (int) x;
        int blockY = (int) y;

        outer:
        for (int i = 1; i < 50; i++) {
            Block block = world.getBlockFromChunks(blockX, blockY - (i * Material.SIZE));

            if (block == null) continue;

            if (block.getMaterial().getID() == Material.LOG.getID()) {
                block.destroyClear(world, block);
            } else {

                blockX = (int) block.getX() - Material.SIZE;
                blockY = (int) block.getY() - Material.SIZE * 2;

                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 3; k++) {
                        Block leafblock = world.getBlockFromChunks(blockX + (j * Material.SIZE), blockY + (k * Material.SIZE));
                        if (leafblock.getMaterial() == Material.LEAF) {
                            block.destroyClear(world, leafblock);
                        } else {
                            break outer;
                        }
                    }
                }
            }
        }
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> list = new ArrayList<>();
        list.add(new ItemStack(new ItemWood(), 1));
        return list;
    }
}
