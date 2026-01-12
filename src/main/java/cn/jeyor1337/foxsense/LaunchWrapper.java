package cn.jeyor1337.foxsense;

import net.fabricmc.api.ClientModInitializer;

public class LaunchWrapper implements ClientModInitializer {
    private final Foxsense foxsense = new Foxsense();

    @Override
    public void onInitializeClient() {
        foxsense.onInitializeClient();
    }

}
