package com.acorn.elearning.content.controller;

import com.acorn.elearning.content.service.ContentRecommendationService;
import com.acorn.elearning.content.view.ContentInstallResource;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContentController {
    private final ContentRecommendationService contentRecommendationService;

    public ContentController(ContentRecommendationService contentRecommendationService) {
        this.contentRecommendationService = contentRecommendationService;
    }

    @GetMapping({"/content/recommendations", "/community/recommendations"})
    public String recommendations(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false, name = "slot") String slot,
            Model model
    ) {
        Long activeSubjectId = subjectId == null ? 1L : subjectId;
        model.addAttribute("screen", "content/recommendations");
        model.addAttribute("activeSubjectId", activeSubjectId);
        model.addAttribute("subjectLabel", subjectLabel(activeSubjectId));
        model.addAttribute("videoView", contentRecommendationService.recommendations(activeSubjectId, "VIDEO", slot));
        model.addAttribute("installView", installResource(activeSubjectId));
        return "content/recommendations";
    }

    private ContentInstallResource installResource(Long subjectId) {
        return switch (subjectId == null ? 1 : subjectId.intValue()) {
            case 2 -> new ContentInstallResource(
                    "https://www.youtube.com/watch?v=kqtD5dpn9C8",
                    List.of(link("Windows 사용자용 설치 버전 다운로드", "https://www.python.org/downloads/windows/")),
                    List.of(link("Mac 사용자용 다운로드", "https://www.python.org/downloads/macos/")),
                    List.of("Windows 설치 파일을 내려받습니다.", "PATH 추가 옵션을 확인합니다.", "PowerShell을 다시 열어 확인합니다."),
                    List.of("Mac 설치 파일을 내려받습니다.", "설치 안내에 따라 진행합니다.", "터미널을 다시 열어 확인합니다."),
                    List.of(
                            "Python 공식 다운로드 페이지에서 Windows installer 링크를 선택합니다.",
                            "다운로드한 설치 파일을 실행합니다.",
                            "첫 화면에서 Add python.exe to PATH 옵션을 체크합니다.",
                            "Install Now를 눌러 설치를 진행합니다.",
                            "설치 완료 화면이 나오면 Close를 누릅니다.",
                            "명령 프롬프트나 PowerShell을 새로 엽니다.",
                            "python --version을 입력해 버전이 출력되는지 확인합니다."
                    ),
                    List.of(
                            "Python 공식 다운로드 페이지에서 macOS installer 링크를 선택합니다.",
                            "다운로드한 pkg 파일을 실행합니다.",
                            "설치 안내 화면에서 계속을 눌러 설치를 진행합니다.",
                            "설치가 끝나면 터미널을 새로 엽니다.",
                            "python3 --version을 입력해 버전이 출력되는지 확인합니다.",
                            "python 명령어가 필요하면 사용하는 IDE의 인터프리터 설정도 확인합니다."
                    ),
                    "python --version",
                    "설치 후 Windows는 명령 프롬프트나 PowerShell, Mac은 터미널에서 설치 확인 명령어를 입력해 주세요."
            );
            case 3 -> new ContentInstallResource(
                    "https://www.youtube.com/watch?v=pQN-pnXPaVg",
                    List.of(
                            link("Node.js Windows 사용자용 설치 버전 다운로드", "https://nodejs.org/en/download"),
                            link("VS Code Windows 사용자용 설치 버전 다운로드", "https://code.visualstudio.com/sha/download?build=stable&os=win32-x64-user")
                    ),
                    List.of(
                            link("Node.js Mac 사용자용 다운로드", "https://nodejs.org/en/download"),
                            link("VS Code Mac 사용자용 다운로드", "https://code.visualstudio.com/sha/download?build=stable&os=darwin-universal")
                    ),
                    List.of("Node.js와 VS Code를 내려받습니다.", "Node.js를 먼저 설치합니다.", "PowerShell에서 버전을 확인합니다."),
                    List.of("Node.js와 VS Code를 내려받습니다.", "Node.js를 먼저 설치합니다.", "터미널에서 버전을 확인합니다."),
                    List.of(
                            "Node.js 공식 페이지에서 Windows installer를 내려받습니다.",
                            "설치 파일을 실행하고 기본 옵션으로 Node.js를 설치합니다.",
                            "설치 완료 후 명령 프롬프트나 PowerShell을 새로 엽니다.",
                            "node -v와 npm -v를 입력해 버전을 확인합니다.",
                            "VS Code Windows 설치 파일을 내려받습니다.",
                            "VS Code를 설치한 뒤 작업 폴더를 열어 HTML/CSS/JS 파일을 작성합니다.",
                            "필요하면 VS Code의 확장 프로그램에서 Live Server를 설치합니다."
                    ),
                    List.of(
                            "Node.js 공식 페이지에서 macOS installer를 내려받습니다.",
                            "pkg 파일을 실행하고 안내에 따라 Node.js를 설치합니다.",
                            "설치 완료 후 터미널을 새로 엽니다.",
                            "node -v와 npm -v를 입력해 버전을 확인합니다.",
                            "VS Code macOS 파일을 내려받아 Applications 폴더로 옮깁니다.",
                            "VS Code를 실행하고 작업 폴더를 열어 실습을 시작합니다.",
                            "필요하면 VS Code의 확장 프로그램에서 Live Server를 설치합니다."
                    ),
                    "node -v / npm -v",
                    "설치 후 Windows는 명령 프롬프트나 PowerShell, Mac은 터미널에서 설치 확인 명령어를 입력해 주세요."
            );
            case 4 -> new ContentInstallResource(
                    "https://www.youtube.com/watch?v=27axs9dO7AE",
                    List.of(link("MySQL Windows 사용자용 설치 버전 다운로드", "https://dev.mysql.com/downloads/installer/")),
                    List.of(link("MySQL Mac 사용자용 다운로드", "https://dev.mysql.com/downloads/mysql/")),
                    List.of("MySQL Installer를 내려받습니다.", "Server와 Workbench를 설치합니다.", "PowerShell에서 실행 여부를 확인합니다."),
                    List.of("MySQL macOS 파일을 내려받습니다.", "MySQL Server를 설치합니다.", "터미널에서 실행 여부를 확인합니다."),
                    List.of(
                            "MySQL 공식 다운로드 페이지에서 MySQL Installer for Windows를 내려받습니다.",
                            "설치 파일을 실행하고 Developer Default 또는 필요한 항목을 선택합니다.",
                            "MySQL Server와 MySQL Workbench가 포함되어 있는지 확인합니다.",
                            "설치 중 root 비밀번호를 설정하고 따로 기록해 둡니다.",
                            "설치가 끝나면 MySQL Workbench를 실행해 접속을 확인합니다.",
                            "PowerShell을 새로 열고 mysql --version을 입력합니다.",
                            "명령어가 인식되지 않으면 MySQL bin 경로가 PATH에 잡혔는지 확인합니다."
                    ),
                    List.of(
                            "MySQL 공식 다운로드 페이지에서 macOS DMG Archive를 내려받습니다.",
                            "DMG 파일을 실행하고 MySQL Server 설치 안내를 완료합니다.",
                            "설치 중 root 비밀번호를 설정하고 따로 기록해 둡니다.",
                            "시스템 설정에서 MySQL 서비스가 실행 중인지 확인합니다.",
                            "필요하면 MySQL Workbench도 별도로 내려받아 설치합니다.",
                            "터미널을 새로 열고 mysql --version을 입력합니다.",
                            "명령어가 인식되지 않으면 MySQL 실행 경로를 확인합니다."
                    ),
                    "mysql --version",
                    "설치 후 Windows는 명령 프롬프트나 PowerShell, Mac은 터미널에서 설치 확인 명령어를 입력해 주세요."
            );
            default -> new ContentInstallResource(
                    "https://www.youtube.com/watch?v=xk4_1vDrzzo",
                    List.of(link("JDK Windows 사용자용 설치 버전 다운로드", "https://adoptium.net/temurin/releases/?os=windows&package=jdk")),
                    List.of(link("JDK Mac 사용자용 다운로드", "https://adoptium.net/temurin/releases/?os=mac&package=jdk")),
                    List.of("JDK Windows 설치 파일을 내려받습니다.", "설치 안내에 따라 진행합니다.", "PowerShell에서 버전을 확인합니다."),
                    List.of("JDK Mac 설치 파일을 내려받습니다.", "설치 안내에 따라 진행합니다.", "터미널에서 버전을 확인합니다."),
                    List.of(
                            "Adoptium 다운로드 페이지에서 Windows용 JDK 설치 파일을 선택합니다.",
                            "다운로드한 설치 파일을 실행합니다.",
                            "설치 안내에 따라 기본 옵션으로 JDK를 설치합니다.",
                            "설치가 끝나면 명령 프롬프트나 PowerShell을 새로 엽니다.",
                            "java -version을 입력해 JDK 버전이 출력되는지 확인합니다.",
                            "javac -version도 입력해 컴파일러가 함께 설치되었는지 확인합니다.",
                            "IDE를 사용한다면 프로젝트 SDK가 방금 설치한 JDK로 잡혔는지 확인합니다."
                    ),
                    List.of(
                            "Adoptium 다운로드 페이지에서 Mac용 JDK 설치 파일을 선택합니다.",
                            "다운로드한 pkg 파일을 실행합니다.",
                            "설치 안내에 따라 기본 옵션으로 JDK를 설치합니다.",
                            "설치가 끝나면 터미널을 새로 엽니다.",
                            "java -version을 입력해 JDK 버전이 출력되는지 확인합니다.",
                            "javac -version도 입력해 컴파일러가 함께 설치되었는지 확인합니다.",
                            "IDE를 사용한다면 프로젝트 SDK가 방금 설치한 JDK로 잡혔는지 확인합니다."
                    ),
                    "java -version",
                    "설치 후 Windows는 명령 프롬프트나 PowerShell, Mac은 터미널에서 설치 확인 명령어를 입력해 주세요."
            );
        };
    }

    private ContentInstallResource.DownloadLink link(String label, String url) {
        return new ContentInstallResource.DownloadLink(label, url);
    }

    private String subjectLabel(Long subjectId) {
        if (subjectId == null) {
            return "JAVA";
        }
        return switch (subjectId.intValue()) {
            case 2 -> "Python";
            case 3 -> "HTML/CSS/JS";
            case 4 -> "SQL";
            default -> "JAVA";
        };
    }
}
