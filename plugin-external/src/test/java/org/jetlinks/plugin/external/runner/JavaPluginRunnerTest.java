package org.jetlinks.plugin.external.runner;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaPluginRunnerTest {

    @Test
    void readsCredentialFromARestrictedFile() throws Exception {
        Path credential = Files.createTempFile("plugin-credential", ".txt");
        try {
            Files.writeString(credential, "token-value\n");
            restrictPermissionsIfSupported(credential);

            JavaPluginRunner.Arguments options = JavaPluginRunner.Arguments.parse(new String[]{
                "--plugin", "plugin.jar",
                "--credential-file", credential.toString()
            });

            assertEquals("token-value", options.credential());
        } finally {
            Files.deleteIfExists(credential);
        }
    }

    @Test
    void rejectsCredentialOnTheCommandLine() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> JavaPluginRunner.Arguments.parse(new String[]{
                "--plugin", "plugin.jar", "--credential", "secret"
            }));
        assertEquals("--credential is not supported; use --credential-file", error.getMessage());
    }

    @Test
    void rejectsCredentialsReadableByOthers() throws Exception {
        Path credential = Files.createTempFile("plugin-credential", ".txt");
        try {
            Files.writeString(credential, "token-value");
            try {
                Files.setPosixFilePermissions(credential, EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.GROUP_READ));
            } catch (UnsupportedOperationException ignored) {
                return;
            }
            assertThrows(IllegalArgumentException.class, () -> JavaPluginRunner.Arguments.parse(new String[]{
                "--plugin", "plugin.jar", "--credential-file", credential.toString()
            }));
        } finally {
            Files.deleteIfExists(credential);
        }
    }

    private static void restrictPermissionsIfSupported(Path credential) throws Exception {
        try {
            Files.setPosixFilePermissions(credential, EnumSet.of(PosixFilePermission.OWNER_READ));
        } catch (UnsupportedOperationException ignored) {
            // Windows ACLs do not expose POSIX permissions.
        }
    }
}
