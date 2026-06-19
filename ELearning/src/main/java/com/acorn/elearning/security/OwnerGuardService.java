package com.acorn.elearning.security;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class OwnerGuardService {
    public void requireOwnerOrAdmin(Long ownerId, SessionUser sessionUser) {
        if (sessionUser == null || (!sessionUser.admin() && !sessionUser.userId().equals(ownerId))) throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
    }
}
