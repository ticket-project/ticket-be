package com.ticket.core.domain.auth.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RBucket<String> bucket;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void 리프레시토큰을_생성하고_memberId를_저장한다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket(org.mockito.ArgumentMatchers.anyString());

        String token = refreshTokenService.createRefreshToken(3L, 120L);

        //when
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        //then
        verify(redissonClient).getBucket(keyCaptor.capture());
        verify(bucket).set("3", Duration.ofSeconds(120L));
        assertThat(keyCaptor.getValue()).startsWith("refresh_token:");
        assertThat(token).isNotBlank();
    }

    @Test
    void validate는_토큰을_소비하고_memberId를_반환한다() {
        //given
        //when
        doReturn(bucket).when(redissonClient).getBucket("refresh_token:token-value");
        when(bucket.getAndDelete()).thenReturn("3");

        //then
        assertThat(refreshTokenService.validate("token-value")).contains(3L);
    }

    @Test
    void validateWithoutConsume는_토큰을_소비하지_않고_memberId를_반환한다() {
        //given
        //when
        doReturn(bucket).when(redissonClient).getBucket("refresh_token:token-value");
        when(bucket.get()).thenReturn("3");

        //then
        assertThat(refreshTokenService.validateWithoutConsume("token-value")).contains(3L);
    }

    @Test
    void 저장된_memberId가_숫자가_아니면_empty를_반환한다() {
        //given
        //when
        doReturn(bucket).when(redissonClient).getBucket("refresh_token:token-value");
        when(bucket.getAndDelete()).thenReturn("not-a-number");

        //then
        assertThat(refreshTokenService.validate("token-value")).isEmpty();
    }

    @Test
    void 소유자가_일치하면_compareAndSet으로_무효화한다() {
        //given
        doReturn(bucket).when(redissonClient).getBucket("refresh_token:token-value");
        when(bucket.compareAndSet("3", null)).thenReturn(true);

        //when
        boolean revoked = refreshTokenService.revokeIfOwned("token-value", 3L);

        //then
        assertThat(revoked).isTrue();
        verify(bucket).compareAndSet("3", null);
    }
}

