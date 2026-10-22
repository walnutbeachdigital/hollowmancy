package com.walnutbeach.hollowmancy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

/** Client-only: opens the Spellbook screen. */
public class SpellbookClient {

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new SpellbookScreen());
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f));
    }
}