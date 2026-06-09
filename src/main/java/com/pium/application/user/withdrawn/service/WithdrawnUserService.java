package com.pium.application.user.withdrawn.service;

import com.pium.application.auth.required.LoadUserPort;
import com.pium.application.auth.required.SaveUserPort;
import com.pium.application.user.withdrawn.provided.WithdrawnUser;
import com.pium.domain.user.exception.UserErrorCode;
import com.pium.domain.user.exception.UserException;
import com.pium.domain.user.model.User;
import com.pium.domain.user.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawnUserService implements WithdrawnUser {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;

    @Override
    public void withdrawn(UserId userId) {
        User user = loadUserPort.load(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_ID));

        user.withdraw();
        saveUserPort.save(user);
    }
}
