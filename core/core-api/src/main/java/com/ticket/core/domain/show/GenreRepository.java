package com.ticket.core.domain.show;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findAllByOrderByCategory_IdAscNameAsc();

    List<Genre> findAllByCategory_CodeOrderByName(String categoryCode);
}
