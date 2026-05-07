package com.empresa.pos.application.port.in.auth;

import com.empresa.pos.application.dto.command.LoginCommand;
import com.empresa.pos.application.dto.response.LoginResponse;

public interface LoginUseCase {

    LoginResponse login(LoginCommand command);
}
