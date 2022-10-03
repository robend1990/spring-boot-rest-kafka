package com.faceit.assessment.user.domain;

import com.faceit.assessment.user.dto.UserSearchRequest;
import org.springframework.data.domain.Page;

interface UserCustomRepository {

    Page<User> findByCriteria(UserSearchRequest userSearchRequest);
}
