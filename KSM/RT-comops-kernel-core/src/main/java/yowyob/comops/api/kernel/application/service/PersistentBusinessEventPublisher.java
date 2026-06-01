package yowyob.comops.api.kernel.application.service;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.OutboxEventRepository;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.OutboxEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PersistentBusinessEventPublisher implements BusinessEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    public PersistentBusinessEventPublisher(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    public Mono<Void> publish(BusinessEvent event) {
        return outboxEventRepository.save(OutboxEvent.create(event)).then();
    }
}
