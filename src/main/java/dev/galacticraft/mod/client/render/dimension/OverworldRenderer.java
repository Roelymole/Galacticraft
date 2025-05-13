/*
 * Copyright (c) 2019-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.render.dimension;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dev.galacticraft.mod.Constant;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

// TODO: Allow support for more planets
public class OverworldRenderer extends SpaceSkyRenderer {
    public static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    @Nullable
    private VertexBuffer skyBuffer;
    private Minecraft minecraft = Minecraft.getInstance();

    public OverworldRenderer() {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        this.createSky();
    }

    private void createSky() {
        if (this.skyBuffer != null) {
            this.skyBuffer.close();
        }
        this.skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.skyBuffer.bind();
        this.skyBuffer.upload(OverworldRenderer.buildSkyDisc(Tesselator.getInstance(), 16.0F));
        VertexBuffer.unbind();
    }

    private static MeshData buildSkyDisc(Tesselator tesselator, float f) {
        float f2 = Math.signum(f) * 512.0F;
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        bufferBuilder.addVertex(0.0F, f, 0.0F);
        for (int i = -180; i <= 180; i += 45) {
            bufferBuilder.addVertex(f2 * Mth.cos(((float) i) * Mth.DEG_TO_RAD), f, 512.0F * Mth.sin(((float) i) * Mth.DEG_TO_RAD));
        }
        return bufferBuilder.buildOrThrow();
    }

    public void renderOverworldSky(Player player, Matrix4f matrix4f, Matrix4f projectionMatrix, float partialTicks, Camera camera, boolean bl, Runnable runnable) {
        runnable.run();
        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(matrix4f);

        float theta = Mth.sqrt(((float) (player.getY()) - Constant.OVERWORLD_SKYPROVIDER_STARTHEIGHT) / 200.0F);
        final float skyModifier = Math.max(1.0F - 0.29F * theta, 0.0F);

        final Vec3 skyColor = this.minecraft.level.getSkyColor(camera.getPosition(), partialTicks);
        float x = (float) skyColor.x * skyModifier;
        float y = (float) skyColor.y * skyModifier;
        float z = (float) skyColor.z * skyModifier;

        // FogRenderer.setupColor(this.minecraft.gameRenderer.getMainCamera(), partialTicks, this.minecraft.level, this.minecraft.options.getEffectiveRenderDistance(), skyModifier);
        FogRenderer.levelFogColor();
        Tesselator tesselator = Tesselator.getInstance();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(x, y, z, 1.0F); // Top half of the sky
        this.skyBuffer.bind();
        this.skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // final float[] sunriseColors = this.minecraft.level.effects().getSunriseColor(this.minecraft.level.getTimeOfDay(partialTicks), partialTicks);
        // if (sunriseColors != null) {
        //     RenderSystem.setShader(GameRenderer::getPositionColorShader);
        //     RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //     poseStack.pushPose();
        //     poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        //     poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(this.minecraft.level.getSunAngle(partialTicks)) < 0.0F ? 180.0F : 0.0F));
        //     poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        //     x = sunriseColors[0] * skyModifier;
        //     y = sunriseColors[1] * skyModifier;
        //     z = sunriseColors[2] * skyModifier;

        //     Matrix4f matrix = poseStack.last().pose();
        //     BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        //     buffer.addVertex(matrix, 0.0F, 100.0F, 0.0F).setColor(x, y, z, sunriseColors[3]);

        //     for (int i = 0; i <= 16; ++i) {
        //         final float angle = ((float) i) * Mth.TWO_PI / 16.0F;
        //         final float S = Mth.sin(angle);
        //         final float C = Mth.cos(angle);
        //         buffer.addVertex(matrix, S * 120.0F, C * 120.0F, -C * 40.0F * sunriseColors[3]).setColor(x, y, z, 0.0F);
        //     }

        //     BufferUploader.drawWithShader(buffer.buildOrThrow());
        //     poseStack.popPose();
        // }

        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // - this.minecraft.level.getRainLevel(partialTicks));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        poseStack.mulPose(Axis.XP.rotationDegrees(this.minecraft.level.getTimeOfDay(partialTicks) * 360.0F));
        double playerHeight = player.getY();

        this.starManager.render(poseStack, projectionMatrix, player.level(), partialTicks);
        // runnable.run();
        // FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, 0.0F, false, 0.0F);

        // Draw sun
        float size = 30.0F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CelestialBodyTextures.SUN);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, -size, 100.0F, -size).setUv(0.0F, 0.0F)
                .addVertex(matrix, size, 100.0F, -size).setUv(1.0F, 0.0F)
                .addVertex(matrix, size, 100.0F, size).setUv(1.0F, 1.0F)
                .addVertex(matrix, -size, 100.0F, size).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Draw moon
        size = 20.0F;
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        float sinphi = this.minecraft.level.getMoonPhase();
        final int cosphi = (int) (sinphi % 4);
        final int var29 = (int) (sinphi / 4 % 2);
        final float yy = (cosphi) / 4.0F;
        final float rand7 = (var29) / 2.0F;
        final float zz = (cosphi + 1) / 4.0F;
        final float rand9 = (var29 + 1) / 2.0F;
        buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, -size, -100.0F, size).setUv(zz, rand9)
                .addVertex(matrix, size, -100.0F, size).setUv(yy, rand9)
                .addVertex(matrix, size, -100.0F, -size).setUv(yy, rand7)
                .addVertex(matrix, -size, -100.0F, -size).setUv(zz, rand7);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        poseStack.popPose();
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

        double heightOffset = playerHeight - 64;
        if (heightOffset > this.minecraft.options.getEffectiveRenderDistance() * 16) {
            theta *= 400.0F;

            final float sinth = Math.max(Math.min(theta / 100.0F - 0.2F, 0.5F), 0.0F);

            poseStack.pushPose();
            float scale = 850 * (0.25F - theta / 10000.0F);
            scale = Math.max(scale, 0.2F);
            poseStack.scale(scale, 1.0F, scale);
            poseStack.translate(0.0F, -(float) player.getY(), 0.0F);

            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, CelestialBodyTextures.EARTH);
            RenderSystem.setShaderColor(sinth, sinth, sinth, 1.0F);
            buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            size = 1.0F;
            float zoomIn = 0.0F;
            float cornerB = 1.0F - zoomIn;
            matrix = poseStack.last().pose();
            buffer.addVertex(matrix, -size, 0, size).setUv(zoomIn, cornerB)
                    .addVertex(matrix, size, 0, size).setUv(cornerB, cornerB)
                    .addVertex(matrix, size, 0, -size).setUv(cornerB, zoomIn)
                    .addVertex(matrix, -size, 0, -size).setUv(zoomIn, zoomIn);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
            poseStack.popPose();
        }

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        // RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
    }

    public static Vec3 getFogColor(ClientLevel level, float partialTicks, Vec3 cameraPos) {
        float heightOffset = ((float) (cameraPos.y()) - Constant.OVERWORLD_SKYPROVIDER_STARTHEIGHT) / 200.0F;
        heightOffset = Math.max(1.0F - 0.29F * Mth.sqrt(heightOffset), 0.0F);

        Vec3 skyColor = level.getSkyColor(cameraPos, partialTicks);
        return skyColor.scale(heightOffset);

        // float y = Mth.clamp(Mth.cos(level.getTimeOfDay(partialTicks) * Mth.TWO_PI) * 2.0F + 0.5F, 0.0F, 1.0F);
        // BiomeManager biomeManager = level.getBiomeManager();
        // Vec3 vec32 = cameraPos.subtract(2.0, 2.0, 2.0).scale(0.25);
        // Vec3 vec = CubicSampler.gaussianSampleVec3(
        //         vec32,
        //         (ix, j, k) -> level.effects().getBrightnessDependentFogColor(Vec3.fromRGB24(biomeManager.getNoiseBiomeAtQuart(ix, j, k).value().getFogColor()), y)
        // );

        // return vec.scale(heightOffset);
    }
}
