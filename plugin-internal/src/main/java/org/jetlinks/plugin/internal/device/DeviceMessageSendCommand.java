package org.jetlinks.plugin.internal.device;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.jetlinks.core.message.DeviceMessage;
import org.jetlinks.core.message.MessageType;
import org.jetlinks.plugin.core.AbstractPluginCommand;
import reactor.core.publisher.Flux;

@AllArgsConstructor(staticName = "of")
public class DeviceMessageSendCommand extends AbstractPluginCommand<Flux<DeviceMessage>> {
    public static final String ID = "DeviceMessageSend";

    @Setter
    private DeviceMessage message;

    public DeviceMessageSendCommand() {

    }


    public DeviceMessage getMessage() {
        if (message != null) {
            return message;
        }
        return (DeviceMessage) MessageType
                .convertMessage(readable())
                .orElseThrow(() -> new UnsupportedOperationException("unsupported command parameters"));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void validate() {
        getMessage();
    }
}
