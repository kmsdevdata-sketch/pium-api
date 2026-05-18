package com.pium.application.user.bootstrap.service;

import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.application.user.bootstrap.provided.GetUserBootstrap;
import com.pium.application.user.bootstrap.required.CheckUserDiagnosisPort;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 로그인 사용자의 초기 진입 상태를 계산한다.
 */
@Service
@RequiredArgsConstructor
public class GetUserBootstrapService implements GetUserBootstrap {

    private final CheckUserDiagnosisPort checkUserDiagnosisPort;

    @Override
    public UserBootstrapView getUserBootstrap(UserId userId) {
        boolean hasDiagnosis = checkUserDiagnosisPort.existsByUserId(userId);
        UserBootstrapView.EntryPoint entryPoint = hasDiagnosis
                ? UserBootstrapView.EntryPoint.HOME
                : UserBootstrapView.EntryPoint.SURVEY;

        return new UserBootstrapView(hasDiagnosis, entryPoint);
    }
}
