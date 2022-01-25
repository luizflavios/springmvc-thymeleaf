package com.projetos.springmvc.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Encrypter {
	
	public static void main(String[] args) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); 
		String codificado = encoder.encode("admin");
		System.out.println(codificado);
	}
	
}
