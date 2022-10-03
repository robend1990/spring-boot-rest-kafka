package com.faceit.assessment.user.dto;

import lombok.Data;

@Data
public class UserSearchRequest extends PagedSearch {

    private String first_name;
    private String last_name;
    private String country;
    private String email;
}
