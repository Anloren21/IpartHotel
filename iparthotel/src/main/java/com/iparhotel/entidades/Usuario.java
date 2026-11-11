package com.iparhotel.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false)
	private String nombre;

	@NotBlank
	@Email
	@Column(nullable = false, unique = true)
	private String email;

	@JsonIgnore
	@NotBlank
	@Column(nullable = false)
	private String password;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rol rol;

}
