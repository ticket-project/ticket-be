package com.ticket.core.domain.commoncode.usecase;

import com.ticket.core.api.controller.response.MetaCodesResponse;
import com.ticket.core.domain.show.Category;
import com.ticket.core.domain.show.CategoryRepository;
import com.ticket.core.domain.show.Genre;
import com.ticket.core.domain.show.GenreRepository;
import com.ticket.core.domain.show.Region;
import com.ticket.core.domain.show.SaleStatus;
import com.ticket.core.domain.show.SaleType;
import com.ticket.core.domain.show.ShowSortKey;
import com.ticket.core.enums.BookingStatus;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.HoldState;
import com.ticket.core.enums.OrderState;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.enums.Role;
import com.ticket.core.enums.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMetaCodesUseCase {

    private final CategoryRepository categoryRepository;
    private final GenreRepository genreRepository;

    public record Output(MetaCodesResponse codes) {}

    public Output execute() {
        final List<MetaCodesResponse.CategoryCodeItem> categories = categoryRepository.findAllByOrderByIdAsc().stream()
                .map(this::toCategoryCodeItem)
                .toList();

        final List<MetaCodesResponse.GenreCodeItem> genres = genreRepository.findAllByOrderByCategory_IdAscNameAsc().stream()
                .map(this::toGenreCodeItem)
                .toList();

        final MetaCodesResponse.EnumCodes enums = new MetaCodesResponse.EnumCodes(
                mapEnumValues(BookingStatus.values(), BookingStatus::getCode, BookingStatus::getDescription),
                mapEnumValues(PerformanceState.values(), PerformanceState::getCode, PerformanceState::getDescription),
                mapEnumValues(PerformanceSeatState.values(), PerformanceSeatState::getCode, PerformanceSeatState::getDescription),
                mapEnumValues(HoldState.values(), HoldState::getCode, HoldState::getDescription),
                mapEnumValues(OrderState.values(), OrderState::getCode, OrderState::getDescription),
                mapEnumValues(SocialProvider.values(), SocialProvider::getCode, SocialProvider::getDescription),
                mapEnumValues(Role.values(), Role::getCode, Role::getDescription),
                mapEnumValues(EntityStatus.values(), EntityStatus::getCode, EntityStatus::getDescription),
                mapEnumValues(SaleType.values(), SaleType::getCode, SaleType::getDescription),
                mapEnumValues(SaleStatus.values(), SaleStatus::getCode, SaleStatus::getDescription),
                mapEnumValues(Region.values(), Region::getCode, Region::getDescription),
                mapEnumValues(ShowSortKey.values(), ShowSortKey::getApiValue, ShowSortKey::getDescription)
        );

        return new Output(new MetaCodesResponse(categories, genres, enums));
    }

    private MetaCodesResponse.CategoryCodeItem toCategoryCodeItem(final Category category) {
        return new MetaCodesResponse.CategoryCodeItem(
                category.getId(),
                category.getCode(),
                category.getName()
        );
    }

    private MetaCodesResponse.GenreCodeItem toGenreCodeItem(final Genre genre) {
        return new MetaCodesResponse.GenreCodeItem(
                genre.getId(),
                genre.getCategory().getCode(),
                genre.getCode(),
                genre.getName()
        );
    }

    private <E> List<MetaCodesResponse.EnumCodeItem> mapEnumValues(
            final E[] values,
            final Function<E, String> codeMapper,
            final Function<E, String> descriptionMapper
    ) {
        return Arrays.stream(values)
                .map(v -> new MetaCodesResponse.EnumCodeItem(
                        codeMapper.apply(v),
                        descriptionMapper.apply(v)
                ))
                .toList();
    }
}
