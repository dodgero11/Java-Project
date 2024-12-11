package gui_user;

import javax.swing.*;

public class MainFrame extends JFrame {
    
    public MainFrame() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new LoginPanel());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setVisible(true);
    }
}