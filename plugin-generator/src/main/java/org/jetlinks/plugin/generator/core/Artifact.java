package org.jetlinks.plugin.generator.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.joox.Match;
import org.springframework.util.StringUtils;

import static org.joox.JOOX.$;

@Getter
@Setter
@EqualsAndHashCode
public class Artifact {
    private String groupId;
    private String artifactId;
    private String version;

    public void withPattern(String pattern) {
        String[] split = pattern.split(":");
        if (split.length > 0) {
            this.groupId = split[0];
        }
        if (split.length > 1) {
            this.artifactId = split[1];
        }
        if (split.length > 2) {
            this.version = split[2];
        }
    }

    public Match toXml() {
        Match match = $("dependency")
            .append($("groupId").text(groupId))
            .append($("artifactId").text(artifactId));
        if (StringUtils.hasText(version)) {
            match = match.append($("version").text(version));
        }
        return match;
    }

    @Override
    public String toString() {
        return String.join(":", groupId, artifactId, version);
    }
}
