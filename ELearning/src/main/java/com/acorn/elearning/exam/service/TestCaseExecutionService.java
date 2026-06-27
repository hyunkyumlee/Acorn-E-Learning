package com.acorn.elearning.exam.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TestCaseExecutionService {
    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "judge", "testcase-execution", "status", "SKELETON");
    }
}
