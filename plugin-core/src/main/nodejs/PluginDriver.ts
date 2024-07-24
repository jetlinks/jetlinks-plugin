import {Monitor} from "@jetlinks/jetlinks-core/Monitor";
import {CommandSupport} from "@jetlinks/jetlinks-core/Commands";
import {Subscription} from "rxjs";

export interface Plugin extends CommandSupport {

    startup(): void;

    shutdown(): void;

}

export declare class PluginDriver {

    getType(): PluginType;

    getDescription(): PluginDescription;

    createPlugin(context: PluginContext): Plugin;

}


export interface PluginContext {

    monitor(): Monitor;

    services(): ServiceRegistry;

    environment(): PluginEnvironment;

    scheduler(): PluginScheduler;

    workDir(): string;

}

export interface ServiceRegistry {

    getService(serviceName: string): CommandSupport

}


export interface PluginScheduler {

    schedule(name: string, cron: string, task: () => void, singleton: boolean): Subscription;

}

export interface PluginEnvironment {

    getProperties(): Map<string, any>;

}

export class PluginDescription {
    id: string;
    name: string;
    version: string;
    platformVersion?: string[];
    others?: Map<string, any>;
}

export class PluginType {
    id: string;
    name: string
}

