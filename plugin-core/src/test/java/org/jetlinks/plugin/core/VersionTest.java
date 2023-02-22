package org.jetlinks.plugin.core;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @org.junit.jupiter.api.Test
    void test() {

        assertTrue(Version.platform_2_1.after(Version.platform_2_0));
        assertTrue(Version.platform_2_0.before(Version.platform_2_1));

    }
}