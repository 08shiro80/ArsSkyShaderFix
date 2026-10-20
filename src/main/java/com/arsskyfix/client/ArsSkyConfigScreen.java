package com.arsskyfix.client;

import com.arsskyfix.ArsSkyConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class ArsSkyConfigScreen extends Screen {

    private final Screen parent;

    public ArsSkyConfigScreen(Screen parent) {
        super(Component.literal("Ars Sky Shader Fix"));
        this.parent = parent;
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new ArsSkyConfigScreen(parent)));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int w = 220;
        int left = centerX - w / 2;
        int y = this.height / 4;
        int gap = 24;

        addRenderableWidget(CycleButton.<ArsSkyConfig.RenderMode>builder(mode -> Component.literal(mode.name()))
            .withValues(ArsSkyConfig.RenderMode.values())
            .withInitialValue(ArsSkyConfig.MODE.get())
            .create(left, y, w, 20, Component.literal("Render Mode"),
                (btn, value) -> ArsSkyConfig.MODE.set(value)));
        y += gap;

        addRenderableWidget(CycleButton.onOffBuilder(ArsSkyConfig.TOP_MARKER.get())
            .create(left, y, w, 20, Component.literal("Top Marker"),
                (btn, value) -> ArsSkyConfig.TOP_MARKER.set(value)));
        y += gap;

        addRenderableWidget(new ColorSlider(left, y, w, 20, "Top Color R", ArsSkyConfig.TOP_R));
        y += gap;
        addRenderableWidget(new ColorSlider(left, y, w, 20, "Top Color G", ArsSkyConfig.TOP_G));
        y += gap;
        addRenderableWidget(new ColorSlider(left, y, w, 20, "Top Color B", ArsSkyConfig.TOP_B));
        y += gap + 8;

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
            .bounds(left, y, w, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 24, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        ArsSkyConfig.SPEC.save();
        this.minecraft.setScreen(this.parent);
    }

    private static final class ColorSlider extends AbstractSliderButton {

        private final String label;
        private final net.minecraftforge.common.ForgeConfigSpec.IntValue cfg;

        ColorSlider(int x, int y, int w, int h, String label, net.minecraftforge.common.ForgeConfigSpec.IntValue cfg) {
            super(x, y, w, h, Component.empty(), cfg.get() / 255.0);
            this.label = label;
            this.cfg = cfg;
            updateMessage();
        }

        private int channel() {
            return (int) Math.round(this.value * 255.0);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + channel()));
        }

        @Override
        protected void applyValue() {
            cfg.set(channel());
        }
    }
}
