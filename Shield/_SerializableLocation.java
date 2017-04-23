package Shield;

import PluginReference.MC_Location;
import java.io.Serializable;

public class _SerializableLocation
implements Serializable {
    public int dimension;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public _SerializableLocation(double x, double y, double z, int dimension, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public _SerializableLocation(MC_Location loc) {
        this.dimension = loc.dimension;
        this.x = loc.x;
        this.y = loc.y;
        this.z = loc.z;
        this.pitch = loc.pitch;
        this.yaw = loc.yaw;
    }

    public static String GetDimensionName(int dimension) {
        if (dimension == 0) {
            return "world";
        }
        if (dimension == -1) {
            return "world_nether";
        }
        if (dimension == 1) {
            return "world_the_end";
        }
        return "Dimension " + dimension;
    }

    public String toString() {
        return String.valueOf(_SerializableLocation.GetDimensionName(this.dimension)) + "(" + (int)this.x + "," + (int)this.y + "," + (int)this.z + ")";
    }
}

