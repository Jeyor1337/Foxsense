package cn.jeyor1337.foxsense.base.util.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class ItemUtils {
    public static boolean isSwordItem(Item item) {
        return item == Items.WOODEN_SWORD ||
                item == Items.STONE_SWORD ||
                item == Items.IRON_SWORD ||
                item == Items.GOLDEN_SWORD ||
                item == Items.DIAMOND_SWORD ||
                item == Items.NETHERITE_SWORD;
    }
}
