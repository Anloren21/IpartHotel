package com.iparhotel.seguridad;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import com.iparhotel.entidades.Usuario;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtEncoder jwtEncoder;

	@Value("${jwt.expiration}")
	private long expiration;

	@Value("${jwt.issuer}")
	private String issuer;

	public String generarToken(Usuario usuario) {

		Instant ahora = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).issuedAt(ahora)
				.expiresAt(ahora.plusSeconds(expiration)).subject(usuario.getEmail())
				.claim("nombre", usuario.getNombre()).claim("rol", usuario.getRol().name()).build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

}
