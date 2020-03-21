package com.campsite.reservation.config;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

@Configuration
@EnableSwagger2WebFlux
public class SwagerConfig {
  @Bean
  public Docket api() {
      return new Docket(DocumentationType.SWAGGER_2)
              .select()
              .apis(RequestHandlerSelectors.basePackage("com.campsite.reservation.controller"))
              .paths(PathSelectors.ant("/reservations/*"))
              .build()
              .apiInfo(new ApiInfo(
            	      "Flux Reservations",
            	      "Demo REST API - Make Campsite Reservations",
            	      "1.0",
            	      null,
            	      new Contact("Raul Bajales", "https://www.linkedin.com/in/raulbajales", "raul.bajales@gmail.com"),
            	      "GNU v3.0",
            	      "https://www.gnu.org/licenses/gpl-3.0.html",
            	      new ArrayList<>()));
  }
}