package com.assignment.rest;

import com.assignment.domain.Reservation;
import com.assignment.exception.BookingException;
import com.assignment.rest.assembler.ReservationResourceAssembler;
import com.assignment.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.assignment.rest.ErrorResponse.anErrorMessage;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Custom implementation for POST and PUT methods instead of standard Spring Data-Rest.
 * GET and DELETE methods are not customized.
 * RestExceptionHandler handle some common errors like 500 or 404.
 */
@RepositoryRestController
public class ReservationResource {

    private final ReservationService reservationService;
    private final RepositoryEntityLinks entityLinks;
    private final ReservationResourceAssembler resourceAssembler;

    @Autowired
    public ReservationResource(ReservationService reservationService, RepositoryEntityLinks entityLinks,
                               ReservationResourceAssembler resourceAssembler) {
        this.reservationService = reservationService;
        this.entityLinks = entityLinks;
        this.resourceAssembler = resourceAssembler;
    }

    @RequestMapping(value = "/reservations", method = POST)
    public @ResponseBody ResponseEntity<?> createReservation(@RequestBody final Resource<Reservation> bookingRequest) {
        try {
            Reservation reservation = reservationService.save(bookingRequest.getContent());
            return success(CREATED, reservation);
        } catch (BookingException e) {
            return error(CONFLICT, e);
        }
    }

    @RequestMapping(value = "/reservations/{reservationId}", method = PUT)
    public @ResponseBody ResponseEntity<?> updateReservation(@RequestBody final Resource<Reservation> bookingRequest,
            @PathVariable("reservationId") Long reservationId) {
        if (!isReservationExist(reservationId)) {
            return ResponseEntity.status(NOT_FOUND)
                    .build();
        }
        try {
            Reservation requestReservation = bookingRequest.getContent();
            requestReservation.setId(reservationId);
            Reservation reservation = reservationService.save(requestReservation);
            return success(OK, reservation);
        } catch (BookingException e) {
            return error(CONFLICT, e);
        }
    }

    private boolean isReservationExist(Long reservationId) {
        return reservationService.get(reservationId) != null;
    }

    private ResponseEntity<Resource<Reservation>> success(HttpStatus status, Reservation reservation) {
        return ResponseEntity.status(status)
                .body(resourceAssembler.toResource(reservation));
    }

    private ResponseEntity<ErrorResponse<String>> error(HttpStatus status, Exception e) {
        return ResponseEntity.status(status)
                .body(anErrorMessage(e.getMessage()));
    }

}
