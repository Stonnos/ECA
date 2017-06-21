/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.frames;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import eca.gui.ButtonUtils;
import eca.jdbc.DataBaseConnection;
import eca.gui.PanelBorderUtils;
import weka.core.Instances;

/**
 *
 * @author Рома
 */
public class QueryFrame extends JFrame {

    private static final String dbTitle = "База данных";
    private static final String urlTitle = "URL:";
    private static final String userTitle = "User:";
    private static final String queryTitle = "SELECT запрос";

    private final DataBaseConnection connection;

    private JTextArea queryArea;
    private JProgressBar progress;
    private JList<String> sets;
    private ListModel model;
    private JButton execute;
    private JButton interrupt;

    private SwingWorkerConstruction worker;

    private JMainFrame parent;

    public QueryFrame(JMainFrame parent, DataBaseConnection connection) {
        this.parent = parent;
        this.connection = connection;
        this.setLayout(new GridBagLayout());
        this.setIconImage(parent.getIconImage());
        this.setResizable(false);
        this.makeGUI();
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                interruptWorker();
                closeConnection();
            }

            @Override
            public void windowClosed(WindowEvent evt) {
                closeConnection();
            }

        });
        //-----------------------------------
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    public java.util.ArrayList<Instances> instances() {
        int[] indices = sets.getSelectedIndices();
        ArrayList<Instances> result = new ArrayList<>(indices.length);
        for (int i : indices) {
            result.add(model.instance(i));
        }
        return result;
    }

    private void interruptWorker() {
        if (worker != null && !worker.isCancelled()) {
            worker.cancel(true);
        }
    }

    private void closeConnection() {
        try {
            connection.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void makeGUI() {
        JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paramPanel.setBorder(PanelBorderUtils.createTitledBorder(dbTitle));
        JTextField url = new JTextField(30);
        url.setText(connection.getConnectionDescriptor().getUrl());
        url.setBackground(Color.WHITE);
        url.setCaretPosition(0);
        url.setEditable(false);
        JTextField user = new JTextField(10);
        user.setText(connection.getConnectionDescriptor().getLogin());
        user.setBackground(Color.WHITE);
        user.setEditable(false);
        paramPanel.add(new JLabel(urlTitle));
        paramPanel.add(url);
        paramPanel.add(new JLabel(userTitle));
        paramPanel.add(user);
        //-----------------------------------------------------
        JPanel queryPanel = new JPanel(new GridBagLayout());
        queryPanel.setBorder(PanelBorderUtils.createTitledBorder(queryTitle));
        queryArea = new JTextArea(10, 20);
        queryArea.setWrapStyleWord(true);
        queryArea.setLineWrap(true);
        queryArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPanel = new JScrollPane(queryArea);
        execute = new JButton("Выполнить");
        JButton clear = new JButton("Очистить");
        interrupt = new JButton("Прервать");
        interrupt.setEnabled(false);
        //-----------------------------------------
        execute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                progress.setIndeterminate(true);
                worker = new SwingWorkerConstruction(queryArea.getText());
                interrupt.setEnabled(true);
                worker.execute();
            }
        });

        interrupt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                interruptWorker();
            }
        });

        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                queryArea.setText("");
            }
        });
        //-----------------------------------------
        Dimension dim = new Dimension(150, 25);
        execute.setPreferredSize(dim);
        clear.setPreferredSize(dim);
        interrupt.setPreferredSize(dim);
        queryPanel.add(scrollPanel, new GridBagConstraints(0, 0, 3, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        queryPanel.add(execute, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 0, 3, 3), 0, 0));
        queryPanel.add(clear, new GridBagConstraints(1, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 0, 0));
        queryPanel.add(interrupt, new GridBagConstraints(2, 1, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 0), 0, 0));
        //-----------------------------------------------------
        progress = new JProgressBar();
        //-----------------------------------------------------
        model = new ListModel();
        sets = new JList<>(model);
        JScrollPane setsPane = new JScrollPane(sets);
        setsPane.setBorder(PanelBorderUtils.createTitledBorder("Данные"));
        setsPane.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sets.addMouseListener(new MouseAdapter() {

            InstancesFrame instancesFrame;

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int i = sets.locationToIndex(e.getPoint());
                    instancesFrame = new InstancesFrame(model.instance(i), QueryFrame.this);
                    instancesFrame.setVisible(true);
                }
            }

        });
        //-----------------------------------------------------
        JButton okButton = ButtonUtils.createOkButton();
        JButton cancelButton = ButtonUtils.createCancelButton();

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });
        //-----------------------------------------------
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                interruptWorker();
                if (sets.getSelectedIndices().length != 0) {
                    try {
                        for (Instances ins : instances()) {
                            parent.createDataFrame(ins);
                        }
                        dispose();
                    }
                    catch (Throwable ex) {
                        JOptionPane.showMessageDialog(parent,
                                ex.getMessage(),
                                "", JOptionPane.WARNING_MESSAGE);
                    }

                } else {
                    JOptionPane.showMessageDialog(QueryFrame.this,
                            "Необходимо сформировать выборку и выбрать ее в списке!",
                            "", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        //-----------------------------------------------------
        this.add(paramPanel, new GridBagConstraints(0, 0, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 0));
        this.add(queryPanel, new GridBagConstraints(0, 1, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 0));
        this.add(progress, new GridBagConstraints(0, 2, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 0));
        this.add(setsPane, new GridBagConstraints(0, 3, 2, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 0));
        this.add(okButton, new GridBagConstraints(0, 4, 1, 1, 1, 1,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 8, 3), 0, 0));
        this.add(cancelButton, new GridBagConstraints(1, 4, 1, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 3, 8, 0), 0, 0));
        //-----------------------------------------------
        this.getRootPane().setDefaultButton(okButton);
    }

    /**
     *
     */
    private static class ListModel extends DefaultListModel<String> {

        ArrayList<Instances> instances = new ArrayList<>();

        public Instances instance(int i) {
            return instances.get(i);
        }

        public void addInstances(Instances data) {
            instances.add(data);
            this.addElement(data.relationName());
        }

    } //End of class ListModel

    /**
     *
     */
    private class SwingWorkerConstruction extends SwingWorker<Void, Void> {

        String query;
        Instances data;
        String errorMessage;

        SwingWorkerConstruction(String query) {
            this.query = query;
        }

        @Override
        protected Void doInBackground() {
            try {
                execute.setEnabled(false);
                data = connection.executeQuery(query);
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = e.getMessage();
            }
            return null;
        }

        @Override
        protected void done() {
            progress.setIndeterminate(false);
            execute.setEnabled(true);
            interrupt.setEnabled(false);
            if (data != null) {
                model.addInstances(data);
                sets.setSelectedIndex(0);
            }
            else {
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(QueryFrame.this,
                            errorMessage,
                            "", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

    } //End of class SwingWorkerConstruction

}
