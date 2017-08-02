/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.choosers;

import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Рома
 */
public class OpenDataFileChooser extends OpenFileChooser {

    public OpenDataFileChooser() {
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Xls data files (*.xls)", "xls"));
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Xls data files (*.xlsx)", "xlsx"));
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Csv data files (*.csv)", "csv"));
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter("Arff data files (*.arff)", "arff"));
    }

}
