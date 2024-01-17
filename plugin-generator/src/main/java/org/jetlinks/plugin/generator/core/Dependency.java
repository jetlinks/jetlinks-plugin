package org.jetlinks.plugin.generator.core;

import lombok.Getter;
import lombok.Setter;
import org.joox.Match;

import static org.joox.JOOX.$;

@Getter
@Setter
public class Dependency extends Artifact {
    private String scope;
    private String classifier;
    private String type;
    private String systemPath;
    private boolean optional;

    public static Dependency of(String artifact) {
        Dependency dependency = new Dependency();
        dependency.withPattern(artifact);
        return dependency;
    }


    public Match toXml() {
        Match xml = super.toXml();

        if (scope != null) {
            xml = xml.append($("scope").text(scope));
        }
        if (classifier != null) {
            xml = xml.append($("classifier").text(classifier));
        }
        if (type != null) {
            xml = xml.append($("type").text(type));
        }
        if (systemPath != null) {
            xml = xml.append($("systemPath").text(systemPath));
        }
        if (optional) {
            xml = xml.append($("optional").text("true"));
        }

        return xml;
    }
}
