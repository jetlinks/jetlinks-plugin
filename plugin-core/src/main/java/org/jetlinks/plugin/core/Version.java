package org.jetlinks.plugin.core;

import lombok.Getter;

@Getter
public class Version implements Comparable<Version> {
    public static Version platform_2_0 = new Version(2, 0, 0, false);
    public static Version platform_2_1 = new Version(2, 1, 0, false);
    public static Version platform_2_2 = new Version(2, 2, 0, false);

    public static Version platform_latest = new Version(255, 255, 255, false);

    public final int id;

    private final byte major;

    private final byte minor;

    private final byte revision;

    private final boolean snapshot;

    public Version(int major, int minor, int revision, boolean snapshot) {

        this.id = major * 10000 + minor * 100 + revision;
        this.major = (byte) major;
        this.minor = (byte) minor;
        this.revision = (byte) revision;
        this.snapshot = snapshot;
    }

    public boolean after(Version version) {
        return version.id < id;
    }

    public boolean onOrAfter(Version version) {
        return version.id <= id;
    }

    public boolean before(Version version) {
        return version.id > id;
    }

    public boolean onOrBefore(Version version) {
        return version.id >= id;
    }


    @Override
    public String toString() {
        return major + "." + minor + "." + revision + (snapshot ? "-SNAPSHOT" : "");
    }

    @Override
    public int compareTo(Version other) {
        return Integer.compare(this.id, other.id);
    }

}
