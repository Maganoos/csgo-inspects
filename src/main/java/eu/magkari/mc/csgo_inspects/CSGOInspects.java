package eu.magkari.mc.csgo_inspects;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;


public class CSGOInspects implements ClientModInitializer {
	public static final String MOD_ID = "csgo-inspects";
	public static boolean shouldInspect = false;

	@Override
	public void onInitializeClient() {
		KeyBinding bind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.csgo-inspects.inspect",
			GLFW.GLFW_KEY_G,
			"key.categories.misc"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (bind.wasPressed()) {
				shouldInspect = true;
			}
		});
	}
}