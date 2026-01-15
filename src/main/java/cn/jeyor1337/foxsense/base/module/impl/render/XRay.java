package cn.jeyor1337.foxsense.base.module.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.cubk.event.annotations.EventTarget;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;

import cn.jeyor1337.foxsense.base.event.EventRender3D;
import cn.jeyor1337.foxsense.base.event.EventTick;
import cn.jeyor1337.foxsense.base.module.Module;
import cn.jeyor1337.foxsense.base.module.ModuleType;
import cn.jeyor1337.foxsense.base.value.BooleanValue;
import cn.jeyor1337.foxsense.base.value.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.DynamicUniforms.TransformsValue;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;

public class XRay extends Module {

    public NumberValue range = new NumberValue("Range", 50, 10, 100, 5);
    public NumberValue updateDelay = new NumberValue("UpdateDelay", 500, 100, 2000, 100);
    public NumberValue brightness = new NumberValue("Brightness", 1.0, 0.0, 1.0, 0.1);

    public BooleanValue outlineMode = new BooleanValue("Outline", true);

    public BooleanValue diamond = new BooleanValue("Diamond", true);
    public BooleanValue emerald = new BooleanValue("Emerald", true);
    public BooleanValue gold = new BooleanValue("Gold", true);
    public BooleanValue iron = new BooleanValue("Iron", true);
    public BooleanValue coal = new BooleanValue("Coal", false);
    public BooleanValue copper = new BooleanValue("Copper", false);
    public BooleanValue redstone = new BooleanValue("Redstone", true);
    public BooleanValue lapis = new BooleanValue("Lapis", true);
    public BooleanValue spawner = new BooleanValue("Spawner", true);
    public BooleanValue chest = new BooleanValue("Chest", true);
    public BooleanValue netherQuartz = new BooleanValue("NetherQuartz", false);
    public BooleanValue ancientDebris = new BooleanValue("AncientDebris", true);
    public BooleanValue antiFakeOre = new BooleanValue("Anti-Fake Ore", false);

    private final List<OreBlock> oreBlocks = new ArrayList<>();
    private final Map<Block, Color> oreColors = new HashMap<>();
    private long lastUpdate = 0;
    private double originalGamma;
    private final Set<String> fakeChunks = new HashSet<>();

    private GpuBuffer vertexBuffer;
    private int indexCount = 0;
    private boolean needsRebuild = true;

    private static class OreBlock {
        final BlockPos pos;
        final Block block;
        final Color color;

        OreBlock(BlockPos pos, Block block, Color color) {
            this.pos = pos;
            this.block = block;
            this.color = color;
        }
    }

    public XRay() {
        super("XRay", ModuleType.RENDER);

        addValues(range, updateDelay, brightness);
        addValues(outlineMode);
        addValues(diamond, emerald, gold, iron, coal, copper, redstone, lapis);
        addValues(spawner, chest, netherQuartz, ancientDebris, antiFakeOre);
    }

    @Override
    protected void onEnable() {
        super.onEnable();

        if (isNull()) {
            return;
        }

        if (mc != null && mc.options != null) {
            originalGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(brightness.getValue().doubleValue());
        }

        initializeColors();
        scanForOres();
        needsRebuild = true;
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (mc != null && mc.options != null) {
            mc.options.getGamma().setValue(originalGamma);
        }

        oreBlocks.clear();
        clearVBO();
    }

