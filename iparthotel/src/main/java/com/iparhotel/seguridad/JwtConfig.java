package com.iparhotel.seguridad;

import java.time.Duration;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtConfig {

	@Value("${jwt.secret}")
	private String secreto;

	@Value("${jwt.issuer}")
	private String issuer;

	@Bean
	public SecretKey jwtSecretKey() {

		byte[] clave = Base64.getDecoder().decode(secreto);

		if (clave.length < 32) {
			throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
		}

		return new SecretKeySpec(clave, "HmacSHA256");
	}

	@Bean
	public JwtEncoder jwtEncoder(SecretKey clave) {

		return NimbusJwtEncoder.withSecretKey(clave).build();
	}

	@Bean
	public JwtDecoder jwtDecoder(SecretKey clave) {

		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();

		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(Duration.ZERO),
				new JwtIssuerValidator(issuer)));

		return decoder;
	}

}
