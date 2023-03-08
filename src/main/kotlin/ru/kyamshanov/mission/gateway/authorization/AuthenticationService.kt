package ru.kyamshanov.mission.gateway.authorization

import org.springframework.beans.factory.annotation.Qualifier

@Qualifier
internal annotation class AuthenticationServiceQualifier(

    val serviceType: AuthenticationService
)

internal enum class AuthenticationService {

    MISSION
}