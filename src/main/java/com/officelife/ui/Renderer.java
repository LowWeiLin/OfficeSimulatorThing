package com.officelife.ui;

import java.io.IOException;
import java.util.Optional;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import com.officelife.Coords;
import com.officelife.World;
import com.officelife.actors.Actor;
import com.officelife.items.Item;

public class Renderer {

    private static final TerminalSize windowSize = new TerminalSize(75, 20);
    private Coords viewOffset =
        new Coords(-windowSize.getColumns() / 2, -windowSize.getRows() / 2);

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

        for (Item item : state.items.values()) {
            Optional<Coords> location = state.itemLocation(item.id());;

            if (!location.isPresent()) {
                // it's possible the item may be in someone's inventory
                continue;
            }
            renderRep(location.get(), item.textRepresentation());
        }

        // actors take precedence when rendering
        for (Actor actor : state.actors.values()) {
            Optional<Coords> location = state.actorLocation(actor.id());
            if (!location.isPresent()) {
                System.err.println("could not render actor " + actor.id());
                continue;
            }
            renderRep(location.get(), actor.textRepresentation());
        }

        return bufferToString();
    }

    private void renderRep(Coords loc, char[][] representation) {
        for (int y = 0; y < representation.length; y++) {
            for (int x = 0; x < representation[y].length; x++) {
                char rep = representation[y][x];
                Coords repLocation = new Coords(loc.x + x - viewOffset.x,
                    loc.y + y - viewOffset.y);
                if (inView(repLocation)) {
                    buffer[repLocation.y][repLocation.x] = rep;
                }
            }
        }
    }

    boolean inView(Coords location) {
        if (location.x >= 0 && location.y >= 0 &&
            location.x < windowSize.getColumns() && location.y < windowSize.getRows()) {
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
