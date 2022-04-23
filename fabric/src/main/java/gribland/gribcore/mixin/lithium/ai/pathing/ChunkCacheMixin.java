package gribland.gribcore.mixin.lithium.ai.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The hottest part of path-finding is reading blocks out from the world. This patch makes a number of changes to
 * avoid slow paths in the game and to better inline code. In testing, it shows a small improvement in path-finding
 * code.
 */
@Mixin(PathNavigationRegion.class)
public class ChunkCacheMixin {
    private static final BlockState DEFAULT_BLOCK = Blocks.AIR.defaultBlockState();

    @Shadow
    @Final
    protected int centerX;
    @Shadow
    @Final
    protected int centerZ;
    @Shadow
    @Final
    protected LevelChunk[][] chunks;

    private LevelChunk[] chunksFlat;

    private int xLen, zLen;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Level world, BlockPos minPos, BlockPos maxPos, CallbackInfo ci) {
        this.xLen = 1 + (maxPos.getX() >> 4) - (minPos.getX() >> 4);
        this.zLen = 1 + (maxPos.getZ() >> 4) - (minPos.getZ() >> 4);

        this.chunksFlat = new LevelChunk[this.xLen * this.zLen];

        // Flatten the 2D chunk array into our 1D array
        for (int x = 0; x < this.xLen; x++) {
            System.arraycopy(this.chunks[x], 0, this.chunksFlat, x * this.zLen, this.zLen);
        }
    }

    /**
     * @author null
     */
    @Overwrite
    public BlockState getBlockState(BlockPos pos) {
        int y = pos.getY();

        if (!Level.isOutsideBuildHeight(pos.getY())) {
            int x = pos.getX();
            int z = pos.getZ();

            int chunkX = (x >> 4) - this.centerX;
            int chunkZ = (z >> 4) - this.centerZ;

            if (chunkX >= 0 && chunkX < this.xLen && chunkZ >= 0 && chunkZ < this.zLen) {
                LevelChunk chunk = this.chunksFlat[(chunkX * this.zLen) + chunkZ];

                // Avoid going through Chunk#getBlockState
                if (chunk != null) {
                    LevelChunkSection section = chunk.getSections()[y >> 4];

                    if (section != null) {
                        return section.getBlockState(x & 15, y & 15, z & 15);
                    }
                }
            }
        }

        return DEFAULT_BLOCK;
    }

    /**
     * @author null
     */
    @Overwrite
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }
}

