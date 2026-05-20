package com.pium.application.user.bootstrap.service;

import com.pium.adapter.outbound.user.UserProfilePersistenceAdapter;
import com.pium.application.auth.required.LoadUserProfilePort;
import com.pium.application.user.bootstrap.dto.UserBootstrapView;
import com.pium.application.user.bootstrap.provided.GetUserBootstrap;
import com.pium.application.user.bootstrap.required.CheckUserDiagnosisPort;
import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;
import com.pium.domain.user.model.UserProfile;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 로그인 사용자의 초기 진입 상태를 계산한다.
 */
@Service
@RequiredArgsConstructor
public class GetUserBootstrapService implements GetUserBootstrap{

    private final CheckUserDiagnosisPort checkUserDiagnosisPort;
    private final LoadUserProfilePort loadUserProfilePort;

    @Override
    public UserBootstrapView getUserBootstrap(UserId userId) {

        String userName = loadUserProfilePort.findByUserId(userId)
                .map(UserProfile::getNickname)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_PROFILE_ID));


        boolean hasDiagnosis = checkUserDiagnosisPort.existsByUserId(userId);
        UserBootstrapView.EntryPoint entryPoint = hasDiagnosis
                ? UserBootstrapView.EntryPoint.HOME
                : UserBootstrapView.EntryPoint.SURVEY;

        return new UserBootstrapView(userName,hasDiagnosis, entryPoint);
    }
}
