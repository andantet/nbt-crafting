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

import com.google.common.collect.ImmutableMap;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SmithingTransformRecipe.class)
public class MixinSmithingTransformRecipe {
	@Shadow
	@Final
	ItemStack result;

	private Dollar[] outputDollars;

	private Map<String, Object> buildDollarReference(Inventory inv) {
		return ImmutableMap.of(
				"template", NbtUtil.getTagOrEmpty(inv.getStack(0)),
				"base", NbtUtil.getTagOrEmpty(inv.getStack(1)),
				"addition", NbtUtil.getTagOrEmpty(inv.getStack(2))
		);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(Identifier id, Ingredient template, Ingredient base, Ingredient addition, ItemStack result, CallbackInfo ci){
		outputDollars = DollarParser.extractDollars(result.getNbt(), false);
	}


	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	public void craft(Inventory inventory, DynamicRegistryManager registryManager, CallbackInfoReturnable<ItemStack> cir){
		ItemStack output = RecipeUtil.applyDollars(result.copy(), outputDollars, buildDollarReference(inventory));
		if (output != null) cir.setReturnValue(output);
	}
}
