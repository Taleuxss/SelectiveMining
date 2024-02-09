package taleuxss;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class SelectiveMiningClient implements ClientModInitializer {
    private static final String CATEGORY = "SelectiveMining";
    private static final KeyBinding TOGGLE_KEYBIND = new KeyBinding("Toggle Selective Mining", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    private static boolean isEnabled = false;
    private static Block allowedBlock = null;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYBIND);
        AttackBlockCallback.EVENT.register(this::handleAttackBlock);
        ClientTickEvents.END_CLIENT_TICK.register(this::handleClientTick);
    }

    private void handleClientTick(MinecraftClient client) {
        if (TOGGLE_KEYBIND.wasPressed()) {
            toggleSelectiveMining(client);
        }
        if (isEnabled) {
            updateAllowedBlockIfNotMining(client);
        }
    }

    private void updateAllowedBlockIfNotMining(MinecraftClient client) {
        if (!isMiningButtonPressed()) {
            updateAllowedBlock(client);
        }
    }

    private boolean isMiningButtonPressed() {
        return MinecraftClient.getInstance().options.attackKey.isPressed();
    }

    private ActionResult handleAttackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos pos, Direction direction) {
        if (isEnabled) {
            Block block = world.getBlockState(pos).getBlock();
            return block.equals(allowedBlock) ? ActionResult.PASS : ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private void toggleSelectiveMining(MinecraftClient client) {
        isEnabled = !isEnabled;
        displayMessageOnScreen(client, isEnabled ? "Enabled Selective Mining" : "Disabled Selective Mining", isEnabled ? Formatting.GREEN : Formatting.RED);
    }

    private void updateAllowedBlock(MinecraftClient client) {
        HitResult hitResult = client.crosshairTarget;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            assert client.world != null;
            allowedBlock = client.world.getBlockState(blockHitResult.getBlockPos()).getBlock();
        }
    }

    private void displayMessageOnScreen(MinecraftClient client, String message, Formatting formatting) {
        client.inGameHud.setOverlayMessage(Text.of(formatting + message), false);
    }
}
