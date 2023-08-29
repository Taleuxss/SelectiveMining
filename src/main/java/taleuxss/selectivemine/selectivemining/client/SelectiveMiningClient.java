package taleuxss.selectivemine.selectivemining.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class SelectiveMiningClient implements ClientModInitializer {
    private static final String CATEGORY = "SelectiveMining";
    private static final KeyBinding TOGGLE_KEYBIND = new KeyBinding("Toggle Selective Mining", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    private static boolean isEnabled = false;
    private static Block ALLOWED_BLOCK = null;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYBIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_KEYBIND.wasPressed()) {
                isEnabled = !isEnabled;
            }
            if (isEnabled) {
                GameOptions gameOptions = MinecraftClient.getInstance().options;
                boolean isMiningButtonPressed = gameOptions.attackKey.isPressed();
                if (!isMiningButtonPressed) {
                    updateAllowedBlock(client);
                }
            }
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (isEnabled) {
                Block block = world.getBlockState(pos).getBlock();
                return block.equals(ALLOWED_BLOCK) ? ActionResult.PASS : ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });
    }

    private void updateAllowedBlock(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            ALLOWED_BLOCK = client.world.getBlockState(blockHitResult.getBlockPos()).getBlock();
        }
    }
}
