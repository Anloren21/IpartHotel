
package com.iparhotel.excepciones;

import lombok.Getter;

@Getter
public class ReservaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final CodigoError codigo;

	public ReservaException(String mensaje) {
		this(CodigoError.DATOS_INVALIDOS, mensaje);
	}

	public ReservaException(CodigoError codigo, String mensaje) {
		super(mensaje);
		this.codigo = codigo;
	}

	public ReservaException(String mensaje, Throwable causa) {
		super(mensaje, causa);
		this.codigo = CodigoError.DATOS_INVALIDOS;
	}
}
