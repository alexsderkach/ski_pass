package io.ski.card.validator;

import io.ski.card.Card;
import io.ski.card.Validator;
import io.ski.card.validator.support.HolidayResolverAware;
import io.ski.support.validation.ValidationResult;
import io.ski.support.validation.HolidayResolver;

import java.time.Clock;
import java.time.LocalDate;

public class NotWorkdayValidator<T extends Card> implements Validator<T>, HolidayResolverAware {

  public static final String THIS_CARD_WORKS_ONLY_ON_NOT_WORKDAYS_MESSAGE = "This card works only on not workdays.";

  private final Clock clock;

  private HolidayResolver holidayResolver;

  public NotWorkdayValidator() {
    this(Clock.systemDefaultZone());
  }

  // Used for testing
  NotWorkdayValidator(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void validate(T passCard, ValidationResult validationResult) {
    LocalDate today = LocalDate.now(clock);
    if (ValidatorUtils.isWorkingDay(holidayResolver, today)) {
      validationResult.reject(THIS_CARD_WORKS_ONLY_ON_NOT_WORKDAYS_MESSAGE);
    }
  }

  @Override
  public void setHolidayResolver(HolidayResolver holidayResolver) {
    this.holidayResolver = holidayResolver;
  }
}
