package cofh.thermaldynamics.duct.item;
/** Models the real class hierarchy and the trailing powered-state byte; never packaged. */
public class TileItemDuctEnder extends TileItemDuctPowered {
    public boolean powered;
    public TileItemDuctEnder(Object world) { super(world); }
}
abstract class TileItemDuctPowered extends TileItemDuct {
    TileItemDuctPowered(Object world) { super(world); }
}
