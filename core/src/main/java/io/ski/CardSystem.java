package io.ski;

import io.ski.card.*;
import io.ski.card.event.PostHandleListener;
import io.ski.card.event.PostValidationListener;
import io.ski.card.validator.support.HolidayResolverAware;
import io.ski.exception.AlreadyRegisteredCardTypeException;
import io.ski.exception.UnregisteredCardTypeException;
import io.ski.repository.CardRepository;
import io.ski.statistics.HandleLogger;
import io.ski.statistics.ValidationRejectionLogger;
import io.ski.statistics.View;
import io.ski.statistics.repository.PassEventRepository;
import io.ski.support.validation.ValidationResult;
import io.ski.support.validation.HolidayResolver;

import java.util.*;

public class CardSystem {
  private final Map<String, CardProvider<? extends Card>> cardProviderResolver;
  private final CardRepository cardRepository;
  private final PassEventRepository passEventRepository;
  private final HolidayResolver holidayResolver;

  private final Collection<PostHandleListener> postHandleListeners;
  private final Collection<PostValidationListener> postValidationListeners;

  public CardSystem(CardRepository cardRepository, PassEventRepository passEventRepository, HolidayResolver holidayResolver) {
    this.holidayResolver = holidayResolver;
    this.cardRepository = cardRepository;
    this.passEventRepository = passEventRepository;

    this.cardProviderResolver = new HashMap<>();
    this.postHandleListeners = new ArrayList<>();
    this.postValidationListeners = new ArrayList<>();

    initPostListeners();
  }

  public View createEventQueryView() {
    return new View(this.passEventRepository);
  }

  public <T extends Card> void registerCardType(CardDefinition<T> cardDefinition) {
    String discriminator = cardDefinition.getDiscriminator();
    CardFactory<T> cardFactory = cardDefinition.getCardFactory();
    Validator<T> validator = cardDefinition.getValidator();
    Handler<T> handler = cardDefinition.getHandler();

    Objects.requireNonNull(discriminator);
    Objects.requireNonNull(cardFactory);
    Objects.requireNonNull(validator);
    Objects.requireNonNull(handler);

    if (cardProviderResolver.containsKey(discriminator)) {
      throw new AlreadyRegisteredCardTypeException(discriminator);
    }

    CardProvider<? extends Card> cardProvider = new CardProvider<>(cardFactory, validator, handler);
    cardProviderResolver.put(discriminator, cardProvider);

    postRegistration(cardProvider);
  }

  public UserCard create(String cardDiscriminator) {
    Card card = createInstance(cardDiscriminator);
    cardRepository.persist(card);
    return UserCard.of(card);
  }

  public boolean pass(UserCard userCard) {
    Card card = getCard(userCard);
    ValidationResult validationResult = new ValidationResult();

    applyValidator(card, validationResult);
    applyPostValidationListeners(card, validationResult);
    if (validationResult.hasErrors()) {
      return false;
    }

    applyHandler(card);
    applyPostHandleListeners(card);
    return true;
  }

  public void block(UserCard userCard) {
    setBlock(userCard, true);
  }

  public void unblock(UserCard userCard) {
    setBlock(userCard, false);
  }

  public void addPostPassHandleListener(PostHandleListener listener) {
    postHandleListeners.add(listener);
  }

  public void addPostValidationRejectionListener(PostValidationListener listener) {
    postValidationListeners.add(listener);
  }

  private void setBlock(UserCard userCard, boolean block) {
    Card card = getCard(userCard);
    cardRepository.get(card.getId()).setBlocked(block);
  }

  private Card createInstance(String cardType) {
    return getProvider(cardType).getCardFactory().create();
  }

  private void postRegistration(CardProvider<? extends Card> provider) {
    Validator<? extends Card> validator = provider.getValidator();
    if (validator instanceof HolidayResolverAware) {
      ((HolidayResolverAware) validator).setHolidayResolver(holidayResolver);
    }
  }

  @SuppressWarnings("unchecked")
  private void applyValidator(Card card, ValidationResult validationResult) {
    String cardDiscriminator = card.getDiscriminator();
    getProvider(cardDiscriminator).getValidator().validate(card, validationResult);
  }

  @SuppressWarnings("unchecked")
  private void applyHandler(Card card) {
    String cardDiscriminator = card.getDiscriminator();
    getProvider(cardDiscriminator).getHandler().handle(card);
  }

  private void applyPostValidationListeners(Card card, ValidationResult validationResult) {
    postValidationListeners.parallelStream().forEach(l -> l.postValidation(card, validationResult));
  }

  private void applyPostHandleListeners(Card card) {
    postHandleListeners.parallelStream().forEach(l -> l.postHandle(card));
  }

  private CardProvider getProvider(String cardType) {
    return Optional.ofNullable(cardProviderResolver.get(cardType)).orElseThrow(() -> new UnregisteredCardTypeException(cardType));
  }

  private Card getCard(UserCard userCard) {
    return cardRepository.get(userCard.getId());
  }

  private void initPostListeners() {
    addPostValidationRejectionListener(new ValidationRejectionLogger(this.passEventRepository));
    addPostPassHandleListener(new HandleLogger(this.passEventRepository));
  }
}
