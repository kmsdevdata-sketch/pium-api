package com.pium.application.user.bootstrap.required;

import com.pium.domain.user.vo.UserId;

/**
 * 사용자의 진단 이력 존재 여부를 확인한다.
 */
public interface CheckUserDiagnosisPort {

    boolean existsByUserId(UserId userId);
}
