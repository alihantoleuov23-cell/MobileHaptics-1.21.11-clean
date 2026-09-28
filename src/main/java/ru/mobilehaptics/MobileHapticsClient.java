package ru.mobilehaptics;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class MobileHapticsClient implements ClientModInitializer {
    private static final String MOD_ID = "mobile-haptics";

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "settings")
    );

    private static final KeyMapping SETTINGS_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.mobile_haptics.settings",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_H,
                    CATEGORY
            )
    );

    @Override
    public void onInitializeClient() {
        HapticManager.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SETTINGS_KEY.consumeClick()) {
                client.setScreen(new HapticsScreen(client.screen));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("mobilehaptics")
                            .executes(context -> {
                                Minecraft client = Minecraft.getInstance();
                                client.setScreen(new HapticsScreen(client.screen));
                                return 1;
                            })
            );
        });
    }
}
