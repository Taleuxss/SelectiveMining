package taleuxss;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class SelectiveMiningClient implements ClientModInitializer {
    private static final KeyBinding TOGGLE_KEYBIND = new KeyBinding("Toggle Selective Mining", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "SelectiveMining");
    private enum Mode {DISABLED, ENABLED, BLOCKSTATE, POSITION}
    private static Mode mode = Mode.DISABLED;
    private static BlockState allowedBlockState = null;
    private static BlockPos allowedPos = null;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYBIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TOGGLE_KEYBIND.wasPressed()) toggleSelectiveMining(client);
            if (mode != Mode.DISABLED && !client.options.attackKey.isPressed()) allowedBlockState = null;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            BlockState currentState = world.getBlockState(pos);
            if (allowedBlockState == null) {
                allowedBlockState = currentState;
                allowedPos = pos;
            }
            return switch (mode) {
                case ENABLED -> currentState.isOf(allowedBlockState.getBlock()) ? ActionResult.PASS : ActionResult.FAIL;
                case BLOCKSTATE -> currentState.equals(allowedBlockState) ? ActionResult.PASS : ActionResult.FAIL;
                case POSITION -> pos.equals(allowedPos) ? ActionResult.PASS : ActionResult.FAIL;
                case DISABLED -> ActionResult.PASS;
            };
        });
    }

    private void toggleSelectiveMining(MinecraftClient client) {
        switch (mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length]) {
            case ENABLED -> client.inGameHud.setOverlayMessage(Text.of(Formatting.GREEN + "Enabled Selective Mining"), false);
            case BLOCKSTATE -> client.inGameHud.setOverlayMessage(Text.of(Formatting.YELLOW + "Selective Mining For BlockState"), false);
            case POSITION -> client.inGameHud.setOverlayMessage(Text.of(Formatting.GOLD + "Selective Mining For Position"), false);
            case DISABLED -> client.inGameHud.setOverlayMessage(Text.of(Formatting.RED + "Disabled Selective Mining"), false);
        }
    }
}