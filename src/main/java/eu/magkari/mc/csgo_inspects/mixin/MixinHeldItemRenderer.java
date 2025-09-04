package eu.magkari.mc.csgo_inspects.mixin;

import static eu.magkari.mc.csgo_inspects.CSGOInspects.shouldInspect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemRenderer {
    @Unique
    private static long inspectStartTime = -1;

    @Inject(method = "renderFirstPersonItem",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderFirstPersonItem(
            AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress,
            ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
            int light, CallbackInfo ci
    ) {
        if (shouldInspect) {
            boolean right = (hand == Hand.MAIN_HAND && player.getMainArm() == Arm.RIGHT) ||
                    (hand == Hand.OFF_HAND && player.getMainArm() == Arm.LEFT);

            if (inspectStartTime == -1) {
                inspectStartTime = System.currentTimeMillis();
            }

            matrices.push();

            float elapsed = (System.currentTimeMillis() - inspectStartTime) / 1000f;

            float transX = 0.0f;
            float transY = -0.5f;
            float transZ = -0.72f;
            float rotX = 0f;
            float rotY = 0f;
            float rotZ = 0f;

            if (elapsed < 0.4f) {
                float t = elapsed / 0.4f;
                transY += -0.2f * (1 - t);
            } else if (elapsed < 0.8f) {
                float t = (elapsed - 0.4f) / 0.4f;
                rotY = -30f * t;
            } else if (elapsed < 1.2f) {
                float t = (elapsed - 0.8f) / 0.4f;
                rotY = -30f + 60f * t;
            } else if (elapsed < 1.8f) {
                float t = (elapsed - 1.2f) / 0.6f;
                t = (float)Math.sin(t * Math.PI * 0.5f);

                rotX = -45f - 30f * t;
                rotZ = 20f + 20f * t;
                rotY = 0f;

                transX = right ? 0.15f : -0.15f;
                transZ = -0.72f + 0.2f * t;
            } else if (elapsed < 2.2f) {
                float t = (elapsed - 1.8f) / 0.4f;
                rotX = -45f + 45f * t;
                rotY = 30f - 30f * t;
                rotZ = 30f - 30f * t;
                transY += 0.2f - 0.2f * t;
            } else {
                shouldInspect = false;
                inspectStartTime = -1;
            }

            if (right) {
                matrices.translate(0.6 + transX, transY, transZ);
            } else {
                matrices.translate(-0.6 + transX, transY, transZ);
            }

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotX));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotY));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotZ));

            ((HeldItemRenderer)(Object)this).renderItem(
                    player,
                    item,
                    right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                    matrices,
                    vertexConsumers,
                    light
            );

            matrices.pop();
            ci.cancel();
        } else {
            inspectStartTime = -1;
        }
    }
}
