package br.com.lf.brasileiraoapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;


@Configuration
public class ModelMapparConfig {
	@Bean
	public ModelMapper modelMappar() {
	return new ModelMapper();
	}

}
