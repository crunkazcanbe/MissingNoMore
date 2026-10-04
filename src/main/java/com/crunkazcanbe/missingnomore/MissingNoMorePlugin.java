package com.crunkazcanbe.missingnomore;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Scans every registered item/block stack and, if its baked model is the
 * missing model OR its texture is the missing-texture sprite, blacklists it
 * from the HEI/JEI item list so it never shows up when you open your inventory.
 *
 * Truly de-registering items would corrupt recipes and existing worlds, so we
 * hide them from the list instead — which is exactly what you see when you hit E.
 */
@JEIPlugin
public class MissingNoMorePlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getRenderItem() == null) {
            return; // dedicated server / no render context — nothing to do
        }

        final IItemBlacklist blacklist = registry.getJeiHelpers().getItemBlacklist();
        final RenderItem renderItem = mc.getRenderItem();
        final ItemModelMesher mesher = renderItem.getItemModelMesher();
        final IBakedModel missingModel = mesher.getModelManager().getMissingModel();
        final TextureAtlasSprite missingSprite =
            mc.getTextureMapBlocks() != null ? mc.getTextureMapBlocks().getMissingSprite() : null;

        int scanned = 0;
        int hidden = 0;

        for (Item item : ForgeRegistries.ITEMS) {
            if (item == null) {
                continue;
            }

            NonNullList<ItemStack> stacks = NonNullList.create();
            try {
                // SEARCH tab pulls every variant the item exposes across its creative tabs
                item.getSubItems(CreativeTabs.SEARCH, stacks);
            } catch (Throwable ignored) {
                // some modded items throw here — fall through to the plain stack
            }
            if (stacks.isEmpty()) {
                try {
                    stacks.add(new ItemStack(item));
                } catch (Throwable ignored) {
                    continue;
                }
            }

            for (ItemStack stack : stacks) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                scanned++;
                try {
                    IBakedModel model = mesher.getItemModel(stack);
                    boolean missing = (model == null) || (model == missingModel);
                    if (!missing && missingSprite != null) {
                        TextureAtlasSprite particle = model.getParticleTexture();
                        if (particle == missingSprite) {
                            missing = true;
                        }
                    }
                    if (missing) {
                        blacklist.addItemToBlacklist(stack);
                        hidden++;
                    }
                } catch (Throwable t) {
                    // a stack that explodes during model lookup is itself broken — hide it too
                    try {
                        blacklist.addItemToBlacklist(stack);
                        hidden++;
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        System.out.println("[MissingNoMore] scanned " + scanned + " stacks, hid " + hidden
            + " missing-texture items/blocks from the HEI list.");
    }
}
