package org.jetlinks.plugin.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Initial setup data exchanged before any plugin command is accepted. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class SetupMessage {
    private String version = ExternalPluginProtocol.VERSION;
    private String runtimeId;
    private String driverId;
    private long generation;
    private String sdkVersion;
    private String pluginVersion;
    private List<String> capabilities = Collections.emptyList();
    private String credential;
    private int maxFrameBytes = ExternalPluginProtocol.DEFAULT_MAX_FRAME_BYTES;

    public SetupMessage() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public long getGeneration() {
        return generation;
    }

    public void setGeneration(long generation) {
        this.generation = generation;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ArrayList<>(capabilities));
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public int getMaxFrameBytes() {
        return maxFrameBytes;
    }

    public void setMaxFrameBytes(int maxFrameBytes) {
        this.maxFrameBytes = maxFrameBytes;
    }
}
