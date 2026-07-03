package com.acorn.elearning.admin.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChartPointResponse {


    private String label;
    private long value;
    private int percent;
}
