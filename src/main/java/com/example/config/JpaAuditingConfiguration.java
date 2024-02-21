package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 가 붙은 값들을 DB에 자동으로 저장
public class JpaAuditingConfiguration {
}