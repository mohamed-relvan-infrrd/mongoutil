package ai.infrrd.utility.mongoutil.enums;

import lombok.Getter;

@Getter
public enum Delimiter {

    COMMA(","),
    COLON(":");

    private String value;

    Delimiter(String value) {
        this.value = value;
    }
}