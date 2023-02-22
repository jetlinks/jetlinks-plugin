package org.jetlinks.plugin.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class VersionRange {
    private final Version from;
    private final Version to;

    public boolean isInRange(Version version) {
        return version.onOrAfter(from) && version.onOrBefore(to);
    }
}
