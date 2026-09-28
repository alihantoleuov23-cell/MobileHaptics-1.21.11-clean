package ru.mobilehaptics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class HapticsScreen extends Screen {
    private final Screen parent;

    private ButtonWidget enabledButton;
    private ButtonWidget breakButton;
    private ButtonWidget placeButton;

    public HapticsScreen(Screen parent) {
        super(Text.translatable("screen.mobile-haptics.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        MobileHapticsConfig config = HapticManager.getConfig();

        int centerX = this.width / 2;
        int y = this.height / 2 - 80;

        enabledButton = this.addDrawableChild(
                ButtonWidget.builder(enabledText(config), button -> {
                    config.enabled = !config.enabled;
                    updateButtonTexts();
                    HapticManager.save();
                }).dimensions(centerX - 100, y, 200, 20).build()
        );

        y += 25;

        breakButton = this.addDrawableChild(
                ButtonWidget.builder(breakText(config), button -> {
                    config.breakEnabled = !config.breakEnabled;
                    updateButtonTexts();
                    HapticManager.save();
                }).dimensions(centerX - 100, y, 200, 20).build()
        );

        y += 25;

        placeButton = this.addDrawableChild(
                ButtonWidget.builder(placeText(config), button -> {
                    config.placeEnabled = !config.placeEnabled;
                    updateButtonTexts();
                    HapticManager.save();
                }).dimensions(centerX - 100, y, 200, 20).build()
        );

        y += 30;

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("screen.mobile-haptics.test"),
                        button -> HapticManager.test()
                ).dimensions(centerX - 100, y, 200, 20).build()
        );

        y += 30;

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("screen.mobile-haptics.break_strength",
                                config.breakStrength),
                        button -> {
                            config.breakStrength += 10;

                            if (config.breakStrength > 100) {
                                config.breakStrength = 10;
                            }

                            HapticManager.save();
                            clearAndInit();
                        }
                ).dimensions(centerX - 100, y, 200, 20).build()
        );

        y += 25;

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("screen.mobile-haptics.place_strength",
                                config.placeStrength),
                        button -> {
                            config.placeStrength += 10;

                            if (config.placeStrength > 100) {
                                config.placeStrength = 10;
                            }

                            HapticManager.save();
                            clearAndInit();
                        }
                ).dimensions(centerX - 100, y, 200, 20).build()
        );

        y += 30;

        this.addDrawableChild(
                ButtonWidget.builder(
                        Text.translatable("gui.done"),
                        button -> close()
                ).dimensions(centerX - 100, y, 200, 20).build()
        );
    }

    private void clearAndInit() {
        this.clearChildren();
        this.init();
    }

    private void updateButtonTexts() {
        MobileHapticsConfig config = HapticManager.getConfig();

        enabledButton.setMessage(enabledText(config));
        breakButton.setMessage(breakText(config));
        placeButton.setMessage(placeText(config));
    }

    private Text enabledText(MobileHapticsConfig config) {
        return Text.translatable(
                "screen.mobile-haptics.enabled",
                config.enabled
        );
    }

    private Text breakText(MobileHapticsConfig config) {
        return Text.translatable(
                "screen.mobile-haptics.break",
                config.breakEnabled
        );
    }

    private Text placeText(MobileHapticsConfig config) {
        return Text.translatable(
                "screen.mobile-haptics.place",
                config.placeEnabled
        );
    }

    @Override
    public void close() {
        HapticManager.save();

        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                25,
                0xFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}