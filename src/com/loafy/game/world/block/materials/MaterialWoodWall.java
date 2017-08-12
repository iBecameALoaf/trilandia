package com.loafy.game.world.block.materials;

import com.loafy.game.world.block.Material;

import java.awt.*;

import static com.loafy.game.world.block.MaterialType.WALL;

public class MaterialWoodWall extends Material {

    public MaterialWoodWall() {
        super("wood_wall",21, 70, 0.1f, true, true, WALL, "Wood Wall", new Color(76, 59, 45));
    }
}
