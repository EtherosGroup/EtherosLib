package cn.etheros.etheroslib.common;

/**
 * EtherosLib 运行平台。
 */
public enum Platform {

    BUKKIT("Bukkit/Paper"),
    VELOCITY("Velocity"),
    UNKNOWN("Unknown");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
