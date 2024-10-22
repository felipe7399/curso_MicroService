package br.com.microservices.orchestrated.orderservice.core.controller;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilter;
import br.com.microservices.orchestrated.orderservice.core.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/event")
public class EventController {

    private EventService eventService;

    @GetMapping("/findByFilter")
    public ResponseEntity<Event> findByFilter (EventFilter eventFilter) {
        return ResponseEntity.ok(eventService.findByFilter(eventFilter));
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<Event>> findAll () {
        return ResponseEntity.ok(eventService.findAll());
    }

}
