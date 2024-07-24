import {Plugin, PluginContext, PluginDescription, PluginDriver, PluginType} from "../../PluginDriver";
import {SimpleCommandSupport} from "@jetlinks/jetlinks-core/Commands";

export class TestPluginDriver implements PluginDriver {

    getType(): PluginType {
        return {
            id: "test",
            name: "test"
        }
    }

    getDescription(): PluginDescription {
        return {
            id: "test",
            name: "test",
            version: "1.0.0"
        }
    }

    createPlugin(context: PluginContext): Plugin {
        return new TestPlugin(context);
    }

}

class TestPlugin extends SimpleCommandSupport implements Plugin {
    context: PluginContext;

    constructor(context: PluginContext) {
        super([]);
        this.context = context;
    }

    startup(): void {
        this.context
            .monitor()
            .logger()
            .debug("startup");
    }

    shutdown() {
        this.context
            .monitor()
            .logger()
            .debug("shutdown");
    }
}

export default  TestPluginDriver;

