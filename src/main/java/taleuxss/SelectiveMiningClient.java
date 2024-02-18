package taleuxss;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class SelectiveMiningClient implements ClientModInitializer {
    private static final KeyBinding TOGGLE_KEYBIND = new KeyBinding("Toggle Selective Mining", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "SelectiveMining");
    private static boolean isEnabled = false;
    private static Block allowedBlock = null;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYBIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> { if (TOGGLE_KEYBIND.wasPressed()) toggleSelectiveMining(client); if (isEnabled && allowedBlock != null && !client.options.attackKey.isPressed() ) { allowedBlock = null; }});
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> { if ( allowedBlock == null) { allowedBlock = state.getBlock(); } return true; });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> isEnabled && allowedBlock != null && !world.getBlockState(pos).isOf(allowedBlock) ? ActionResult.FAIL : ActionResult.PASS);
    }

    private void toggleSelectiveMining(MinecraftClient client) {
        isEnabled = !isEnabled;
        client.inGameHud.setOverlayMessage(Text.of((isEnabled ? Formatting.GREEN : Formatting.RED) + (isEnabled ? "Enabled Selective Mining" : "Disabled Selective Mining")), false);
    }
}