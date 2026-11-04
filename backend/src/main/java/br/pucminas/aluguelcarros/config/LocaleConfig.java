package br.pucminas.aluguelcarros.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

/** Fixa o idioma da API em pt-BR, para que as mensagens de validação saiam em português. */
@Configuration
public class LocaleConfig {

    @Bean
    LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("pt-BR"));
    }
}
