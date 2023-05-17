package pl.kempa.saska.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  @Autowired private BCryptPasswordEncoder passwordEncoder;
  @Autowired private AuthenticationManager authenticationManager;
  @Autowired private UserDetailsService userDetailsService;

  @Value("${app.oauth2.client-id}")
  private String clientId;

  @Value("${app.oauth2.client-secret}")
  private String secret;

  @Value("${app.oauth2.token-validity}")
  private int validityMS;

  @Value("${app.oauth2.signing-key}")
  private String signingKey;

  @Override
  public void configure(ClientDetailsServiceConfigurer clients)
      throws Exception {
    clients.inMemory()
        .withClient(clientId)
        .secret(passwordEncoder.encode(secret))
        .authorizedGrantTypes("password")
        .scopes("any")
        .accessTokenValiditySeconds(validityMS);
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints)
      throws Exception {
    endpoints
        .tokenStore(tokenStore())
        .accessTokenConverter(accessTokenConverter())
        .userDetailsService(userDetailsService)
        .authenticationManager(authenticationManager);
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(accessTokenConverter());
  }

  @Bean
  JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey(signingKey);
    return converter;
  }
}
