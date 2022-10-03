package com.faceit.assessment.infrastructure;

public interface DomainEventPublisher<V extends DomainEvent> {

    void publishEvent(V domainEvent);
}
