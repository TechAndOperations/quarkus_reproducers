package org.acme.xml;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "Action")
@XmlEnum
public enum Action {

    @XmlEnumValue("create")
    CREATE("create"),
    @XmlEnumValue("cancel")
    CANCEL("cancel"),
    @XmlEnumValue("correct")
    CORRECT("correct"),
    @XmlEnumValue("status")
    STATUS("status");
    private final String value;

    Action(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Action fromValue(String v) {
        for (Action c: Action.values()) {
            if (c.value().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}