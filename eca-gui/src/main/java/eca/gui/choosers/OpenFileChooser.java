/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.choosers;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Roman Batygin
 */
public abstract class OpenFileChooser {

    private JFileChooser chooser = new JFileChooser();

    protected OpenFileChooser() {
        chooser.setCurrentDirectory(new File("."));
        chooser.setAcceptAllFileFilterUsed(false);
    }

    public File openFile(Component parent) {
        return (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFile() : null);
    }

    public JFileChooser getChooser() {
        return chooser;
    }
}
