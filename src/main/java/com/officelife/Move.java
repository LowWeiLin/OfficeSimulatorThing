package com.officelife;

/**
 * Movement action. Mutates the location of an actor in 4 possible directions.
 */
public class Move implements Action {
    enum Direction {
        UP, DOWN, LEFT, RIGHT;

        public static final Direction directionToMove(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
            if (to.first > from.first) {
                return Move.Direction.RIGHT;
            } else if (to.first < from.first) {
                return Move.Direction.LEFT;
            } else if (to.second < from.second) {
                return Move.Direction.DOWN;
            } else if (to.second > from.second) {
                return Move.Direction.UP;
            } else {
                throw new RuntimeException("Invalid direction logic");
            }
        }

    }
    private final Actor actor;

    private final Direction direction;

    public Move(Actor actor, Direction direction) {
        this.actor = actor;
        this.direction = direction;
    }

    @Override
    public void accept(World world) {
        try {
            Pair<Integer, Integer> old = world.actorLocation(actor.id());
            Pair<Integer, Integer> updated = updatedLocation(old);

            if (world.locationActor.containsKey(updated)) {
                // if banging against something
                // do not change location
                return;
            }
            world.locationActor.remove(old);
            world.locationActor.put(updated, actor.id());

            System.err.println("Moved");
        } catch (Exception e) {
            throw new RuntimeException("Unable to update", e);
        }

    }

    private Pair<Integer, Integer> updatedLocation(Pair<Integer, Integer> location) throws Exception {
        switch (direction) {
            case UP:
                return new Pair<>(location.first, location.second + 1);

            case DOWN:
                return new Pair<>(location.first, location.second - 1);

            case LEFT:
                return new Pair<>(location.first - 1, location.second);

            case RIGHT:
                return new Pair<>(location.first + 1, location.second);

            default:
                throw new Exception("bad direction");
        }
    }
}
