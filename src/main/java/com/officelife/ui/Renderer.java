package com.officelife.ui;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.officelife.World;
import com.officelife.actors.Actor;
import com.officelife.common.Pair;
import com.officelife.items.Item;

public class Renderer {

    private static final TerminalSize windowSize = new TerminalSize(75, 20);
    private Pair<Integer, Integer> viewOffset =
        new Pair<>(-windowSize.getColumns() / 2, -windowSize.getRows() / 2);

    private final char[][] buffer;
    private final GUI gui;

    public Renderer() throws IOException {
        buffer = new char[windowSize.getRows()][];
        for (int y = 0; y < windowSize.getRows(); y++) {
            buffer[y] = new char[windowSize.getColumns()];
        }
        clearBuffer();
        gui = new GUI(createComponentRenderer(windowSize));
    }

    public GUI getGUI() {
        return gui;
    }

    private void clearBuffer() {
        for (int y = 0; y < windowSize.getRows(); y++) {
            for (int x = 0; x < windowSize.getColumns(); x++) {
                buffer[y][x] = ' ';
            }
        }
    }

    private String bufferToString() {
        String s = "";
        for (int y = 0; y < windowSize.getRows(); y++) {
            for (int x = 0; x < windowSize.getColumns(); x++) {
                s += buffer[y][x];
            }
            s += '\n';
        }
        return s;
    }

    public String renderText(World state) {
        clearBuffer();

        for (Actor actor : state.actors.values()) {
            Pair<Integer, Integer> location;
            try {
                location = state.actorLocation(actor.id());
            } catch (Exception e) {
                throw new RuntimeException("Unable to get actor location", e);
            }
            char[][] representation = actor.textRepresentation();
            for (int y = 0; y < representation.length; y++) {
                for (int x = 0; x < representation[y].length; x++) {
                    char rep = representation[y][x];
                    Pair<Integer, Integer> repLocation = new Pair<>(location.first + x - viewOffset.first,
                        location.second + y - viewOffset.second);
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
            char[][] representation = item.textRepresentation();
            for (int y = 0; y < representation.length; y++) {
                for (int x = 0; x < representation[y].length; x++) {
                    char rep = representation[y][x];
                    Pair<Integer, Integer> repLocation = new Pair<>(location.first + x - viewOffset.first,
                        location.second + y - viewOffset.second);
                    if (inView(repLocation)) {
                        buffer[repLocation.second][repLocation.first] = rep;
                    }
                }
            }
        }

        return bufferToString();
    }

    boolean inView(Pair<Integer, Integer> location) {
        System.out.println(location.first + ", " + location.second);
        if (location.first >= 0 && location.second >= 0 &&
            location.first < windowSize.getColumns() && location.second < windowSize.getRows()) {
            return true;
        }
        return false;
    }

    public void render(World state) {
        // Called for side effect
        // TODO separate effects?
        renderText(state);
    }

    private ComponentRenderer<Panel> createComponentRenderer(TerminalSize screenSize) {
        return new ComponentRenderer<Panel>() {
            @Override
            public TerminalSize getPreferredSize(Panel component) {
                // TODO this should be the size of the game board.
                // For now it's just a size that fits in the terminal.
                TerminalSize buffer = new TerminalSize(1, 1);
                return new TerminalSize(screenSize.getColumns() - buffer.getColumns(),
                    screenSize.getRows() - buffer.getRows());
            }

            @Override
            public void drawComponent(TextGUIGraphics graphics, Panel component) {
                for (int y = 0; y < windowSize.getRows(); y++) {
                    for (int x = 0; x < windowSize.getColumns(); x++) {
                        graphics.setCharacter(x, y, Renderer.this.buffer[y][x]);
                    }
                }
            }
        };
    }
}
