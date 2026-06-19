package com.acorn.elearning.community.mapper;

import com.acorn.elearning.community.model.Report;
import java.util.List;
import java.util.Optional;

public interface ReportMapper {
    Optional<Report> findById(Long id);
    List<Report> findAll();
    int insert(Report model);
    int update(Report model);
}
