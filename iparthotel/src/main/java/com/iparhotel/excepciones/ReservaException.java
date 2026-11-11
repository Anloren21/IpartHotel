package com.iparhotel.excepciones;

public class ReservaException extends RuntimeException {

	private static final long serialVersionUID = -8430769904459116167L;

	public ReservaException(String mensaje) {
		super(mensaje);
	}

	public ReservaException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
