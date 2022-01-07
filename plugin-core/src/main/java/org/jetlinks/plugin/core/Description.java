package org.jetlinks.plugin.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Description implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String description;

    private Map<String, Object> others;
}
