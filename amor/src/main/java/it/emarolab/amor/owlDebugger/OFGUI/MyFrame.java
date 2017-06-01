package it.emarolab.amor.owlDebugger.OFGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;


@SuppressWarnings("serial")
public class MyFrame extends JFrame {    
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                // nothing
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                if( KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()
                        .equals( ClassExchange.getClassRootObj().getEditor().getEditorComponent())){
                    ClassRootManager.updateComboBox( e);
                } else if( KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()
                        .equals( ClassExchange.getFindItemObj().getEditor().getEditorComponent())){
                    ClassFindManager.updateComboBox( e);
                }
            } else if (e.getID() == KeyEvent.KEY_TYPED) {
                // nothing
            }
            return false;
        }
    }
    
    
    public MyFrame( String title) {
        this.setTitle( title);
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
    }
}
