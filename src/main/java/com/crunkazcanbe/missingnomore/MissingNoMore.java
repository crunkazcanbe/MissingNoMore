package com.crunkazcanbe.missingnomore;

import net.minecraftforge.fml.common.Mod;

/**
 * MissingNoMore — hides items/blocks that render with the missing texture/model
 * (the purple-and-black "MissingNo" look) from the HEI/JEI item list.
 * All the real work happens in {@link MissingNoMorePlugin}; this is just the mod entry point.
 */
@Mod(
    modid = MissingNoMore.MODID,
    name = "MissingNoMore",
    version = "1.0.0",
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.12.2]"
)
public class MissingNoMore {
    public static final String MODID = "missingnomore";
}
