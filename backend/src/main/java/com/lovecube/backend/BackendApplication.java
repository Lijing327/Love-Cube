package com.lovecube.backend;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.crypto.SecretKey;
import java.util.Base64;


@SpringBootApplication
@EnableJpaRepositories("com.lovecube.backend.repository")
public class BackendApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(BackendApplication.class, args);
//		SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
//		String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
//		System.out.println("生成的 SECRET_KEY: " + base64Key);
        
    }

}
