package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskLogActionEnum {
    CREATE("CREATE", "Create"),
    UPDATE("UPDATE", "Update"),
    ASSIGN("ASSIGN", "Assign"),
    REASSIGN("REASSIGN", "Reassign"),
    STATUS_CHANGE("STATUS_CHANGE", "Status change");

    private final String code;
    private final String name;
}

