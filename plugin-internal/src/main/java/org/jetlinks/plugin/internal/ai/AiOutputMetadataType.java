package org.jetlinks.plugin.internal.ai;

import org.jetlinks.sdk.server.ai.AiOutput;

/**
 * @author gyl
 * @since 2.3
 */
public enum AiOutputMetadataType {
    /**
     * @see AiOutput#flat()
     */
    flat,
    /**
     * @see AiOutput#lightWeighFlat()
     */
    lightWeighFlat,
    /**
     * @see AiOutput#toLightWeighMap()
     */
    lightWeigh;
}
