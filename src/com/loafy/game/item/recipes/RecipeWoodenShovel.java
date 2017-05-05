package com.loafy.game.item.recipes;

import com.loafy.game.entity.player.CraftingList;
import com.loafy.game.entity.player.PlayerInventory;
import com.loafy.game.item.ItemStack;
import com.loafy.game.item.Recipe;
import com.loafy.game.item.material.ItemPlantFiber;
import com.loafy.game.item.material.ItemWood;
import com.loafy.game.item.tool.WoodenShovel;

public class RecipeWoodenShovel extends Recipe {

    public RecipeWoodenShovel(PlayerInventory inventory, CraftingList list) {
        super(inventory, list);

        addIngredient(new ItemStack(new ItemWood(), 15));
        addIngredient(new ItemStack(new ItemPlantFiber(), 6));
    }

    public ItemStack result() {
        return new ItemStack(new WoodenShovel(), 1);
    }
}
