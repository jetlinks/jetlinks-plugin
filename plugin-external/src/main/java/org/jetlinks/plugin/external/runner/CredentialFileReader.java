package org.jetlinks.plugin.external.runner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/** Reads a runner credential without putting the secret in process arguments or logs. */
final class CredentialFileReader {
    private static final long MAX_BYTES = 4096;

    private CredentialFileReader() {
    }

    static String read(Path file) throws IOException {
        Path path = file.toAbsolutePath().normalize();
        BasicFileAttributes before = attributes(path);
        if (!before.isRegularFile()) {
            throw new IOException("credential file is not a regular file");
        }
        if (before.size() > MAX_BYTES) {
            throw new IOException("credential file exceeds the maximum size");
        }
        verifyPermissions(path);
        byte[] content = Files.readAllBytes(path);
        BasicFileAttributes after = attributes(path);
        if (!sameFile(before, after) || before.size() != content.length) {
            throw new FileSystemException("credential file changed while it was being read");
        }
        String credential = new String(content, StandardCharsets.UTF_8);
        if (credential.endsWith("\n")) {
            credential = credential.substring(0, credential.length() - 1);
            if (credential.endsWith("\r")) {
                credential = credential.substring(0, credential.length() - 1);
            }
        }
        if (credential.isEmpty()) {
            throw new IOException("credential file is empty");
        }
        return credential;
    }

    private static BasicFileAttributes attributes(Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }

    private static boolean sameFile(BasicFileAttributes before, BasicFileAttributes after) {
        Object beforeKey = before.fileKey();
        Object afterKey = after.fileKey();
        return before.isRegularFile()
            && after.isRegularFile()
            && (beforeKey == null || beforeKey.equals(afterKey))
            && before.lastModifiedTime().equals(after.lastModifiedTime());
    }

    private static void verifyPermissions(Path path) throws IOException {
        try {
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path, LinkOption.NOFOLLOW_LINKS);
            Set<PosixFilePermission> forbidden = EnumSet.of(
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_WRITE,
                PosixFilePermission.OTHERS_EXECUTE);
            forbidden.retainAll(permissions);
            if (!forbidden.isEmpty()) {
                throw new IOException("credential file must not be readable by group or others");
            }
        } catch (UnsupportedOperationException ignored) {
            // DOS/Windows filesystems do not expose POSIX mode bits. The regular-file and
            // no-follow checks above still apply.
        }
    }
}
