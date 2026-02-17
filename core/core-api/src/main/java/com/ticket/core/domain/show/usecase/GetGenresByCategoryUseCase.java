package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.GenreResponse;
import com.ticket.core.domain.show.Genre;
import com.ticket.core.domain.show.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetGenresByCategoryUseCase {

    private final GenreRepository genreRepository;

    public record Input(String categoryCode) {
    }

    public record Output(List<GenreResponse> genres) {
    }

    public Output execute(Input input) {
        List<Genre> genres;
        
        if (input.categoryCode == null || input.categoryCode.isBlank()) {
            genres = genreRepository.findAllByOrderByCategory_IdAscNameAsc();
        } else {
            genres = genreRepository.findAllByCategory_CodeOrderByName(input.categoryCode);
        }
        
        List<GenreResponse> responses = genres.stream()
                .map(g -> new GenreResponse(g.getId(), g.getCode(), g.getName()))
                .toList();
        
        return new Output(responses);
    }
}