    private void clearVBO() {
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        indexCount = 0;
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (isNull())
            return;

        if (mc.options != null && mc.options.getGamma().getValue() < brightness.getValue().doubleValue()) {
            mc.options.getGamma().setValue(brightness.getValue().doubleValue());
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdate > updateDelay.getValue().longValue()) {
            lastUpdate = currentTime;
            scanForOres();
            needsRebuild = true;
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (isNull() || oreBlocks.isEmpty() || !outlineMode.isEnabled())
            return;

        MatrixStack matrices = event.getMatrices();

        if (needsRebuild) {
            rebuildVBO();
            needsRebuild = false;
        }

        if (vertexBuffer == null || indexCount == 0) {
            return;
        }

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos().negate();

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        GpuTextureView colorTextureView = mc.getFramebuffer().getColorAttachmentView();
        GpuTextureView depthTextureView = mc.getFramebuffer().getDepthAttachmentView();

        matrix4fStack.pushMatrix();
        matrix4fStack.mul(matrices.peek().getPositionMatrix());
        matrix4fStack.translate((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);

        com.mojang.blaze3d.buffers.GpuBufferSlice[] gpubufferslice = RenderSystem.getDynamicUniforms()
                .writeTransforms(new TransformsValue(
                        new Matrix4f(matrix4fStack),
                        new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                        new Vector3f(),
                        new Matrix4f()));

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderSystem.setShaderFog(gpubufferslice[0]);

        GpuBuffer indexBuffer = RenderSystem
                .getSequentialBuffer(com.mojang.blaze3d.vertex.VertexFormat.DrawMode.LINES)
                .getIndexBuffer(indexCount);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "xray", colorTextureView, OptionalInt.empty(), depthTextureView,
                        OptionalDouble.empty())) {

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer,
                    RenderSystem.getSequentialBuffer(com.mojang.blaze3d.vertex.VertexFormat.DrawMode.LINES)
                            .getIndexType());
            renderPass.setUniform("DynamicTransforms", gpubufferslice[0]);
            renderPass.setPipeline(RenderPipelines.LINES);
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        matrix4fStack.popMatrix();
    }

    private void rebuildVBO() {
        clearVBO();

        if (oreBlocks.isEmpty()) {
            return;
        }

        MatrixStack identityStack = new MatrixStack();

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(RenderPipelines.LINES.getVertexFormatMode(), RenderPipelines.LINES.getVertexFormat());

        for (OreBlock ore : oreBlocks) {
            int x = ore.pos.getX();
            int y = ore.pos.getY();
            int z = ore.pos.getZ();

            int colorInt = (ore.color.getAlpha() << 24) | (ore.color.getRed() << 16) | (ore.color.getGreen() << 8)
                    | ore.color.getBlue();

            VertexRendering.drawOutline(identityStack, bufferBuilder, VoxelShapes.fullCube(), x, y, z, colorInt, 1f);
        }

        try (BuiltBuffer meshData = bufferBuilder.endNullable()) {
            if (meshData != null) {
                indexCount = meshData.getDrawParameters().indexCount();
                vertexBuffer = RenderSystem.getDevice()
                        .createBuffer(() -> "XRay vertex buffer", GpuBuffer.USAGE_VERTEX, meshData.getBuffer());
            }
        }
    }

