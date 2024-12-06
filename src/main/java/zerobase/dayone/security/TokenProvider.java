package zerobase.dayone.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zerobase.dayone.service.MemberService;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60;

    private final MemberService memberService;

    @Value("${spring.jwt.secret}")
    private String secretKey;

    public String generateToken(String username , List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(KEY_ROLES, roles);

        var now =new Date();
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(SignatureAlgorithm.HS512, decodeSecretKey()) // 디코딩된 키 사용
                .compact();
    }

    public String getUsername(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            var claims = this.parseClaims(token);
            return claims.getExpiration().after(new Date()); // 현재 시간 이후면 유효
        } catch (ExpiredJwtException e) {
            return false; // 만료된 토큰은 false 반환
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(decodeSecretKey()) // 디코딩된 키 사용
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


    public Authentication getAuthentication(String jwt) {
        Claims claims = this.parseClaims(jwt);
        List<String> roles = claims.get("roles", List.class); // roles 추출
        System.out.println("Parsed roles: " + roles);

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        System.out.println("Granted authorities: " + authorities);

        UserDetails userDetails = memberService.loadUserByUsername(this.getUsername(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    private String decodeSecretKey() {
        String decodedKey = new String(Base64.getDecoder().decode(this.secretKey));
        System.out.println("Decoded Secret Key: " + decodedKey);
        return decodedKey;
    }
}
