package com.infopulse.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public final class TokenUtils {
    private TokenUtils() {
    }

    public static String createAccessJwtToken() {


        Claims claims = Jwts.claims().setSubject("user1");
        //claims.put("scopes", userContext.getAuthorities().stream().map(s -> s.toString()).collect(Collectors.toList()));


        Date currentTime = new Date();

        return  Jwts.builder()
                .setClaims(claims)
                .setIssuer("keycloak")
                .setIssuedAt(currentTime)
                .setExpiration(currentTime)
                .signWith(SignatureAlgorithm.HS512, "erwewerwerwerwfeafwfeewfssadfdsffdfddfdsfddfsfdserw")
                .compact();

    }

}
