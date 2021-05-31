package editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    private boolean isTextChanged = false;
    private String text;
    private String searchText;
    private final List<Integer> searchResultIndexes = new ArrayList<>();
    private final List<Integer> searchResultLength = new ArrayList<>();
    private int iterator = 0;
    private boolean useRegex = false;

    public TextEditor() {
        super();
        createGUI();
    }

    private void createGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Simple Text Editor");
        setSize(600, 700);
        setLocation(600, 200);
        Font font = new Font("Courier", Font.PLAIN, 16);


        JTextArea textArea = new JTextArea();
        textArea.setName("TextArea");
        textArea.setFont(font);
        textArea.getDocument().addDocumentListener(new MyDocumentListener());

        JScrollPane scrollableTextArea = new JScrollPane(textArea);
        scrollableTextArea.setName("ScrollPane");

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setName("FileChooser");
        jfc.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT files", "txt");
        jfc.addChoosableFileFilter(filter);
        add(jfc);

        JButton openButton = new JButton(new ImageIcon("Text Editor/task/src/res/openIcon.png"));
        openButton.setName("OpenButton");
        openButton.setPreferredSize(new Dimension(38, 38));
        openButton.addActionListener(event -> {
            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    String dataFromFile = new String(Files.readAllBytes(Paths.get(jfc.getSelectedFile().getAbsolutePath())));
                    textArea.setText(dataFromFile);
                } catch (IOException ioException) {
                    System.out.println("Error: " + ioException.getMessage());
                    textArea.setText("");
                }
            }
        });

        JButton saveButton = new JButton(new ImageIcon("Text Editor/task/src/res/saveIcon.png"));
        saveButton.setName("SaveButton");
        saveButton.setPreferredSize(new Dimension(38, 38));
        saveButton.addActionListener(event -> {
            int returnValue = jfc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String filePath = jfc.getSelectedFile().getAbsolutePath();
                if (!".txt".equals(filePath.substring(filePath.length() - 4))) {
                    filePath += ".txt";
                }
                File targetFile = new File(filePath);
                String dataToFile = textArea.getText();
                try (FileWriter writer = new FileWriter(targetFile)) {
                    writer.write(dataToFile);
                } catch (IOException ioException) {
                    System.out.println("Error: " + ioException.getMessage());
                }
            }
        });

        JTextField searchField = new JTextField();
        searchField.setName("SearchField");
        searchField.setFont(font.deriveFont(18f));
        searchField.getDocument().addDocumentListener(new MyDocumentListener());

        class TextFinder extends SwingWorker<Object, Object> {

            @Override
            protected Object doInBackground() {
                searchText = searchField.getText();
                if (!"".equals(searchText)) {
                    if (isTextChanged) {
                        text = textArea.getText();
                        if (useRegex) {
                            Pattern pattern = Pattern.compile(searchText);
                            Matcher matcher = pattern.matcher(text);
                            while (matcher.find()) {
                                searchResultIndexes.add(matcher.start());
                                searchResultLength.add(matcher.end() - matcher.start());
                            }
                        } else {
                            int occurrenceIndex = text.indexOf(searchText);
                            int indexForSubString = occurrenceIndex;
                            while (indexForSubString != -1) {
                                searchResultIndexes.add(occurrenceIndex);
                                searchResultLength.add(searchText.length());
                                String searchSubString = text.substring(occurrenceIndex + searchText.length());
                                indexForSubString = searchSubString.indexOf(searchText);
                                occurrenceIndex = indexForSubString + text.length() - searchSubString.length();
                            }
                        }
                        isTextChanged = false;
                    }
                    if (!searchResultIndexes.isEmpty()) {
                        int index = searchResultIndexes.get(iterator = 0);
                        textArea.setCaretPosition(index + searchResultLength.get(iterator));
                        textArea.select(index, index + searchResultLength.get(iterator));
                        textArea.grabFocus();
                    }
                }

                return null;
            }
        }

        JButton searchButton = new JButton(new ImageIcon("Text Editor/task/src/res/searchIcon.png"));
        searchButton.setName("StartSearchButton");
        searchButton.setPreferredSize(new Dimension(38, 38));
        searchButton.addActionListener(event -> (new TextFinder()).execute());

        JButton prevMatchButton = new JButton(new ImageIcon("Text Editor/task/src/res/prevMatchIcon.png"));
        prevMatchButton.setName("PreviousMatchButton");
        prevMatchButton.setPreferredSize(new Dimension(38, 38));
        prevMatchButton.addActionListener(event -> {
            if (!searchResultIndexes.isEmpty()) {
                int index;
                if (iterator - 1 < 0) {
                    iterator = searchResultIndexes.size();
                }
                index = searchResultIndexes.get(--iterator);
                textArea.setCaretPosition(index + searchResultLength.get(iterator));
                textArea.select(index, index + searchResultLength.get(iterator));
                textArea.grabFocus();
            }
        });

        JButton nextMatchButton = new JButton(new ImageIcon("Text Editor/task/src/res/nextMatchIcon.png"));
        nextMatchButton.setName("NextMatchButton");
        nextMatchButton.setPreferredSize(new Dimension(38, 38));
        nextMatchButton.addActionListener(event -> {
            if (!searchResultIndexes.isEmpty()) {
                int index;
                if (iterator + 1 == searchResultIndexes.size()) {
                    iterator = -1;
                }
                index = searchResultIndexes.get(++iterator);
                textArea.setCaretPosition(index + searchResultLength.get(iterator));
                textArea.select(index, index + searchResultLength.get(iterator));
                textArea.grabFocus();
            }
        });

        JCheckBox useRegexBox = new JCheckBox("Use regex");
        useRegexBox.setName("UseRegExCheckbox");
        useRegexBox.setFont(font.deriveFont(18f));
        useRegexBox.addItemListener(e -> updateCheckbox());

        JPanel filePane = new JPanel();
        filePane.setLayout(new BoxLayout(filePane, BoxLayout.LINE_AXIS));
        filePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filePane.add(openButton);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(saveButton);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(searchField);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(searchButton);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(prevMatchButton);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(nextMatchButton);
        filePane.add(Box.createRigidArea(new Dimension(10, 0)));
        filePane.add(useRegexBox);

        JPanel textPane = new JPanel();
        textPane.setLayout(new BorderLayout());
        textPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        textPane.add(scrollableTextArea);


        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setName("MenuOpen");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(event -> {
            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    String dataFromFile = new String(Files.readAllBytes(Paths.get(jfc.getSelectedFile().getAbsolutePath())));
                    textArea.setText(dataFromFile);
                } catch (IOException ioException) {
                    System.out.println("Error: " + ioException.getMessage());
                    textArea.setText("");
                }
            }
        });

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setName("MenuSave");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.addActionListener(event -> {
            int returnValue = jfc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                String filePath = jfc.getSelectedFile().getAbsolutePath();
                if (!".txt".equals(filePath.substring(filePath.length() - 4))) {
                    filePath += ".txt";
                }
                File targetFile = new File(filePath);
                String dataToFile = textArea.getText();
                try (FileWriter writer = new FileWriter(targetFile)) {
                    writer.write(dataToFile);
                } catch (IOException ioException) {
                    System.out.println("Error: " + ioException.getMessage());
                }
            }
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setName("MenuExit");
        exitMenuItem.setMnemonic(KeyEvent.VK_X);
        exitMenuItem.addActionListener(event -> dispose());

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem startSearchMenuItem = new JMenuItem("Start search");
        startSearchMenuItem.setName("MenuStartSearch");
        startSearchMenuItem.addActionListener(event -> (new TextFinder()).execute());

        JMenuItem prevMatchMenuItem = new JMenuItem("Previous match");
        prevMatchMenuItem.setName("MenuPreviousMatch");
        prevMatchMenuItem.addActionListener(event -> {
            if (!searchResultIndexes.isEmpty()) {
                int index;
                if (iterator - 1 < 0) {
                    iterator = searchResultIndexes.size();
                }
                index = searchResultIndexes.get(--iterator);
                textArea.setCaretPosition(index + searchResultLength.get(iterator));
                textArea.select(index, index + searchResultLength.get(iterator));
                textArea.grabFocus();
            }
        });

        JMenuItem nextMatchMenuItem = new JMenuItem("Next match");
        nextMatchMenuItem.setName("MenuNextMatch");
        nextMatchMenuItem.addActionListener(event -> {
            if (!searchResultIndexes.isEmpty()) {
                int index;
                if (iterator + 1 == searchResultIndexes.size()) {
                    iterator = -1;
                }
                index = searchResultIndexes.get(++iterator);
                textArea.setCaretPosition(index + searchResultLength.get(iterator));
                textArea.select(index, index + searchResultLength.get(iterator));
                textArea.grabFocus();
            }
        });

        JMenuItem useRegexMenuItem = new JMenuItem("Use regular expressions");
        useRegexMenuItem.setName("MenuUseRegExp");
        useRegexMenuItem.addActionListener(event -> {
            useRegexBox.doClick();
            useRegexBox.requestFocusInWindow();
        });

        searchMenu.add(startSearchMenuItem);
        searchMenu.add(prevMatchMenuItem);
        searchMenu.add(nextMatchMenuItem);
        searchMenu.add(useRegexMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(searchMenu);


        add(filePane, BorderLayout.PAGE_START);
        add(textPane, BorderLayout.CENTER);
    }

    private class MyDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            isTextChanged = true;
            searchResultIndexes.clear();
            searchResultLength.clear();
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            isTextChanged = true;
            searchResultIndexes.clear();
            searchResultLength.clear();
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
        }
    }

    private void updateCheckbox() {
        useRegex = !useRegex;
        isTextChanged = true;
        searchResultIndexes.clear();
        searchResultLength.clear();
    }
}