    private void scanForOres() {
        if (isNull())
            return;

        oreBlocks.clear();
        fakeChunks.clear();

        int rangeValue = range.getValue().intValue();
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = -rangeValue; x <= rangeValue; x++) {
            for (int y = -rangeValue; y <= rangeValue; y++) {
                for (int z = -rangeValue; z <= rangeValue; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (shouldShowBlock(block)) {
                        if (antiFakeOre.isEnabled() && isFakeOre(pos, block)) {
                            continue;
                        }

                        Color color = oreColors.getOrDefault(block, Color.WHITE);
                        oreBlocks.add(new OreBlock(pos, block, color));
                    }
                }
            }
        }
    }

    private boolean shouldShowBlock(Block block) {
        if (block == Blocks.DIAMOND_ORE && diamond.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_DIAMOND_ORE && diamond.isEnabled())
            return true;
        if (block == Blocks.EMERALD_ORE && emerald.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_EMERALD_ORE && emerald.isEnabled())
            return true;
        if (block == Blocks.GOLD_ORE && gold.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_GOLD_ORE && gold.isEnabled())
            return true;
        if (block == Blocks.NETHER_GOLD_ORE && gold.isEnabled())
            return true;
        if (block == Blocks.IRON_ORE && iron.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_IRON_ORE && iron.isEnabled())
            return true;
        if (block == Blocks.COAL_ORE && coal.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_COAL_ORE && coal.isEnabled())
            return true;
        if (block == Blocks.COPPER_ORE && copper.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_COPPER_ORE && copper.isEnabled())
            return true;
        if (block == Blocks.REDSTONE_ORE && redstone.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_REDSTONE_ORE && redstone.isEnabled())
            return true;
        if (block == Blocks.LAPIS_ORE && lapis.isEnabled())
            return true;
        if (block == Blocks.DEEPSLATE_LAPIS_ORE && lapis.isEnabled())
            return true;
        if (block == Blocks.SPAWNER && spawner.isEnabled())
            return true;
        if ((block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST)
                && chest.isEnabled())
            return true;
        if (block == Blocks.NETHER_QUARTZ_ORE && netherQuartz.isEnabled())
            return true;
        if (block == Blocks.ANCIENT_DEBRIS && ancientDebris.isEnabled())
            return true;

        return false;
    }

    private boolean isFakeOre(BlockPos pos, Block oreBlock) {
        boolean exposedToAir = false;
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = pos.offset(dir);
            Block adjacentBlock = mc.world.getBlockState(adjacent).getBlock();

            if (adjacentBlock == Blocks.AIR) {
                exposedToAir = true;
                break;
            }
        }

        if (!exposedToAir) {
            return true;
        }

        return false;
    }

    private void initializeColors() {
        oreColors.clear();
        oreColors.put(Blocks.DIAMOND_ORE, new Color(0, 255, 255));
        oreColors.put(Blocks.DEEPSLATE_DIAMOND_ORE, new Color(0, 255, 255));
        oreColors.put(Blocks.EMERALD_ORE, new Color(0, 255, 0));
        oreColors.put(Blocks.DEEPSLATE_EMERALD_ORE, new Color(0, 255, 0));
        oreColors.put(Blocks.GOLD_ORE, new Color(255, 215, 0));
        oreColors.put(Blocks.DEEPSLATE_GOLD_ORE, new Color(255, 215, 0));
        oreColors.put(Blocks.NETHER_GOLD_ORE, new Color(255, 215, 0));
        oreColors.put(Blocks.IRON_ORE, new Color(255, 255, 255));
        oreColors.put(Blocks.DEEPSLATE_IRON_ORE, new Color(255, 255, 255));
        oreColors.put(Blocks.COAL_ORE, new Color(64, 64, 64));
        oreColors.put(Blocks.DEEPSLATE_COAL_ORE, new Color(64, 64, 64));
        oreColors.put(Blocks.COPPER_ORE, new Color(255, 140, 0));
        oreColors.put(Blocks.DEEPSLATE_COPPER_ORE, new Color(255, 140, 0));
        oreColors.put(Blocks.REDSTONE_ORE, new Color(255, 0, 0));
        oreColors.put(Blocks.DEEPSLATE_REDSTONE_ORE, new Color(255, 0, 0));
        oreColors.put(Blocks.LAPIS_ORE, new Color(0, 0, 255));
        oreColors.put(Blocks.DEEPSLATE_LAPIS_ORE, new Color(0, 0, 255));
        oreColors.put(Blocks.SPAWNER, new Color(255, 0, 255));
        oreColors.put(Blocks.CHEST, new Color(139, 69, 19));
        oreColors.put(Blocks.TRAPPED_CHEST, new Color(255, 140, 0));
        oreColors.put(Blocks.ENDER_CHEST, new Color(128, 0, 128));
        oreColors.put(Blocks.NETHER_QUARTZ_ORE, new Color(255, 255, 255));
        oreColors.put(Blocks.ANCIENT_DEBRIS, new Color(139, 69, 19));
    }

    public List<OreBlock> getOreBlocks() {
        return oreBlocks;
    }
}
