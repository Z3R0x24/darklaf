/*
 * MIT License
 *
 * Copyright (c) 2021 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.darklaf.ui.togglebutton;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class ToggleButtonFocusNavigationActions {

    private final KeyListener keyListener;
    private final AbstractButton button;
    private boolean traversalKeysEnabled;

    public ToggleButtonFocusNavigationActions(final AbstractButton button) {
        this.button = button;
        keyListener = new DarkToggleButtonKeyHandler();
    }

    public void installActions() {
        if (button == null) return;
        traversalKeysEnabled = button.getFocusTraversalKeysEnabled();
        button.setFocusTraversalKeysEnabled(false);
        button.addKeyListener(keyListener);
        button.getActionMap().put("Previous", new SelectPreviousBtn());
        button.getActionMap().put("Next", new SelectNextBtn());

        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("UP"), "Previous");
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DOWN"), "Next");
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("LEFT"),
                "Previous");
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("RIGHT"), "Next");
    }

    public void uninstallActions() {
        if (button == null) return;
        if (!button.getFocusTraversalKeysEnabled()) {
            button.setFocusTraversalKeysEnabled(traversalKeysEnabled);
        }
        button.removeKeyListener(keyListener);

        button.getActionMap().remove("Previous");
        button.getActionMap().remove("Next");
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke("UP"));
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke("DOWN"));
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke("LEFT"));
        button.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke("RIGHT"));
    }

    protected static void selectToggleButton(final ActionEvent event, final boolean next) {
        // Get the source of the event.
        Object eventSrc = event.getSource();

        // Check whether the source is AbstractButton, it so, whether it is visible
        if (!ButtonGroupInfo.isValidButton(eventSrc)) {
            return;
        }

        ButtonGroupInfo btnGroupInfo = new ButtonGroupInfo((AbstractButton) eventSrc);
        btnGroupInfo.selectNewButton(next);
    }

    public static class SelectPreviousBtn extends AbstractAction {
        public SelectPreviousBtn() {
            super("Previous");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            selectToggleButton(e, false);
        }
    }

    public static class SelectNextBtn extends AbstractAction {
        public SelectNextBtn() {
            super("Next");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            selectToggleButton(e, true);
        }
    }
}
