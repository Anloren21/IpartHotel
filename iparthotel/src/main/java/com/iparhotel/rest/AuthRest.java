
package com.iparhotel.rest;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.iparhotel.dtos.LoginDto;
import com.iparhotel.dtos.TokenDto;
import com.iparhotel.entidades.Usuario;
import com.iparhotel.repositorios.UsuarioRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRest {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final long expiration;

	public AuthRest(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
			@Value("${jwt.expiration:3600000}") long expiration) {

		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.expiration = expiration;
	}

	@PostMapping("/login")
	public TokenDto login(@Valid @RequestBody LoginDto datos) {

		Usuario usuario = usuarioRepository.findByEmail(datos.email())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

		if (!passwordEncoder.matches(datos.password(), usuario.getPassword())) {

			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
		}

		Instant ahora = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("iparhotel").subject(usuario.getEmail()).issuedAt(ahora)
				.expiresAt(ahora.plusMillis(expiration)).claim("roles", List.of(usuario.getRol().name())).build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		return new TokenDto(token, "Bearer", usuario.getRol().name());
	}
}
