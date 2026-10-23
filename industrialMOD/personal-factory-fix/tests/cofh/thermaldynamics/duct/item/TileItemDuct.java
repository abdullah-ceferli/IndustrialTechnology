package cofh.thermaldynamics.duct.item;
/** Test fixture: never packaged into the mod. */
public class TileItemDuct extends BaseTile {
    public TileItemDuct(Object world) { worldObj=world; }
    public Object getTestWorld() { return worldObj; }
}
class BaseTile {
    public int xCoord=1,yCoord=2,zCoord=3;
    protected Object worldObj;
}
