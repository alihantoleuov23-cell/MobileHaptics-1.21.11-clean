package ru.mobilehaptics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HapticsScreen extends Screen {

    private final Screen parent;

    private CycleButton<Boolean> enabledButton;
    private CycleButton<Boolean> breakButton;
    private CycleButton<Boolean> placeButton;

    private EditBox breakDurationBox;
    private EditBox placeDurationBox;
    private EditBox breakStrengthBox;
    private EditBox placeStrengthBox;

    public HapticsScreen(Screen parent) {
        super(Component.translatable(
                "screen.mobile_haptics.title"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;

        MobileHapticsConfig config =
                HapticManager.getConfig();

        int y = 45;

        enabledButton =
                CycleButton.onOffBuilder(config.enabled)
                        .create(
                                centerX - 100,
                                y,
                                200,
                                20,
                                Component.translatable(
                                        "option.mobile_haptics.enabled"
                                ),
                                (button, value) -> {
                                    config.enabled = value;
                                }
                        );

        this.addRenderableWidget(
                enabledButton
        );

        y += 27;

        breakButton =
                CycleButton.onOffBuilder(
                                config.breakEnabled
                        )
                        .create(
                                centerX - 100,
                                y,
                                200,
                                20,
                                Component.translatable(
                                        "option.mobile_haptics.break"
                                ),
                                (button, value) -> {
                                    config.breakEnabled = value;
                                }
                        );

        this.addRenderableWidget(
                breakButton
        );

        y += 27;

        placeButton =
                CycleButton.onOffBuilder(
                                config.placeEnabled
                        )
                        .create(
                                centerX - 100,
                                y,
                                200,
                                20,
                                Component.translatable(
                                        "option.mobile_haptics.place"
                                ),
                                (button, value) -> {
                                    config.placeEnabled = value;
                                }
                        );

        this.addRenderableWidget(
                placeButton
        );

        y += 30;

        breakDurationBox =
                createNumberBox(
                        centerX - 100,
                        y,
                        Integer.toString(
                                config.breakDuration
                        )
                );

        y += 27;

        placeDurationBox =
                createNumberBox(
                        centerX - 100,
                        y,
                        Integer.toString(
                                config.placeDuration
                        )
                );

        y += 27;

        breakStrengthBox =
                createNumberBox(
                        centerX - 100,
                        y,
                        Integer.toString(
                                config.breakStrength
                        )
                );

        y += 27;

        placeStrengthBox =
                createNumberBox(
                        centerX - 100,
                        y,
                        Integer.toString(
                                config.placeStrength
                        )
                );

        y += 32;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.mobile_haptics.test"
                                ),
                                button ->
                                        HapticManager.test()
                        )
                        .bounds(
                                centerX - 100,
                                y,
                                200,
                                20
                        )
                        .build()
        );

        y += 25;

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.mobile_haptics.save"
                                ),
                                button ->
                                        saveAndClose()
                        )
                        .bounds(
                                centerX - 100,
                                y,
                                200,
                                20
                        )
                        .build()
        );
    }

    private EditBox createNumberBox(
            int x,
            int y,
            String value
    ) {

        EditBox box =
                new EditBox(
                        this.font,
                        x,
                        y,
                        200,
                        20,
                        Component.empty()
                );

        box.setValue(value);

        box.setFilter(
                text ->
                        text.matches("\\d{0,3}")
        );

        this.addRenderableWidget(box);

        return box;
    }

    private void saveAndClose() {

        MobileHapticsConfig config =
                HapticManager.getConfig();

        config.breakDuration =
                clamp(
                        parseInt(
                                breakDurationBox.getValue(),
                                config.breakDuration
                        ),
                        5,
                        200
                );

        config.placeDuration =
                clamp(
                        parseInt(
                                placeDurationBox.getValue(),
                                config.placeDuration
                        ),
                        5,
                        200
                );

        config.breakStrength =
                clamp(
                        parseInt(
                                breakStrengthBox.getValue(),
                                config.breakStrength
                        ),
                        1,
                        100
                );

        config.placeStrength =
                clamp(
                        parseInt(
                                placeStrengthBox.getValue(),
                                config.placeStrength
                        ),
                        1,
                        100
                );

        HapticManager.save();

        this.minecraft.setScreen(parent);
    }

    private static int parseInt(
            String value,
            int fallback
    ) {

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(
            int value,
            int min,
            int max
    ) {

        return Math.max(
                min,
                Math.min(value, max)
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        this.renderBackground(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.break_duration"
                ),
                this.width / 2 - 100,
                143,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.place_duration"
                ),
                this.width / 2 - 100,
                170,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.break_strength"
                ),
                this.width / 2 - 100,
                197,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.place_strength"
                ),
                this.width / 2 - 100,
                224,
                0xFFFFFF
        );

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}