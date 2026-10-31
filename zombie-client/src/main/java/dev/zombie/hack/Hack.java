package dev.zombie.hack;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public abstract class Hack {
    public enum Category { COMBAT, MOVEMENT, RENDER, PLAYER }

    protected final MinecraftClient mc = MinecraftClient.getInstance();
    public final String name;
    public final Category category;
    public final int key; // GLFW key code, -1 = unbound
    public KeyBinding binding; // set at startup, rebindable in Controls
    private boolean enabled;

    protected Hack(String name, Category category, int key) {
        this.name = name;
        this.category = category;
        this.key = key;
    }

    public boolean isEnabled() { return enabled; }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
}
