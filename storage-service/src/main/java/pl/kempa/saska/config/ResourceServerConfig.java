package pl.kempa.saska.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  @Value("${app.oauth2.signing-key}")
  private String signingKey;

  @Override
  public void configure(HttpSecurity http)
      throws Exception {
    http
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/**").permitAll()
        .antMatchers("/actuator/**").permitAll()
        .antMatchers("/api/storages/**").hasRole("USER")
        .antMatchers(HttpMethod.POST, "/api/storages").hasRole("ADMIN")
        .antMatchers(HttpMethod.DELETE, "/api/storages/**").hasRole("ADMIN")
        .anyRequest().authenticated();
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer config) {
    config.tokenStore(tokenStore());
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(signingKey);
    return converter;
  }
}
