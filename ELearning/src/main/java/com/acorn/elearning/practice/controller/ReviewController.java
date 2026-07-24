package com.acorn.elearning.practice.controller;

import com.acorn.elearning.practice.form.WrongAnswerRetryForm;
import com.acorn.elearning.practice.service.WrongAnswerService;
import com.acorn.elearning.practice.view.WrongAnswerDetailView;
import com.acorn.elearning.practice.view.WrongAnswerNote;
import com.acorn.elearning.practice.view.WrongAnswerPageView;
import com.acorn.elearning.practice.view.WrongAnswerSummaryView;
import com.acorn.elearning.security.SessionUser;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Lesson;

@Controller
public class ReviewController {

    private final WrongAnswerService wrongAnswerService;
    private final LessonMapper lessonMapper;
    private final CurriculumNodeMapper curriculumNodeMapper;

    public ReviewController(WrongAnswerService wrongAnswerService, LessonMapper lessonMapper, CurriculumNodeMapper curriculumNodeMapper) {
        this.wrongAnswerService = wrongAnswerService;
        this.lessonMapper = lessonMapper;
        this.curriculumNodeMapper = curriculumNodeMapper;
    }

    // 1. 오답 요약 페이지
    @GetMapping("/learning/review")
    public String summary(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model) {

        WrongAnswerSummaryView view = wrongAnswerService.summary(sessionUser);

        model.addAttribute("view", view);
        model.addAttribute("screen", "learning/review");
        return "learning/review";
    }

    //2. 오답목록 - 현재 lessonid 기준
    @GetMapping("/learning/review/list")
    public String list(@SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                       @RequestParam(name = "nodeId", required = false) Long nodeId,
                       @RequestParam(name = "lessonId", required = false) Long lessonId,
                       Model model) {

        WrongAnswerPageView view = wrongAnswerService.list(sessionUser, nodeId, lessonId);

        Lesson lesson = null;
        CurriculumNode node = null;

        if (lessonId != null) {
            lesson = lessonMapper.findById(lessonId).orElse(null);

            if (lesson != null && lesson.getNodeId() != null) {
                node = curriculumNodeMapper.findById(lesson.getNodeId()).orElse(null);
            }
        } else if (nodeId != null) {
            node = curriculumNodeMapper.findById(nodeId).orElse(null);
        }

        model.addAttribute("view", view);
        model.addAttribute("nodeId", nodeId);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("lesson", lesson);
        model.addAttribute("node", node);

        return "learning/review-list";
    }

    @GetMapping("/learning/review/{wrongAnswerId}")
    public String detail(@PathVariable Long wrongAnswerId,
                         @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                         Model model) {

        WrongAnswerDetailView view = wrongAnswerService.detail(sessionUser, wrongAnswerId);

        Long lessonId = null;
        Long nodeId = null;

        Object lessonIdValue = view.attributes().get("lessonId");
        if (lessonIdValue instanceof Long longValue) {
            lessonId = longValue;
        } else if (lessonIdValue instanceof Number numberValue) {
            lessonId = numberValue.longValue();
        }

        Object nodeIdValue = view.attributes().get("nodeId");
        if (nodeIdValue instanceof Long longValue) {
            nodeId = longValue;
        } else if (nodeIdValue instanceof Number numberValue) {
            nodeId = numberValue.longValue();
        }

        Lesson lesson = null;
        CurriculumNode node = null;

        if (lessonId != null) {
            lesson = lessonMapper.findById(lessonId).orElse(null);
        }

        if (nodeId != null) {
            node = curriculumNodeMapper.findById(nodeId).orElse(null);
        }

        model.addAttribute("view", view);
        model.addAttribute("lesson", lesson);
        model.addAttribute("node", node);
        model.addAttribute("screen", "learning/review");

        return "learning/review";
    }

    @GetMapping(value = "/learning/review/{wrongAnswerId}/note.md", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> downloadNote(
            @PathVariable Long wrongAnswerId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        WrongAnswerNote note = wrongAnswerService.note(sessionUser, wrongAnswerId);

        byte[] markdown = note.markdown().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(new MediaType("application", "octet-stream", StandardCharsets.UTF_8))
                .contentLength(markdown.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(note.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(markdown);
    }

    @PostMapping("/learning/review/{wrongAnswerId}/community-draft")
    public String createCommunityDraft(
            @PathVariable Long wrongAnswerId,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {
        WrongAnswerNote note = wrongAnswerService.note(sessionUser, wrongAnswerId);
        redirectAttributes.addFlashAttribute("wrongAnswerCommunityDraft", note);
        redirectAttributes.addFlashAttribute("message", "오답노트를 게시글 초안으로 옮겼습니다. 내용을 보완한 뒤 등록해 주세요.");
        return "redirect:/community/write";
    }

    @PostMapping("/learning/review/{wrongAnswerId}/retry")
    public String retry(@PathVariable Long wrongAnswerId,
                        @RequestParam(name = "lessonId", required = false) Long lessonId,
                        @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                        @Validated @ModelAttribute("form") WrongAnswerRetryForm form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // 검증 실패 시 다시 상세 페이지로 (모델 데이터 복구 필요 시 처리)
            return "learning/review";
        }
        /*
        wrongAnswerService.retry(sessionUser, form, wrongAnswerId);
        redirectAttributes.addFlashAttribute("message", "재시도 처리가 완료되었습니다.");
        boolean correct = wrongAnswerService.retry(sessionUser, form, wrongAnswerId);
        */

        boolean correct = wrongAnswerService.retry(sessionUser, form, wrongAnswerId);
        redirectAttributes.addFlashAttribute(
        "retryResultMessage",
        correct ? "대단해요! 스스로 부족한 점을 찾아 채워가는 모습이 정말 멋집니다!" : "다시 한번 도전해 볼까요?"
        );
        if (lessonId != null) {
            redirectAttributes.addFlashAttribute("lessonId", lessonId);
        }

        return "redirect:/learning/review/" + wrongAnswerId;
    }


    @PostMapping("/learning/review/{wrongAnswerId}/reviewed")
    public String markReviewed(@PathVariable Long wrongAnswerId,
                               @RequestParam(name = "lessonId", required = false) Long lessonId,
                               @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                               RedirectAttributes redirectAttributes) {

        wrongAnswerService.markReviewed(sessionUser, wrongAnswerId);
        redirectAttributes.addFlashAttribute("message", "검토 완료되었습니다.");

        if (lessonId != null) {
            return "redirect:/learning/review/list?lessonId=" + lessonId;
        }

        return "redirect:/learning/review";
    }

}
