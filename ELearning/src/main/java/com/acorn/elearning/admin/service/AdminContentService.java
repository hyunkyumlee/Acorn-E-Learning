package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import com.acorn.elearning.admin.dto.response.AdminLessonManageRowResponse;
import com.acorn.elearning.admin.dto.response.AdminProblemManageRowResponse;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.LessonForm;
import com.acorn.elearning.admin.form.ProblemForm;
import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.mapper.AdminLessonMapper;
import com.acorn.elearning.admin.mapper.AdminProblemMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final SubjectMapper sm;
    private final CurriculumNodeMapper cm;
    private final LessonMapper lm;
    private final PracticeProblemMapper ppm;
    private final AdminLessonMapper alm;
    private final AdminProblemMapper apm;

    private final AdminLogService adminLogService;

    private AdminOperationLog operationLog(Long adminId, String actionType, String targetType,Long targetId){

        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(adminId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());

        return log;
    }

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

    public int createSubject(SubjectForm form, Long adminId){

        Subject s = new Subject();
        s.setSubjectName(form.getSubjectName());
        s.setIsActive(form.getIsActive());
        s.setDescription(form.getDescription());

        s.setSubjectCode(form.getSubjectName());
        s.setSortOrder(sm.findAll().size() + 1);

        int inserted = sm.insert(s);

        if(inserted == 1){
            adminLogService.insert(
                    operationLog(adminId, "SUBJECT_CREATE", "SUBJECT", s.getSubjectId())
            );
        }

        return inserted;
    }

    public int updateSubject(SubjectForm form, Long adminId){
        Subject s = sm.findById(form.getSubjectId())
                        .orElseThrow();

        s.setSubjectName(form.getSubjectName());
        s.setIsActive(form.getIsActive());
        s.setDescription(form.getDescription());

        int updated = sm.update(s);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "SUBJECT_UPDATE", "SUBJECT", s.getSubjectId())
            );
        }
        return updated;
    }

    //커리큘럼 조회
    public List<CurriculumNode> findAllCurriculumNode(){
        return cm.findAll();
    }

    //커리큘럼 단건 조회
    public Optional<CurriculumNode> findByCurriculumId(Long id){
        return cm.findById(id);
    }

    public int createCurriculumNode(CurriculumNodeForm form, Long adminId){

        CurriculumNode c = new CurriculumNode();



        c.setSubjectId(form.getSubjectId());
        c.setLevelCode(form.getLevelCode());
        c.setNodeType(form.getNodeType());

        c.setTitle(form.getTitle());
        c.setSortOrder(form.getSortOrder() == null ? 0 : form.getSortOrder());
        c.setIsActive(form.getIsActive() == null ? Boolean.TRUE : form.getIsActive());
        c.setDescription(form.getDescription());

        int created = cm.insert(c);

        if(created == 1){
            adminLogService.insert(
                    operationLog(adminId, "CURRICULUM_NODE_CREATE", "CURRICULUM_NODE", c.getNodeId())
            );
        }
        return created;
    }

    public int updateCurriculumNode(CurriculumNodeForm form, Long adminId){
        CurriculumNode c = cm.findById(form.getNodeId())
                .orElseThrow();

        c.setSubjectId(form.getSubjectId());
        c.setLevelCode(form.getLevelCode());
        c.setNodeType(form.getNodeType() == null || form.getNodeType().isBlank()
                        ? c.getNodeType() : form.getNodeType()  );

        c.setTitle(form.getTitle());
        c.setSortOrder(form.getSortOrder() == null ? 0 : form.getSortOrder());
        c.setIsActive(form.getIsActive() == null ? Boolean.TRUE : form.getIsActive());
        c.setDescription(form.getDescription());

        int updated = cm.update(c);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "CURRICULUM_NODE_UPDATE", "CURRICULUM_NODE", c.getNodeId())
            );
        }
        return updated;
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

    public int createLesson(LessonForm form, Long adminId){
        Lesson lesson = new Lesson();
        lesson.setNodeId(form.getNodeId());
        lesson.setTitle(form.getTitle());
        lesson.setContent(form.getContent());
        lesson.setSortOrder(form.getSortOrder() == null ? 0 : form.getSortOrder());
        lesson.setIsActive(form.getIsActive() == null ? Boolean.TRUE : form.getIsActive());

        int inserted = lm.insert(lesson);

        if(inserted == 1){
            adminLogService.insert(
                    operationLog(adminId, "LESSON_CREATE", "LESSON", lesson.getLessonId())
            );
        }


        return inserted;
    }

    public int updateLesson(LessonForm form, Long adminId){
        Lesson lesson = lm.findById(form.getLessonId())
                .orElseThrow();

        lesson.setNodeId(form.getNodeId());
        lesson.setTitle(form.getTitle());
        lesson.setContent(form.getContent());
        lesson.setSortOrder(form.getSortOrder() == null ? lesson.getSortOrder() : form.getSortOrder());
        lesson.setIsActive(form.getIsActive() == null ? lesson.getIsActive() : form.getIsActive());

        int updated = lm.update(lesson);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "LESSON_UPDATE", "LESSON", lesson.getLessonId())
            );
        }
        return updated;
    }

    @Transactional
    public int deleteLesson(Long lessonId, Long adminId) {
        alm.deleteBookmarksByLessonId(lessonId);

        int deleted = alm.deleteById(lessonId);

        if(deleted == 1){
            adminLogService.insert(
                    operationLog(adminId, "LESSON_DELETE", "LESSON", lessonId)
            );
        }
        return deleted;
    }


    //관리자 문제 목록 조회
    public List<AdminProblemManageRowResponse> findAllAdminProblem(){
        return apm.findAll();
    }

    public int deleteProblem(Long problemId, Long adminId) {

        int deleted = apm.deleteById(problemId);

        if(deleted == 1){
            adminLogService.insert(
                    operationLog(adminId, "PROBLEM_DELETE", "PROBLEM", problemId)
            );
        }
        
        return deleted;
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

    public int createProblem(ProblemForm form, Long adminId){
        PracticeProblem problem = new PracticeProblem();
        problem.setSubjectId(form.getSubjectId());
        problem.setNodeId(form.getNodeId());
        problem.setProblemType(toProblemTypeCode(form.getProblemType()));
        problem.setQuestion(form.getQuestion());
        problem.setAnswerText(form.getAnswerText());

        /*
            practice PracticeProblem에 explanation 필드 추가 후 연결
            problem.setExplanation(form.getExplanation());
        */
        problem.setDifficultyCode(form.getDifficultyCode());
        problem.setIsActive(form.getIsActive() == null ? Boolean.TRUE : form.getIsActive());

        int inserted = ppm.insert(problem);

        if(inserted == 1){
            adminLogService.insert(
                    operationLog(adminId, "PROBLEM_CREATE", "PROBLEM", problem.getProblemId())
            );
        }

        return inserted;
    }

    public int updateProblem(ProblemForm form, Long adminId){
        PracticeProblem problem = ppm.findById(form.getProblemId())
                .orElseThrow();

        problem.setSubjectId(form.getSubjectId());
        problem.setNodeId(form.getNodeId());
        problem.setProblemType(toProblemTypeCode(form.getProblemType()));
        problem.setQuestion(form.getQuestion());
        problem.setAnswerText(form.getAnswerText());
        problem.setDifficultyCode(form.getDifficultyCode());
        problem.setIsActive(form.getIsActive() == null ? problem.getIsActive() : form.getIsActive());

        int updated = ppm.update(problem);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "PROBLEM_UPDATE", "PROBLEM", problem.getProblemId())
            );
        }
        return updated;
    }

    private String toProblemTypeCode(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "객관식" -> "MULTIPLE_CHOICE";
            case "빈칸" -> "FILL_BLANK";
            case "코드 결과 예측" -> "CODE_OUTPUT";
            case "간단 코드 입력", "SHORT_CODE" -> "CODE_SHORT";
            default -> value;
        };
    }

}

