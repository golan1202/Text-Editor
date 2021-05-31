package editor;

public class ApplicationRunner {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TextEditor frame = new TextEditor();
            frame.setVisible(true);
        });
    }
}