package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import com.acorn.elearning.admin.dto.response.*;
import com.acorn.elearning.admin.form.CurriculumNodeForm;
import com.acorn.elearning.admin.form.LessonForm;
import com.acorn.elearning.admin.form.ProblemForm;
import com.acorn.elearning.admin.form.SubjectForm;
import com.acorn.elearning.admin.mapper.AdminContentMapper;
import com.acorn.elearning.admin.mapper.AdminCurriculumNodeMapper;
import com.acorn.elearning.admin.mapper.AdminLessonMapper;
import com.acorn.elearning.admin.mapper.AdminProblemMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
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
    private final AdminCurriculumNodeMapper acm;
    private final AdminContentMapper actm;

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


    //과목 비활성화
    private void deactivateSubjectContents(Long subjectId){
        actm.backupContentStatusesBySubjectId(subjectId);

        actm.deactivateLessonsBySubjectId(subjectId);
        actm.deactivateProblemsBySubjectId(subjectId);
        actm.deactivateCurriculumNodesBySubjectId(subjectId);
        actm.cancelActiveExamsBySubjectId(subjectId);
    }

    private void reactivateSubjectContents(Long subjectId) {
        actm.restoreCurriculumNodesBySubjectId(subjectId);
        actm.restoreLessonsBySubjectId(subjectId);
        actm.restoreProblemsBySubjectId(subjectId);
        actm.deleteContentStatusBackupsBySubjectId(subjectId);
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

    @Transactional
    public int updateSubject(SubjectForm form, Long adminId){
        Subject s = sm.findById(form.getSubjectId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "수정할 과목을 찾을 수 없습니다."
                ));

        boolean becomingInactive =
                Boolean.TRUE.equals(s.getIsActive())
                        && Boolean.FALSE.equals(form.getIsActive());

        if (becomingInactive) {
            deactivateSubjectContents(s.getSubjectId());
        }

        boolean becomingActive =
                Boolean.FALSE.equals(s.getIsActive())
                        && Boolean.TRUE.equals(form.getIsActive());

        if (becomingActive) {
            reactivateSubjectContents(s.getSubjectId());
        }

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
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "수정할 커리큘럼을 찾을 수 없습니다."
                ));

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

    public AdminPageResponse<CurriculumNode> findCurriculumPage(int page, int size, String keyword, Long subjectId, String levelCode){

        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<CurriculumNode> items = acm.findPage(pageSize, offset, keyword, subjectId, levelCode);
        long totalCount = acm.countAll(keyword, subjectId, levelCode);

        return new AdminPageResponse<>(items, currentPage, pageSize, totalCount);
    }

    //이론 자료 조회
    public List<Lesson> findAllLesson(){
        return lm.findAll();
    }

    //관리자 화면 이론 조회
    public List<AdminLessonManageRowResponse> findAllAdminLesson(){
        return alm.findAll();
    }

    public AdminPageResponse<AdminLessonManageRowResponse> findLessonPage(
            int page,
            int size,
            String keyword,
            String subjectName,
            String curriculumTitle,
            String levelCode,
            Boolean isActive
    ) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<AdminLessonManageRowResponse> items = alm.findPage(
                pageSize,
                offset,
                keyword,
                subjectName,
                curriculumTitle,
                levelCode,
                isActive
        );
        long totalCount = alm.countAll(keyword, subjectName, curriculumTitle, levelCode, isActive);

        return new AdminPageResponse<>(items, currentPage, pageSize, totalCount);
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
        lesson.setRequiredForCompletion(
                form.getRequiredForCompletion() == null ? Boolean.TRUE : form.getRequiredForCompletion()
        );
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
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "수정할 이론 자료를 찾을 수 없습니다."
                ));

        lesson.setNodeId(form.getNodeId());
        lesson.setTitle(form.getTitle());
        lesson.setContent(form.getContent());
        lesson.setRequiredForCompletion(
                form.getRequiredForCompletion() == null
                        ? lesson.getRequiredForCompletion()
                        : form.getRequiredForCompletion()
        );
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

        Lesson lesson = lm.findById(form.getLessonId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "문제에 연결할 이론 자료를 찾을 수 없습니다."
                ));

        CurriculumNode node = cm.findById(lesson.getNodeId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "문제에 연결할 커리큘럼을 찾을 수 없습니다."
                ));

        problem.setSubjectId(node.getSubjectId());
        problem.setNodeId(lesson.getNodeId());
        problem.setProblemType(toProblemTypeCode(form.getProblemType()));
        problem.setQuestion(form.getQuestion());
        problem.setAnswerText(form.getAnswerText());
        problem.setLessonId(lesson.getLessonId());
        problem.setExplanation(form.getExplanation());
        problem.setDifficultyCode(form.getDifficultyCode());
        problem.setCreatedBy(adminId);
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
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "수정할 문제를 찾을 수 없습니다."
                ));


        Lesson lesson = lm.findById(form.getLessonId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "문제에 연결할 이론 자료를 찾을 수 없습니다."
                ));

        CurriculumNode node = cm.findById(lesson.getNodeId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "문제에 연결할 커리큘럼을 찾을 수 없습니다."
                ));

        problem.setSubjectId(node.getSubjectId());
        problem.setNodeId(lesson.getNodeId());
        problem.setProblemType(toProblemTypeCode(form.getProblemType()));
        problem.setQuestion(form.getQuestion());
        problem.setAnswerText(form.getAnswerText());
        problem.setLessonId(lesson.getLessonId());
        problem.setExplanation(form.getExplanation());
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

    public int updateProblemStatus(Long problemId, Boolean isActive, Long adminId) {
        if (isActive == null) {
            return 0;
        }

        PracticeProblem problem = ppm.findById(problemId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "상태를 변경할 문제를 찾을 수 없습니다."
                ));

        problem.setIsActive(isActive);

        int updated = ppm.update(problem);

        if (updated == 1) {
            adminLogService.insert(
                    operationLog(adminId, "PROBLEM_STATUS_UPDATE", "PROBLEM", problemId)
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

    public AdminPageResponse<AdminProblemManageRowResponse> findProblemPage(
            int page,
            int size,
            String keyword,
            Long subjectId,
            Long nodeId,
            String problemType,
            String difficultyCode,
            Boolean isActive
    ) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<AdminProblemManageRowResponse> items =
                apm.findPage(pageSize, offset, keyword, subjectId, nodeId, problemType, difficultyCode, isActive);

        long totalCount =
                apm.countAll(keyword, subjectId, nodeId, problemType, difficultyCode, isActive);

        return new AdminPageResponse<>(items, currentPage, pageSize, totalCount);
    }


    //API용 메서드
    public List<SubjectManageResponse> findSubjectResponse(){
        return sm.findAll().stream()
                .map(subject -> new SubjectManageResponse(
                        subject.getSubjectId(),
                        subject.getSubjectName(),
                        subject.getDescription(),
                        subject.getIsActive(),
                        subject.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public int updateSubjectStatus(Long subjectId, Boolean isActive, Long adminId){
        Subject s = sm.findById(subjectId)
                .orElseThrow(() -> new BusinessException(
                ErrorCode.COMMON_NOT_FOUND,
                "상태를 변경할 과목을 찾을 수 없습니다."
        ));

        boolean becomingInactive =
                Boolean.TRUE.equals(s.getIsActive())
                        && Boolean.FALSE.equals(isActive);

        if (becomingInactive) {
            deactivateSubjectContents(subjectId);
        }

        boolean becomingActive =
                Boolean.FALSE.equals(s.getIsActive())
                        && Boolean.TRUE.equals(isActive);

        if (becomingActive) {
            reactivateSubjectContents(subjectId);
        }

        s.setIsActive(isActive);

        int updated = sm.update(s);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "SUBJECT_STATUS_UPDATE", "SUBJECT", subjectId)
            );
        }
        return updated;
    }

    public List<CurriculumNodeManageResponse> findCurriculumNodeResponse(){


        List<Subject> subjects = sm.findAll();

        Map<Long, String> subjectNameMap = subjects.stream()
                .collect(Collectors.toMap(Subject::getSubjectId, Subject::getSubjectName));

        return cm.findAll().stream()
                .map(node -> new CurriculumNodeManageResponse(
                        node.getNodeId(),
                        subjectNameMap.get(node.getSubjectId()),
                        node.getLevelCode(),
                        node.getNodeType(),
                        node.getTitle(),
                        node.getIsActive()

                        ))
                .toList();

    }

    public int updateCurriculumNodeStatus(Long nodeId, Boolean isActive, Long adminId){
        CurriculumNode c = cm.findById(nodeId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "상태를 변경할 커리큘럼을 찾을 수 없습니다."
                ));

        c.setIsActive(isActive);

        int updated = cm.update(c);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "CURRICULUM_NODE_STATUS_UPDATE", "CURRICULUM_NODE", nodeId)
            );
        }
        return updated;
    }

    public int updateLessonStatus(Long lessonId, Boolean isActive, Long adminId){

        if(isActive == null){
            return 0;
        }

        Lesson l = lm.findById(lessonId).orElseThrow(() -> new BusinessException(
                ErrorCode.COMMON_NOT_FOUND,
                "상태를 변경할 이론 자료를 찾을 수 없습니다."
        ));

        l.setIsActive(isActive);

        int updated = lm.update(l);

        if(updated == 1){
            adminLogService.insert(
                    operationLog(adminId, "LESSON_STATUS_UPDATE", "LESSON", lessonId)
            );
        }
        return updated;
    }





}

