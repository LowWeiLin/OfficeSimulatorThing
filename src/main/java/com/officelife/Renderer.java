package com.officelife;


public class Renderer {
    private static final Pair<Integer, Integer> windowSize = new Pair<>(75, 20);
    private char[][] buffer;
    private Pair<Integer, Integer> viewOffset = new Pair<>(-windowSize.first/2,-windowSize.second/2);

    public Renderer() {
        buffer = new char[windowSize.second][];
        for (int y=0 ; y<windowSize.second ; y++) {
            buffer[y] = new char[windowSize.first];
        }
        clearBuffer();
    }

    void clearBuffer() {
        for (int y=0 ; y<windowSize.second ; y++) {
            for (int x=0 ; x<windowSize.first ; x++) {
                buffer[y][x] = ' ';
            }
        }
    }

    String bufferToString(char[][] buf) {
        String s = "";
        for (int y=0 ; y<windowSize.second ; y++) {
            for (int x=0 ; x<windowSize.first ; x++) {
                s += buffer[y][x];
            }
            s += '\n';
        }
        return s;
    }

    String render(World state) {
        clearBuffer();

        for (Actor actor : state.actors.values()) {
            Pair<Integer, Integer> location;
            try {
                location = state.actorLocation(actor.id());
            } catch (Exception e) {
                throw new RuntimeException("Unable to get actor location", e);
            }
            char[][] representation = actor.asciiRepresentation();
            for (int y = 0 ; y<representation.length ; y++) {
                for (int x = 0 ; x<representation[y].length ; x++) {
                    char rep = representation[y][x];
                    Pair<Integer, Integer> repLocation = new Pair<>(location.first + x - viewOffset.first ,
                                                                    location.second + y - viewOffset.second );
                    if (inView(repLocation)) {
                        buffer[repLocation.second][repLocation.first] = rep;
                    }
                }
            }
        }

        for (Item item : state.items.values()) {
            Pair<Integer, Integer> location;
            try {
                location = state.itemLocation(item.id());
            } catch (Exception e) {
                throw new RuntimeException("Unable to get item location", e);
            }
            char[][] representation = item.asciiRepresentation();
            for (int y = 0 ; y<representation.length ; y++) {
                for (int x = 0 ; x<representation[y].length ; x++) {
                    char rep = representation[y][x];
                    Pair<Integer, Integer> repLocation = new Pair<>(location.first + x - viewOffset.first ,
                            location.second + y - viewOffset.second );
                    if (inView(repLocation)) {
                        buffer[repLocation.second][repLocation.first] = rep;
                    }
                }
            }
        }

        return bufferToString(buffer);
    }

    boolean inView(Pair<Integer, Integer> location) {
        System.out.println(location.first + ", " + location.second);
        if (location.first >= 0 && location.second >= 0 &&
                location.first < windowSize.first && location.second < windowSize.second) {
            return true;
        }
        return false;
    }
}
