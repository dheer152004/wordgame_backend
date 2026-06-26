package com.example.WordGame.modules.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class JwtVerifier {
    private final String jwksUrl;
    private volatile JWKSet jwkSet;
    private final AtomicLong jwkFetchedAt = new AtomicLong(0);

    public JwtVerifier(String jwksUrl) {
        this.jwksUrl = jwksUrl;
    }

    private synchronized void refreshIfNeeded() throws Exception {
        long now = Instant.now().getEpochSecond();
        if (jwkSet == null || now - jwkFetchedAt.get() > 60 * 60) { // refresh hourly
            URL url = new URL(jwksUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (InputStream is = conn.getInputStream()) {
                jwkSet = JWKSet.load(is);
                jwkFetchedAt.set(now);
            }
        }
    }

    public Map<String, Object> verifyAndGetClaims(String token) throws Exception {
        refreshIfNeeded();
        SignedJWT jwt = SignedJWT.parse(token);
        JWSHeader header = jwt.getHeader();
        String kid = header.getKeyID();
        JWK jwk = jwkSet.getKeyByKeyId(kid);
        if (jwk == null) {
            // attempt refresh and try again
            refreshIfNeeded();
            jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) throw new JOSEException("JWK with kid not found");
        }
        if (!(jwk instanceof RSAKey)) throw new JOSEException("Unsupported key type");
        RSAKey rsa = (RSAKey) jwk;
        RSAPublicKey pub = rsa.toRSAPublicKey();
        boolean valid = jwt.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(pub));
        if (!valid) throw new JOSEException("JWT signature validation failed");

        // check exp
        Date exp = jwt.getJWTClaimsSet().getExpirationTime();
        if (exp == null || exp.before(new Date())) throw new JOSEException("JWT expired");

        return jwt.getJWTClaimsSet().getClaims();
    }
}
