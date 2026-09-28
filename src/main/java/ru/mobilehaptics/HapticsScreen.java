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

        /*
         * Global vibration
         */
        this.enabledButton =
                CycleButton.onOffBuilder(config.enabled)
                        .create(
                                centerX - 100,
                                35,
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
                this.enabledButton
        );

        /*
         * Block breaking
         */
        this.breakButton =
                CycleButton.onOffBuilder(
                                config.breakEnabled
                        )
                        .create(
                                centerX - 100,
                                58,
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
                this.breakButton
        );

        /*
         * Block placing
         */
        this.placeButton =
                CycleButton.onOffBuilder(
                                config.placeEnabled
                        )
                        .create(
                                centerX - 100,
                                81,
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
                this.placeButton
        );

        /*
         * Duration
         */
        this.breakDurationBox =
                createNumberBox(
                        centerX - 180,
                        123,
                        Integer.toString(
                                config.breakDuration
                        ),
                        150
                );

        this.placeDurationBox =
                createNumberBox(
                        centerX + 30,
                        123,
                        Integer.toString(
                                config.placeDuration
                        ),
                        150
                );

        /*
         * Strength
         */
        this.breakStrengthBox =
                createNumberBox(
                        centerX - 180,
                        161,
                        Integer.toString(
                                config.breakStrength
                        ),
                        150
                );

        this.placeStrengthBox =
                createNumberBox(
                        centerX + 30,
                        161,
                        Integer.toString(
                                config.placeStrength
                        ),
                        150
                );

        /*
         * Test
         */
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.mobile_haptics.test"
                                ),
                                button ->
                                        HapticManager.test()
                        )
                        .bounds(
                                centerX - 150,
                                190,
                                300,
                                20
                        )
                        .build()
        );

        /*
         * Save
         */
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "button.mobile_haptics.save"
                                ),
                                button ->
                                        saveAndClose()
                        )
                        .bounds(
                                centerX - 150,
                                215,
                                300,
                                20
                        )
                        .build()
        );
    }

    private EditBox createNumberBox(
            int x,
            int y,
            String value,
            int width
    ) {

        EditBox box =
                new EditBox(
                        this.font,
                        x,
                        y,
                        width,
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
                                this.breakDurationBox.getValue(),
                                config.breakDuration
                        ),
                        5,
                        200
                );

        config.placeDuration =
                clamp(
                        parseInt(
                                this.placeDurationBox.getValue(),
                                config.placeDuration
                        ),
                        5,
                        200
                );

        config.breakStrength =
                clamp(
                        parseInt(
                                this.breakStrengthBox.getValue(),
                                config.breakStrength
                        ),
                        1,
                        100
                );

        config.placeStrength =
                clamp(
                        parseInt(
                                this.placeStrengthBox.getValue(),
                                config.placeStrength
                        ),
                        1,
                        100
                );

        HapticManager.save();

        this.minecraft.setScreen(
                this.parent
        );
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
                Math.min(
                        value,
                        max
                )
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        /*
         * Не используем renderBackground(),
         * потому что в Minecraft 1.21.11 он
         * вызывает blur и здесь возникает
         * "Can only blur once per frame".
         */
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                0xFF101010
        );

        graphics.drawCenteredString(
                this.font,
                this.title,
                this.width / 2,
                15,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.break_duration"
                ),
                this.width / 2 - 180,
                111,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.place_duration"
                ),
                this.width / 2 + 30,
                111,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.break_strength"
                ),
                this.width / 2 - 180,
                149,
                0xFFFFFF
        );

        graphics.drawString(
                this.font,
                Component.translatable(
                        "label.mobile_haptics.place_strength"
                ),
                this.width / 2 + 30,
                149,
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
        this.minecraft.setScreen(
                this.parent
        );
    }
}