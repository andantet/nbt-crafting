/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.mixin.smithing;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.recipe.IngredientRecipe;

@Mixin(SmithingScreenHandler.class)
public abstract class MixinSmithingScreenHandler extends ForgingScreenHandler {
	@Unique
	private static DefaultedList<ItemStack> remainders = null;

	public MixinSmithingScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("HEAD")
	)
	protected void onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		World world = player.getWorld();
		Optional<SmithingRecipe> match = world.getRecipeManager().getFirstMatch(RecipeType.SMITHING, input, world);
		remainders = match.map(inventoryIngredientRecipe -> inventoryIngredientRecipe.getRemainder(input)).orElse(null);
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("TAIL")
	)
	protected void onOutputTaken(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		if (remainders != null) {
			context.run((world, blockPos) -> {
				RecipeUtil.putRemainders(remainders, input, world, blockPos);
			});
		}
	}
}
