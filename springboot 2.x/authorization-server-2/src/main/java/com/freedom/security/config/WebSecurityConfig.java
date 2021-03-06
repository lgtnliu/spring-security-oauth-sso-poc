package com.freedom.security.config;

import com.freedom.security.logout.SsoServerNoticeLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Order(SecurityProperties.BASIC_AUTH_ORDER - 6)
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 密码编码器
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private LogoutFilter ssoServerAutoLogoutHandler;

    @Bean
    public LogoutSuccessHandler ssoServerNoticeLogoutSuccessHandler(){
        return new SsoServerNoticeLogoutSuccessHandler();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/toLogin")
                .loginProcessingUrl("/login")
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/logout")
                //.logoutSuccessUrl("")
                .logoutSuccessHandler(ssoServerNoticeLogoutSuccessHandler())
                //.deleteCookies("")
                .permitAll()
                .and()
            .csrf().disable()
            .addFilterBefore(ssoServerAutoLogoutHandler, LogoutFilter.class); // 添加自动登出filter
    }

    @Bean
    public UserDetailsService customUserDetailsService(){
        InMemoryUserDetailsManager inMemoryUserDetailsManager = new InMemoryUserDetailsManager();
        inMemoryUserDetailsManager.createUser(User.withUsername("user")
                                                  .password(passwordEncoder().encode("password"))
                                                  .authorities("USER_ROLE").build());

        return inMemoryUserDetailsManager;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    //@Autowired
    //public void globalUserDetails(final AuthenticationManagerBuilder auth) throws Exception {
	 //   auth.inMemoryAuthentication()
	 //       .withUser("user").password(passwordEncoder().encode("password")).roles("USER")
	 //       .and()
    //        .withUser("admin").password(passwordEncoder().encode("admin")).roles("ADMIN");
    //}

    /**
     * 将configure(AuthenticationManagerBuilder)配置的认证管理器注册为Bean
     * @return
     * @throws Exception
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
