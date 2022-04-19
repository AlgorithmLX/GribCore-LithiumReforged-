package gribland.gribcore.lithium.common.shapes;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class CuboidVoxelSet extends DiscreteVoxelShape {
    private final int minX, minY, minZ, maxX, maxY, maxZ;

    protected CuboidVoxelSet(int xSize, int ySize, int zSize, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(xSize, ySize, zSize);

        this.minX = (int) Math.round(minX * xSize);
        this.maxX = (int) Math.round(maxX * xSize);
        this.minY = (int) Math.round(minY * ySize);
        this.maxY = (int) Math.round(maxY * ySize);
        this.minZ = (int) Math.round(minZ * zSize);
        this.maxZ = (int) Math.round(maxZ * zSize);
    }

    @Override
    public boolean isFull(int x, int y, int z) {
        return x >= this.minX && x < this.maxX &&
                y >= this.minY && y < this.maxY &&
                z >= this.minZ && z < this.maxZ;
    }

    @Override
    public void setFull(int x, int y, int z, boolean resize, boolean included) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastFull(Direction.Axis axis) {
        return axis.choose(this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public boolean isEmpty() {
        return this.minX >= this.maxX || this.minY >= this.maxY || this.minZ >= this.maxZ;
    }

    @Override
    public int firstFull(Direction.Axis axis) {
        return axis.choose(this.minX, this.minY, this.minZ);
    }

    @Override
    protected boolean isZStripFull(int minZ, int maxZ, int x, int y) {
        return x >= this.minX && x < this.maxX &&
                y >= this.minY && y < this.maxY &&
                minZ >= this.minZ && maxZ <= this.maxZ; // arg maxZ is exclusive
    }

    @Override
    protected void setZStrip(int minZ, int maxZ, int x, int y, boolean included) {
        throw new UnsupportedOperationException();
    }
}