package com.example.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jwt Claims Parser is used to parse authorities and subject
 * from the jwt claims body
 */
public class JwtClaminsParser {

    private static final Logger LOG = LoggerFactory.getLogger(JwtClaminsParser.class);

    private final Jwt<?, ?> jwtObject;

    public JwtClaminsParser(String jwt, String secretToken) {
        this.jwtObject = parseJwt(jwt, secretToken);
    }

    Jwt<?, ?> parseJwt(String jwtString, String secretToken) {

        byte[] secretKeyBytes = Base64.getEncoder().encode(secretToken.getBytes());
        String algorithm = Jwts.SIG.HS512.key().build().getAlgorithm();
        LOG.info("algorithm {}.", algorithm);

        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA512");

        JwtParser jwtParser = Jwts.parser()
				.verifyWith(secretKey)
				.build();

		try {
			return jwtParser.parse(jwtString);
		} catch (Exception e) {
			LOG.error("JWT parse error. {}", e.getMessage());
		}

        return null;

    }

    public Collection<GrantedAuthority> getUserAuthorities() {

        Object payload = jwtObject.getPayload();

        if (payload instanceof Claims claims) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> scopeValues = claims.get("scope", List.class);

            return scopeValues.stream().
                    map(scopeValue -> new SimpleGrantedAuthority(scopeValue.get("authority")))
                    .collect(Collectors.toList());
        }
        return null;
    }

    public String getJwtSubject() {

        Object payload = jwtObject.getPayload();

        if (payload instanceof Claims claims) {
            return claims.getSubject();
        }

        return null;
    }
}
