package com.ticket.core.domain.auth.oauth2;

import com.ticket.core.domain.auth.infra.oauth2.KakaoApiClient;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class KakaoUnlinkServiceTest {

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Test
    void 사용자아이디가_비어있으면_예외를_던진다() {
        //given
        KakaoUnlinkService kakaoUnlinkService = new KakaoUnlinkService(kakaoApiClient, "admin-key");

        //when
        //then
        assertThatThrownBy(() -> kakaoUnlinkService.unlinkByUserId(" "))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(kakaoApiClient);
    }

    @Test
    void 관리자키가_비어있으면_예외를_던진다() {
        //given
        KakaoUnlinkService kakaoUnlinkService = new KakaoUnlinkService(kakaoApiClient, "");

        //when
        //then
        assertThatThrownBy(() -> kakaoUnlinkService.unlinkByUserId("123"))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(kakaoApiClient);
    }

    @Test
    void 정상요청이면_카카오_unlink_API를_호출한다() {
        //given
        KakaoUnlinkService kakaoUnlinkService = new KakaoUnlinkService(kakaoApiClient, "admin-key");

        kakaoUnlinkService.unlinkByUserId("123");

        //when
        ArgumentCaptor<MultiValueMap<String, String>> formCaptor = ArgumentCaptor.forClass(MultiValueMap.class);
        //then
        verify(kakaoApiClient).unlink(org.mockito.ArgumentMatchers.eq("KakaoAK admin-key"), formCaptor.capture());
        assertThat(formCaptor.getValue().getFirst("target_id_type")).isEqualTo("user_id");
        assertThat(formCaptor.getValue().getFirst("target_id")).isEqualTo("123");
    }

    @Test
    void 카카오_API_실패는_기본오류로_변환한다() {
        //given
        KakaoUnlinkService kakaoUnlinkService = new KakaoUnlinkService(kakaoApiClient, "admin-key");
        doThrow(new IllegalStateException("boom")).when(kakaoApiClient).unlink(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());

        //when
        //then
        assertThatThrownBy(() -> kakaoUnlinkService.unlinkByUserId("123"))
                .isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.DEFAULT_ERROR));
    }
}

