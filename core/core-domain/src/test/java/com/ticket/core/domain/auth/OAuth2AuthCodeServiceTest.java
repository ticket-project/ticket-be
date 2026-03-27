package com.ticket.core.domain.auth;

import com.ticket.core.domain.auth.infra.oauth2.OAuth2AuthCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class OAuth2AuthCodeServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    @Mock
    private com.ticket.core.support.random.UuidSupplier uuidSupplier;

    @InjectMocks
    private OAuth2AuthCodeService oauth2AuthCodeService;

    @Test
    void 일회용_인증코드를_생성하고_memberId를_저장한다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(org.mockito.ArgumentMatchers.anyString());
        when(uuidSupplier.get()).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));

        String code = oauth2AuthCodeService.createCode(7L);

        //when
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        //then
        verify(redissonClient).getBucket(keyCaptor.capture());
        verify(bucket).set("7", Duration.ofSeconds(30));
        assertThat(keyCaptor.getValue()).isEqualTo("oauth2_auth_code:123e4567-e89b-12d3-a456-426614174000");
        assertThat(code).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
    }

    @Test
    void consumeCode는_인증코드를_소비하고_memberId를_반환한다() {
        //given
        //when
        doReturn(bucket).when(redissonClient).getBucket("oauth2_auth_code:code");
        when(bucket.getAndDelete()).thenReturn("7");

        //then
        assertThat(oauth2AuthCodeService.consumeCode("code")).contains(7L);
    }

    @Test
    void consumeCode는_저장값이_없거나_숫자가_아니면_empty를_반환한다() {
        //given
        //when
        doReturn(bucket).when(redissonClient).getBucket("oauth2_auth_code:code");
        when(bucket.getAndDelete()).thenReturn("not-a-number");

        //then
        assertThat(oauth2AuthCodeService.consumeCode("code")).isEmpty();
    }
}

