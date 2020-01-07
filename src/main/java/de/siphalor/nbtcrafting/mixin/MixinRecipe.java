package de.siphalor.nbtcrafting.mixin;

import de.siphalor.nbtcrafting.ingredient.IIngredient;
import de.siphalor.nbtcrafting.util.nbt.NbtHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;

@Mixin(Recipe.class)
public interface MixinRecipe {
	@Shadow
	DefaultedList<Ingredient> getPreviewInputs();

	/**
	 * @reason Returns the recipe remainders. Sadly has to overwrite since this is an interface.
     * @author Siphalor
	 */
	@Overwrite
	default DefaultedList<ItemStack> getRemainingStacks(Inventory inventory) {
		final DefaultedList<ItemStack> stackList = DefaultedList.ofSize(inventory.getInvSize(), ItemStack.EMPTY);
		HashMap<String, Object> reference = new HashMap<>();
        DefaultedList<Ingredient> ingredients = getPreviewInputs();
		for (int j = 0; j < ingredients.size(); j++) {
			for (int i = 0; i < stackList.size(); i++) {
				if(ingredients.get(j).test(inventory.getInvStack(i)))
					reference.putIfAbsent("i"+j, NbtHelper.getTagOrEmpty(inventory.getInvStack(i)));
			}
		}
		main:
		for(int i = 0; i < stackList.size(); ++i) {
			ItemStack itemStack = inventory.getInvStack(i);
			for(Ingredient ingredient : ingredients) {
				if(ingredient.test(itemStack)) {
					//noinspection ConstantConditions
					ItemStack remainder = ((IIngredient)(Object) ingredient).getRecipeRemainder(itemStack, reference);
					if(remainder != null) {
						stackList.set(i, remainder);
						continue main;
					}
				}
			}
			if (itemStack.getItem().hasRecipeRemainder()) {
				stackList.set(i, new ItemStack(itemStack.getItem().getRecipeRemainder()));
			}
		}
		return stackList;
	}
}
