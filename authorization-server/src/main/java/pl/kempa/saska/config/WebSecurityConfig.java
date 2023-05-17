package pl.kempa.saska.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import lombok.extern.slf4j.Slf4j;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired private BCryptPasswordEncoder passwordEncoder;

  @Value("${app.oauth2.user-name}")
  private String userName;

  @Value("${app.oauth2.user-pass}")
  private String userPass;

  @Value("${app.oauth2.admin-name}")
  private String adminName;

  @Value("${app.oauth2.admin-pass}")
  private String adminPass;

  @Bean
  protected AuthenticationManager getAuthenticationManager()
      throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  @Override
  public UserDetailsService userDetailsService() {
    UserDetails user = User.builder()
        .username(userName)
        .password(passwordEncoder.encode(userPass))
        .roles("USER")
        .build();
    UserDetails admin = User.builder()
        .username(adminName)
        .password(passwordEncoder.encode(adminPass))
        .roles("USER", "ADMIN")
        .build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth)
      throws Exception {
    auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder);
  }
}
