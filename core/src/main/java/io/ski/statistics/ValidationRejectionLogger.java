package io.ski.statistics;

import io.ski.card.Card;
import io.ski.card.event.PostValidationListener;
import io.ski.statistics.domain.UnauthorizedPassEvent;
import io.ski.statistics.repository.PassEventRepository;
import io.ski.support.validation.ValidationResult;

import java.time.LocalDateTime;

public class ValidationRejectionLogger implements PostValidationListener {

  private final PassEventRepository passEventRepository;

  public ValidationRejectionLogger(PassEventRepository passEventRepository) {
    this.passEventRepository = passEventRepository;
  }

  @Override
  public void postValidation(Card card, ValidationResult validationResult) {
    if (!validationResult.hasErrors())
      return;

    LocalDateTime now = LocalDateTime.now();
    UnauthorizedPassEvent event = new UnauthorizedPassEvent(validationResult.getErrors(), now, card);
    passEventRepository.persist(event);
  }
}
