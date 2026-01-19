package com.ticket.core.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JasyptTest {

    @Test
    public void jasyptEncryptTest() {
        // given
        String url = "url";
        String username = "username";
        String password = "password";

        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword("mypassword");
        jasypt.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        jasypt.setIvGenerator(new RandomIvGenerator());

        // when - 암호화
        String encryptedUrl = jasypt.encrypt(url);
        String encryptedUsername = jasypt.encrypt(username);
        String encryptedPassword = jasypt.encrypt(password);

        System.out.println("=== 암호화 결과 (application.yml용) ===");
        System.out.println("url: ENC(" + encryptedUrl + ")");
        System.out.println("username: ENC(" + encryptedUsername + ")");
        System.out.println("password: ENC(" + encryptedPassword + ")");

        // 복호화 검증
        assertThat(jasypt.decrypt(encryptedUrl)).isEqualTo(url);
        assertThat(jasypt.decrypt(encryptedUsername)).isEqualTo(username);
        assertThat(jasypt.decrypt(encryptedPassword)).isEqualTo(password);
    }
}
