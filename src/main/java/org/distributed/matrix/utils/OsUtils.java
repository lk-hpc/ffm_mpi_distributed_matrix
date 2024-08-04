package org.distributed.matrix.utils;

public class OsUtils {
    public enum OSType {
        WINDOWS, MAC, LINUX, OTHER
    }

    public static OSType getOSType() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OSType.WINDOWS;
        } else if (osName.contains("mac")) {
            return OSType.MAC;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return OSType.LINUX;
        } else {
            return OSType.OTHER;
        }
    }

    public static String getExtension() {
        var osType = OsUtils.getOSType();
        return switch (osType) {
            case WINDOWS -> ".exe";
            case MAC -> ".dylib";
            case LINUX -> ".so";
            default -> throw new IllegalStateException("Unexpected value: " + osType);
        };
    }

    public static void main(String[] args) {
        OSType osType = getOSType();
        System.out.println("Operating System: " + osType);
    }
}
