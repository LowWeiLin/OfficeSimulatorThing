package com.officelife.actors;

import java.util.*;
import java.util.stream.Collectors;

import com.officelife.World;
import com.officelife.actions.*;
import com.officelife.characteristics.Characteristic;
import com.officelife.commodity.Commodity;
import com.officelife.commodity.FakeWork;
import com.officelife.common.Pair;
import com.officelife.items.Coffee;
import com.officelife.items.CoffeeMachine;
import com.officelife.items.Item;
import com.officelife.knowledge.KnowledgeBase;
import com.officelife.locations.Cubicle;
import com.officelife.locations.LocationTrait;

public class Person implements Actor {
    private final String id;
    private final String name;

    private EnumMap<ActorState, Integer> needs;

    private Map<Class<? extends Item>, List<Item>> inventory;

    public Collection<Characteristic> characteristics;

    public Person(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;

        this.needs = new EnumMap<>(ActorState.class);
        for (ActorState need : ActorState.values()) {
            this.needs.put(need, 0);
        }

        this.inventory = new HashMap<>();
    }

    @Override
    public String id() {
        return id;
    }

    public Commodity commodityWanted() {
        return new FakeWork();
    }

    public Action actionToTake(World state) throws IllegalAccessException, InstantiationException {
        Commodity commodity = commodityWanted();
        Class<? extends Action> actionType = KnowledgeBase.actionProducing(commodity);

        return fulfilRequirementsToTakeAction(actionType, state);
    }

    // TODO factory thing ):
    private Action initialiseWithoutParameters(Class<? extends Action> actionType,
                                                      Pair<Integer, Integer> actorLocation,
                                                      World state, List<Item> items) {
        if (actionType == DrinkCoffee.class) {
            return new DrinkCoffee(this, (Coffee) items.get(0));
        } else if (actionType == UseCoffeeMachine.class) {
            return new UseCoffeeMachine(this, (CoffeeMachine) items.get(0));
        } else if (actionType == PretendToWork.class) {
            return new PretendToWork(this, (Cubicle) state.locationTrait(actorLocation));
        }
        try {
            return actionType.newInstance();
        } catch (InstantiationException | IllegalAccessException  e) {
            throw new RuntimeException("Failed to instantiate " + actionType, e);
        }
    }


    public static Optional<Item> itemAtActorLocation(Pair<Integer, Integer> actorLocation, World state) {
        if (!state.locationItems.containsKey(actorLocation)) {
            return Optional.empty();
        }
        String itemId = state.locationItems.get(actorLocation);
        return Optional.of(state.items.get(itemId));
    }

    public static LocationTrait locationTraitAtActorLocation(Pair<Integer, Integer> actorLocation, World state) {
        return state.locationTrait(actorLocation);
    }

    public Action fulfilRequirementsToTakeAction(Class<? extends Action> actionType, World state) {
        Pair<Integer, Integer> actorLocation;
        try {
            actorLocation = state.actorLocation(id);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get actor location", e);
        }

        Collection<Class<? extends Item>> itemClasses = KnowledgeBase.itemsRequiredForAction(actionType);
        Optional<Item> itemAtActorLocation = itemAtActorLocation(actorLocation, state);

        List<Item> items = new ArrayList<>();
        boolean hasSufficientItems = itemClasses.stream()
                .allMatch(itemClass ->
                        inventoryContainsItemClass(itemClass)
                        || (itemIsOfItemClass(itemAtActorLocation, itemClass))
                );

        Class<? extends LocationTrait> locationTraitClass =
                KnowledgeBase.locationTraitsRequiredForAction(actionType);
        boolean isInRightLocation = locationTraitAtActorLocation(actorLocation, state).getClass().equals(locationTraitClass);
        
        if (hasSufficientItems && isInRightLocation) {
            List<Item> itemsFromInventoryAndFloor = itemClasses.stream()
                    .map(itemClass -> {
                        if (itemIsOfItemClass(itemAtActorLocation, itemClass)) {
                            return itemAtActorLocation.get();
                        }
                        if (inventoryContainsItemClass(itemClass)) {
                            return inventory.get(itemClass).get(0);
                        }
                        throw new RuntimeException("Logic error");
                    })
                    .collect(Collectors.toList());
            items.addAll(itemsFromInventoryAndFloor);

            return initialiseWithoutParameters(actionType, actorLocation, state, items);
        } else if (!hasSufficientItems) {
            Class<? extends Item> itemTypeToAcquire = determineItemToObtain(actionType)
                    .orElseThrow(() -> new RuntimeException("No item to get!"));
            Optional<Item> itemToAcquire = state.itemWithClass(itemTypeToAcquire);
            // if item exist in the world
            // try to take it by moving to it
            if (state.items.values().stream()
                    .anyMatch(item -> itemToAcquire.isPresent() && item.getClass() == itemToAcquire.get().getClass())) {

                Item actualItemToAcquire = itemToAcquire.get();
                Pair<Integer, Integer> itemLocation;
                try {
                    itemLocation = state.itemLocation(actualItemToAcquire.id());
                } catch (Exception e) {
                    throw new RuntimeException("Unable to get item location", e);
                }

                return new Move(this, Move.Direction.directionToMove(actorLocation, itemLocation));
            }
            Class<? extends Action> makeItemAction = KnowledgeBase.actionProducing(itemTypeToAcquire);
            return fulfilRequirementsToTakeAction(makeItemAction, state);
        } else if (!isInRightLocation) {
            Pair<Integer, Integer> traitLocation;
            try {
                traitLocation = state.locationTraitLocation(locationTraitClass);
            } catch (Exception e) {
                throw new RuntimeException("Unable to get item location", e);
            }
            return new Move(this, Move.Direction.directionToMove(actorLocation, traitLocation));
        }
        throw new RuntimeException("No action");
    }

    private boolean itemIsOfItemClass(Optional<Item> itemAtActorLocation, Class<? extends Item> itemClass) {
        return itemAtActorLocation.isPresent()
                && itemAtActorLocation.get().getClass() == itemClass;
    }

    private boolean inventoryContainsItemClass(Class<? extends Item> itemClass) {
        return inventory.containsKey(itemClass)
            && !inventory.get(itemClass).isEmpty();
    }

    private Optional<Class<? extends Item>> determineItemToObtain(Class<? extends Action> actionWanted) {
        Collection<Class<? extends Item>> itemClasses = KnowledgeBase.itemsRequiredForAction(actionWanted);

        return itemClasses.stream()
                .filter(itemClass -> !inventory.containsKey(itemClass) || inventory.get(itemClass).isEmpty())
                .findFirst();
    }

    @Override
    public Action act(World state) {
        try {
            return actionToTake(state);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public void changeNeed(ActorState need, int value) {
        int current = needs.get(need);
        needs.put(need, current + value);

        System.err.println(need + " changed " + value);
        System.err.println(need + " is now " + needs.get(need));
    }

    @Override
    public void addItem(Item item) {
        Class<? extends Item> itemClass = item.getClass();
        if (!inventory.containsKey(itemClass)) {
            inventory.put(itemClass, new ArrayList<>());
        }
        inventory.get(itemClass).add(item);
    }

    @Override
    public void removeItem(Item item) {
        inventory.get(item.getClass()).remove(item);
    }

    @Override
    public char[][] textRepresentation() {
        return new char[][]{{'P'}};
    }
}
