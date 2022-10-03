package com.faceit.assessment.user.dto;

import lombok.Data;

@Data
public class PagedSearch {

    private int page = 0;
    private int size = 20;
}
