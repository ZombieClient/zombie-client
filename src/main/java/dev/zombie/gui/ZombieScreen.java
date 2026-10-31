package dev.zombie.gui;

import dev.zombie.ZombieClient;
import dev.zombie.hack.Hack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ZombieScreen extends Screen {
    public ZombieScreen() { super(Text.literal("Zombie Client")); }

    @Override
    protected void init() {
        List<Hack> hacks = ZombieClient.hacks().all();
        int cols = 3, w = 100, h = 20, gap = 4;
        int startX = width / 2 - (cols * w + (cols - 1) * gap) / 2;
        for (int i = 0; i < hacks.size(); i++) {
            Hack hack = hacks.get(i);
            int x = startX + (i % cols) * (w + gap);
            int y = 40 + (i / cols) * (h + gap);
            addDrawableChild(ButtonWidget.builder(label(hack), b -> {
                hack.toggle();
                b.setMessage(label(hack));
            }).dimensions(x, y, w, h).build());
        }
    }

    private static Text label(Hack h) {
        return Text.literal(h.name + ": ")
                .append(Text.literal(h.isEnabled() ? "ON" : "OFF")
                        .formatted(h.isEnabled() ? Formatting.GREEN : Formatting.RED));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFF55FF55);
    }
}
