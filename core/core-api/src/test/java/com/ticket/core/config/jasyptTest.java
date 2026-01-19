package com.ticket.core.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class jasyptTest {

    @Test
    public void jasyptTest() {
        //given
        String password = "password1234";
        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword("test");
        jasypt.setAlgorithm("PBEWITHMD5ANDDES");

        //when
        String encryptedText = jasypt.encrypt(password);
        String decryptedText = jasypt.decrypt(encryptedText);

        System.out.println(encryptedText);
        System.out.println(decryptedText);

        //then
        assertThat(encryptedText).isNotEqualTo(password);
        assertThat(decryptedText).isEqualTo(password);
    }
}
