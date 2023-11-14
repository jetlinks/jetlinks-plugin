package org.jetlinks.plugin.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PluginDataMapping implements Externalizable {
    private String inPluginId;

    private String inPlatformId;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
         out.writeUTF(inPluginId);
         out.writeUTF(inPlatformId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        inPluginId = in.readUTF();
        inPlatformId = in.readUTF();
    }
}
