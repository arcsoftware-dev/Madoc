package dev.arcsoftware.madoc.service;

import dev.arcsoftware.madoc.auth.model.AuthToken;
import dev.arcsoftware.madoc.auth.model.AuthenticationRequest;
import dev.arcsoftware.madoc.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationService {

    @Value("${auth.cookie.expiry_seconds}")
    private int cookieExpiryInSeconds;

    public AuthToken authenticate(AuthenticationRequest authRequest){
        //placeholder logic
        if(!authRequest.username().isBlank()){
            return new AuthToken(cookieExpiryInSeconds, "123456");
        }
        throw new UnauthorizedException("Unauthorized, invalid username or password");
    }

    public AuthToken emptyToken(){
        return new AuthToken(0, "");
    }
}
