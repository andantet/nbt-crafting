package de.siphalor.nbtcrafting.ingredient;

import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.stream.Stream;

public interface IIngredient {
	void setAdvancedEntries(Stream<? extends IngredientEntry> entries);

	ItemStack getRecipeRemainder(ItemStack stack, Map<String, Object> reference);
}
