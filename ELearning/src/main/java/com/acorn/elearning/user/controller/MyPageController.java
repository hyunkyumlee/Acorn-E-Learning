package com.acorn.elearning.user.controller;

import com.acorn.elearning.config.UploadProperties;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.CommunityActivityPageResponse;
import com.acorn.elearning.user.dto.response.LearningStatusPageResponse;
import com.acorn.elearning.user.dto.response.MyPageSummaryResponse;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.form.ProfileForm;
import com.acorn.elearning.user.service.UserActivityService;
import com.acorn.elearning.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;

@Controller
public class MyPageController {
    private final UserActivityService userActivityService;
    private final UserService userService;
    private final UploadProperties uploadProperties;

    public MyPageController(
            UserActivityService userActivityService,
            UserService userService,
            UploadProperties uploadProperties
    ) {
        this.userActivityService = userActivityService;
        this.userService = userService;
        this.uploadProperties = uploadProperties;
    }

    @GetMapping("/mypage")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        MyPageSummaryResponse view = userActivityService.mypage(sessionUser);
        model.addAttribute("screen", "mypage/index");
        model.addAttribute("view", view);
        model.addAttribute("profileForm", profileForm(view));
        return "mypage/index";
    }

    @GetMapping("/mypage/learning")
    public String learningStatus(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subject", defaultValue = "ALL") String subject,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        LearningStatusPageResponse view = userActivityService.learningStatus(sessionUser, subject, page);
        model.addAttribute("screen", "mypage/learning");
        model.addAttribute("view", view);
        return "mypage/learning";
    }

    @GetMapping("/mypage/community/liked")
    public String likedPosts(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "category", defaultValue = "ALL") String category,
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        return communityActivity(sessionUser, category, query, page, model, "LIKED", "mypage/liked");
    }

    @GetMapping("/mypage/community/scraps")
    public String scrapedPosts(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "category", defaultValue = "ALL") String category,
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        return communityActivity(sessionUser, category, query, page, model, "SCRAPS", "mypage/scraps");
    }

    @GetMapping("/mypage/community/posts")
    public String writtenPosts(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "category", defaultValue = "ALL") String category,
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        return communityActivity(sessionUser, category, query, page, model, "POSTS", "mypage/posts");
    }

    @PostMapping("/mypage/profile")
    public String updateProfile(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("profileForm") ProfileForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("message", "프로필 정보를 확인해주세요.");
            return "redirect:/mypage";
        }

        UserProfileResponse updated = userService.updateProfile(sessionUser, form);
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(updated.userId(), updated.email(), updated.nickname(), updated.role(), sessionUser.premiumActive(), updated.profileImageUrl()));
        redirectAttributes.addFlashAttribute("message", "프로필이 저장되었습니다.");
        return "redirect:/mypage";
    }

    @GetMapping("/mypage/profile-images/{fileName:.+}")
    public ResponseEntity<Resource> profileImage(@PathVariable String fileName) throws MalformedURLException {
        Path directory = uploadBasePath().resolve("profile-images").toAbsolutePath().normalize();
        Path target = directory.resolve(fileName).normalize();
        if (!target.startsWith(directory)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new UrlResource(target.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(contentType(fileName))
                .body(resource);
    }

    private ProfileForm profileForm(MyPageSummaryResponse view) {
        ProfileForm form = new ProfileForm();
        form.setNickname(view.user().nickname());
        form.setLearningGoal(view.learning().learningGoal());
        form.setPrimarySubjectId(view.learning().primarySubjectId());
        return form;
    }

    private String communityActivity(
            SessionUser sessionUser,
            String category,
            String query,
            int page,
            Model model,
            String type,
            String viewName
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        CommunityActivityPageResponse view = userActivityService.communityActivity(sessionUser, type, category, query, page);
        model.addAttribute("screen", viewName);
        model.addAttribute("view", view);
        return viewName;
    }

    private Path uploadBasePath() {
        String configuredPath = uploadProperties == null ? null : uploadProperties.basePath();
        String basePath = configuredPath == null || configuredPath.isBlank() ? "./uploads" : configuredPath;
        return Paths.get(basePath).toAbsolutePath().normalize();
    }

    private MediaType contentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}
