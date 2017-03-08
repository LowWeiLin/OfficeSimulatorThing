package com.officelife.ui;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class GUI {

    private MultiWindowTextGUI gui;
    private BasicWindow window;
    private Function<String, String> replHandler = s -> "";
    private Supplier<Boolean> pauseHandler = () -> false;
    private boolean replShowing = false;
    private TextBox resultField;
    private TextBox replField;
    private Panel replPanel;

    GUI(ComponentRenderer<Panel> gameRenderer) throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        final Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(1));

        Panel gamePanel = new Panel();

        panel.addComponent(gamePanel);

        int replSize = 74;
        resultField = new TextBox(new TerminalSize(replSize, 1));
        resultField.setReadOnly(true);

        panel.addComponent(replPanel = new Panel());
        panel.addComponent(resultField);

        replField = new TextBox(new TerminalSize(replSize, 1)) {
            @Override
            public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
                switch (keyStroke.getKeyType()) {
                    // TODO history?
                    case Enter:
                        if (!getText().isEmpty()) {
                            resultField.setText(replHandler.apply(getText()));
                            setText("");
                        }
                        return Result.HANDLED;
                    case Escape:
                        toggleRepl();
                        return Result.HANDLED;
                }
                return super.handleKeyStroke(keyStroke);
            }
        };

        window = new BasicWindow();
        window.setComponent(panel);
        window.addWindowListener(new WindowListener() {
            @Override
            public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
                switch (keyStroke.getKeyType()) {
                    case Character:
                        if (keyStroke.getCharacter() == ' ') {
                          pause();
                        }
                        break;
                    case Escape:
                        toggleRepl();
                        break;
                }
            }

            @Override
            public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
            }

            @Override
            public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
            }

            @Override
            public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
            }
        });

        gui = new MultiWindowTextGUI(screen,
            new DefaultWindowManager(),
            new EmptySpace(TextColor.ANSI.BLACK));

        gamePanel.setRenderer(gameRenderer);
    }

    private void toggleRepl() {
        if (replShowing) {
            replPanel.removeComponent(replField);
        } else {
            replPanel.addComponent(replField);
            replField.takeFocus();
        }
        replShowing = !replShowing;
    }

    private void pause() {
        if (pauseHandler.get()) {
            resultField.setText("Paused");
        } else {
            resultField.setText("Unpaused");
        }
    }

    public GUI onRepl(Function<String, String> action) {
        this.replHandler = action;
        return this;
    }

    public GUI onPause(Supplier<Boolean> action) {
        this.pauseHandler = action;
        return this;
    }

    /**
     * This blocks
     */
    public void start() {
        gui.addWindowAndWait(window);
    }

    public void runAndWait(Runnable action) {
        try {
            gui.getGUIThread().invokeAndWait(action);
        } catch (InterruptedException e) {
            // TODO better logging
            System.err.println("Could not run action");
            e.printStackTrace();
        }
    }
}
