/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.choosers;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Roman Batygin
 */
public class SaveImageFileChooser extends SaveFileChooser {

    public SaveImageFileChooser() {
        getChooser().addChoosableFileFilter(
                new FileNameExtensionFilter("PNG data files (*.png)", "png"));
    }

}
