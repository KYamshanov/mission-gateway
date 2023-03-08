package ru.kyamshanov.mission.gateway.authorization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.kyamshanov.mission.gateway.authorization.AuthenticationService.MISSION

internal interface AuthenticationFactory {

    fun createFacade(authenticationService: AuthenticationService): AuthenticationFacade
}

@Component
private class AuthenticationFactoryImpl @Autowired constructor(
    @AuthenticationServiceQualifier(MISSION) private val missionAuthenticationFacade: AuthenticationFacade
) : AuthenticationFactory {
    override fun createFacade(authenticationService: AuthenticationService): AuthenticationFacade =
        when (authenticationService) {
            MISSION -> missionAuthenticationFacade
        }

}