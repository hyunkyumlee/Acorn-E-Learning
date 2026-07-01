package com.acorn.elearning.admin.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


import com.acorn.elearning.admin.dto.response.AdminLessonManageRowResponse;
import com.acorn.elearning.admin.dto.response.AdminProblemManageRowResponse;
import com.acorn.elearning.admin.mapper.AdminLessonMapper;
import com.acorn.elearning.admin.mapper.AdminProblemMapper;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Lesson;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final SubjectMapper sm;

    private final CurriculumNodeMapper cm;

    private final LessonMapper lm;

    private final PracticeProblemMapper ppm;
    private final AdminLessonMapper alm;
    private final AdminProblemMapper apm;

    //과목 목록 조회
    public List<Subject> findAllSubject(){

        return sm.findAll();
    }

    //과목 단건 조회
    public Optional<Subject> findBySubjectId(Long id){
        return sm.findById(id);
    }

    //과목 등록
    public int insert(Subject model) {
        return sm.insert(model);
    }

    //과목 수정 / 상태 변경
    public int update(Subject model) {
        return sm.update(model);
    }

    //커리큘럼 조회
    public List<CurriculumNode> findAllCurriculumNode(){
        return cm.findAll();
    }

    //커리큘럼 단건 조회
    public Optional<CurriculumNode> findByCurriculumId(Long id){
        return cm.findById(id);
    }

    //커리큘럼 등록
    public int insert(CurriculumNode model) {
        return cm.insert(model);
    }

    //커리큘럼 수정
    public int update(CurriculumNode model) {
        return cm.update(model);
    }

    //이론 자료 조회
    public List<Lesson> findAllLesson(){
        return lm.findAll();
    }

    //관리자 화면 이론 조회
    public List<AdminLessonManageRowResponse> findAllAdminLesson(){
        return alm.findAll();
    }
    //이론 자료 단건 조회
    public Optional<Lesson> findByLessonId(Long id){
        return lm.findById(id);
    }

    //이론 자료 등록
    public int insert(Lesson model) {
        return lm.insert(model);
    }

    //이론 자료 수정
    public int update(Lesson model) {
        return lm.update(model);
    }

    //관리자 문제 목록 조회
    public List<AdminProblemManageRowResponse> findAllAdminProblem(){
        return apm.findAll();
    }
    //문제 목록 조회
    public List<PracticeProblem> findAllProblems(){
        return ppm.findAll();
    }

    //문제 목록 단건 조회
    public Optional<PracticeProblem> findByProblemId(Long id){
        return ppm.findById(id);
    }

    //문제 목록 등록
    public int insert(PracticeProblem model) {
        return ppm.insert(model);
    }

    //문제 목록 수정
    public int update(PracticeProblem model) {
        return ppm.update(model);
    }




    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser admin = currentAdminSessionUser();
        // Object target = targetMapper.findById(targetId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // targetMapper.update(applyStatusOrForm(target, form));
        // adminOperationLogMapper.insert(AdminOperationLog.changed(admin.userId(), target));
        // return Map.of("result", "updated");
        return Map.of("action", action, "status", "SKELETON");
    }
}
