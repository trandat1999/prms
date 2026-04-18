package com.tranhuudat.prms.service.impl;

import com.tranhuudat.prms.dto.BaseResponse;
import com.tranhuudat.prms.dto.autocomplete.AutocompleteSearchRequest;
import com.tranhuudat.prms.dto.autocomplete.ProjectAutocompleteDto;
import com.tranhuudat.prms.dto.autocomplete.UserAutocompleteDto;
import com.tranhuudat.prms.repository.ProjectRepository;
import com.tranhuudat.prms.repository.UserRepository;
import com.tranhuudat.prms.service.AutoCompleteService;
import com.tranhuudat.prms.service.BaseService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoCompleteServiceImpl extends BaseService implements AutoCompleteService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public BaseResponse users(AutocompleteSearchRequest request) {
        Pageable pageable = getPageable(request);
        Page<UserAutocompleteDto> page = userRepository.autocompleteUsers(entityManager, request, pageable);
        return getResponse200(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponse projects(AutocompleteSearchRequest request) {
        Pageable pageable = getPageable(request);
        Page<ProjectAutocompleteDto> page = projectRepository.autocompleteProjects(entityManager, request, pageable);
        return getResponse200(page.getContent());
    }
}

