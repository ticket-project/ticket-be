//package com.ticket.core.domain.seat;
//
//import com.ticket.core.support.exception.ErrorType;
//import com.ticket.core.support.exception.NotFoundException;
//import com.ticket.storage.db.core.SeatEntity;
//import com.ticket.storage.db.core.SeatRepository;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class SeatFinder {
//
//    private final SeatRepository seatRepository;
//
//    public SeatFinder(final SeatRepository seatRepository) {
//        this.seatRepository = seatRepository;
//    }
//
//    public Seat find(final Long id) {
//        final SeatEntity seatEntity = seatRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND));
//        return new Seat(seatEntity.getId(), seatEntity.getX(), seatEntity.getY());
//    }
//
//    public List<Seat> findAllByIds(final List<Long> ids) {
//        return ids.stream()
//                .map(this::find)
//                .toList();
//    }
//}
