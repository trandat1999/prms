package com.tranhuudat.prms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectMemberRoleEnum {
    PROJECT_MANAGER("PROJECT_MANAGER", "Project Manager"),
    TEAM_LEAD("TEAM_LEAD", "Team Lead"),
    BA("BA", "Business Analyst"),
    DEVELOPER("DEVELOPER", "Developer"),
    TESTER("TESTER", "Tester"),
    REVIEWER("REVIEWER", "Reviewer"),
    OBSERVER("OBSERVER", "Observer");

    private final String code;
    private final String name;
}
