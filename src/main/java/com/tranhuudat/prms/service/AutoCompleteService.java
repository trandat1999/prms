package com.tranhuudat.prms.service;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;

public interface AutoCompleteService {
    BaseResponse users(AutocompleteSearchRequest request);
    BaseResponse projects(AutocompleteSearchRequest request);
    BaseResponse kanbanProjects(AutocompleteSearchRequest request);

    /** Thành viên dự án đang hoạt động (cần {@link AutocompleteSearchRequest#getProjectId()}). */
    BaseResponse projectMembers(AutocompleteSearchRequest request);
}

