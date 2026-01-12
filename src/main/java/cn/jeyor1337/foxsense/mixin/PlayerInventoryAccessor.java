package cn.jeyor1337.foxsense.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.player.PlayerInventory;

@Mixin(PlayerInventory.class)
public interface PlayerInventoryAccessor {
    @Overwrite
    @Accessor("selectedSlot")
    int getSelectedSlot();

    @Overwrite
    @Accessor("selectedSlot")
    void setSelectedSlot(int slot);
}
