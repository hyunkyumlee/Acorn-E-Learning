package com.acorn.elearning.auth.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserRow {
    //users JOIN SQL 결과를 담는 전용 model
    //email로 login할 때 JOIN 쿼리 결과를 MyBatis가 담을 그릇

    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private String status;
    private String profileImageUrl;
    private String passwordHash; //BCrypt matches() 에 필요 - Controller/Response에는 절대 노출 X

    /*

    SELECT u.user_id, u.email, u.nickname, u.role, u.status, u.profile_image_url, c.password_hash
    FROM users u
    INNER JOIN user_credentials c ON c.user_id = u.user_id
    WHERE u.email = #{email}

    의 결과가 LoginUserRow 필드로 들어감
    * */
}
