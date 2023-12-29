package com.smart.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class Myconfigure {
	@Bean
	 UserDetailsService getUserDetailService() { 
		return new UserDetailsServiceImpl();
	}
	@Bean
	 BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	@Bean
	 DaoAuthenticationProvider authenticationProvider() {
		System.out.println("AUTHENTICATION PROVIDER >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		DaoAuthenticationProvider daoAuthenticationProvider= new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(getUserDetailService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}
	@Bean
	SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
	    System.out.println("FILTER CHAIN >>>>>>>>>");
	    httpSecurity.authenticationProvider(authenticationProvider());
	    httpSecurity                             // it will call controller /signin
	             .formLogin(form -> form.loginPage("/signin").permitAll()
	            		                .loginProcessingUrl("/process-loginPage")
	            		                .defaultSuccessUrl("/user/index")
//	            		                .failureUrl("/failure-login")
	            		                )
	             .csrf(csrf -> csrf.disable())
	    		 .authorizeHttpRequests((authorize) -> authorize.requestMatchers(new AntPathRequestMatcher("/user/**")).authenticated()
	            		               .anyRequest().permitAll()
	            		               ).httpBasic(Customizer.withDefaults());
	    return httpSecurity.build();
	}

	
}
