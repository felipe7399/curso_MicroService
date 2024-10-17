package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilter;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private EventRepository eventRepository;

    public void save (Event event ) {
        save(event, false);
    }

    public void save (Event event , boolean isEnding) {
        event.setCreatedAt((event.getCreatedAt() == null || event.getCreatedAt().toString().isEmpty()) ? LocalDateTime.now() : event.getCreatedAt());
        eventRepository.save(event);
        if (isEnding) {
            log.info("Order {} ended! Transaction ID: {}", event.getOrderId(), event.getTransactionId());
        }
    }

    public List<Event> findAll () {
        return eventRepository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilter (EventFilter eventFilter) {
        validateEmptyFilters(eventFilter);
        if (!eventFilter.getTransactionId().isEmpty()) {
            return eventRepository.findById(eventFilter.getTransactionId()).orElseThrow(() -> new ValidationException("Event not found by Transaction ID"));
        }
        else {
            return eventRepository.findById(eventFilter.getOrderId()).orElseThrow(() -> new ValidationException("Event not found by order ID"));
        }

    }

    private void validateEmptyFilters(EventFilter eventFilter) {
        if (eventFilter.getOrderId().isEmpty() && eventFilter.getTransactionId().isEmpty()){
           throw new ValidationException("Filters empty");
        }
    }

}
