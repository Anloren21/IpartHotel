package com.iparhotel.seguridad;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.iparhotel.repositorios.UsuarioRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {

		return email -> usuarioRepository.findByEmail(email)
				.map(usuario -> User.builder().username(usuario.getEmail()).password(usuario.getPassword())
						.roles(usuario.getRol().name()).build())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
	}

	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

		provider.setPasswordEncoder(passwordEncoder);

		return new ProviderManager(provider);
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			String rol = jwt.getClaimAsString("rol");

			if ("SOCIO".equals(rol) || "ADMINISTRADOR".equals(rol)) {

				return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
			}

			return List.of();
		});

		return converter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter)
			throws Exception {

		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				.csrf(AbstractHttpConfigurer::disable)

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						.requestMatchers(HttpMethod.POST, "/api/v2/usuarios/login").permitAll()

						.requestMatchers(HttpMethod.GET, "/api/v2/actividades").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/v2/actividades").hasRole("ADMINISTRADOR")

						.requestMatchers("/api/v2/**").authenticated()

						.anyRequest().denyAll())

				.exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, ex) -> {
					response.setHeader("WWW-Authenticate", "Bearer");
					error401(response);
				}).accessDeniedHandler((request, response, ex) -> error403(response)))

				.oauth2ResourceServer(oauth2 -> oauth2.authenticationEntryPoint((request, response, ex) -> {
					response.setHeader("WWW-Authenticate", "Bearer");
					error401(response);
				}).jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of("http://localhost:4200"));

		config.setAllowedMethods(List.of("GET", "POST", "DELETE", "OPTIONS"));

		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", config);

		return source;
	}

	private void error401(HttpServletResponse response) throws IOException {

		response.setStatus(401);
		response.setContentType("application/problem+json");
		response.setCharacterEncoding("UTF-8");

		response.getWriter().write("""
				{
				  "type": "about:blank",
				  "title": "Unauthorized",
				  "status": 401,
				  "detail": "Token ausente, inválido o caducado"
				}
				""");
	}

	private void error403(HttpServletResponse response) throws IOException {

		response.setStatus(403);
		response.setContentType("application/problem+json");
		response.setCharacterEncoding("UTF-8");

		response.getWriter().write("""
				{
				  "type": "about:blank",
				  "title": "Forbidden",
				  "status": 403,
				  "detail": "No tienes permisos suficientes"
				}
				""");
	}

}
