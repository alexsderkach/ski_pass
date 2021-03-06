package io.ski.cards.workday.limited;

import io.ski.cards.workday.limited.support.AbstractWorkdayLimitedCardDefinition;

public class WorkdayLimited100CardDefinition extends AbstractWorkdayLimitedCardDefinition {

  private static final String CARD_DISCRIMINATOR = WorkdayLimited100CardDefinition.class.getSimpleName();
  private static final long TRIP_COUNT = 100L;

  @Override
  public Long getTripCount() {
    return TRIP_COUNT;
  }

  @Override
  public String getDiscriminator() {
    return CARD_DISCRIMINATOR;
  }
}
