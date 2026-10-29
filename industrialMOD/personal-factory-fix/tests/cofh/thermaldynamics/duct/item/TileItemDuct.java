package cofh.thermaldynamics.duct.item;
/** Test fixture: never packaged into the mod. */
public class TileItemDuct extends BaseTile {
    public TileItemDuct(Object world) { worldObj=world; }
    public Object getTestWorld() { return worldObj; }
    public void setTestWorld(Object world) { worldObj=world; }
    public void setTestPosition(int x,int y,int z) { xCoord=x;yCoord=y;zCoord=z; }
    public int getTestX() { return xCoord; }
    public int getTestY() { return yCoord; }
    public int getTestZ() { return zCoord; }
    public int handledPackets;
}
class BaseTile {
    public int xCoord=1,yCoord=2,zCoord=3;
    protected Object worldObj;
}
