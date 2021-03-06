package io.ski.util.day;

import io.ski.card.Card;
import io.ski.card.Validator;
import io.ski.support.validation.ValidationResult;
import io.ski.util.AbstractMockitoTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(Parameterized.class)
public abstract class AbstractDayRealWorkdayTest extends AbstractMockitoTest {

  private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

  @Mock private Card card;
  @Mock private Clock clock;

  private final LocalDate realWorkday;

  public AbstractDayRealWorkdayTest(LocalDate realWorkday) {
    this.realWorkday = realWorkday;
  }

  @Before
  public void setup(){
    initMocks(this);
    when(clock.getZone()).thenReturn(DEFAULT_ZONE_ID);
  }

  protected void validateOnRealWorkday(Validator<Card> validator, ValidationResult validationResult) {
    when(clock.instant()).thenReturn(DayUtils.dateToInstant(realWorkday));

    validator.validate(card, validationResult);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> realWorkdays() {
    return Arrays.asList(new Object[][]{
        {LocalDate.of(2015, Month.OCTOBER, 5)},
        {LocalDate.of(2015, Month.OCTOBER, 6)},
        {LocalDate.of(2015, Month.OCTOBER, 7)},
        {LocalDate.of(2015, Month.OCTOBER, 8)},
        {LocalDate.of(2015, Month.OCTOBER, 9)},
    });
  }

  protected Clock getClock() {
    return clock;
  }
}