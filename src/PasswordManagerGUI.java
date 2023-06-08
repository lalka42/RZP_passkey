import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


public class PasswordManagerGUI extends JFrame {
    private Connection connection;
    private Map<String, Map<String, String>> passwordMap;
    private JTextField accountField;
    private JPasswordField passwordField;
    private JTextField urlField;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private String masterPassword;
    private String masterHash;

    String inputFile = "passwords.db";
    String encryptedFile = "db.crypt";
    String decryptedFile = "passwords.db";
    String key = "IaZaebalsyaPostavte4pliz";

    private boolean rst;
    private long lastMasterPasswordCheckTime;
    private final long MINUTE_IN_MILLIS = 60_000;
    private JLabel infoLabel;

    public PasswordManagerGUI() {
        passwordMap = new HashMap<>();
        CheckHash();
        checkMasterPassword();
        if (rst) {
            dbcrypt dbcrypt = new dbcrypt();
            try {
                dbcrypt.decryptFile(encryptedFile, decryptedFile, key);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Мастер пароль неверен.", "Ошибка", JOptionPane.ERROR_MESSAGE);}

        setTitle("Менеджер паролей");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Удаление файла при закрытии окна
                try {
                    dbcrypt.encryptFile(inputFile, encryptedFile, key);
                } catch (Exception es) {
                    es.printStackTrace();
                }
                File deldb = new File("passwords.db");
                if (deldb.exists()) {
                    deldb.delete();
                    System.out.println("Файл удален");
                }
                dispose();
            }
        });

        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(32, 34, 37));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        //JPanel basedPanel = new JPanel();
        //basedPanel.setLayout(new GridBagLayout());
        //basedPanel.setBackground(new Color(32, 34, 37));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 3, 10, 5));
        inputPanel.setBackground(new Color(32, 34, 37));

        //JPanel buttonPanel = new JPanel();
        //buttonPanel.setLayout(new GridLayout(1, 3, 10, 10));
        //buttonPanel.setBackground(new Color(32, 34, 37));

        //GridBagConstraints constraints = new GridBagConstraints();
        //constraints.fill = GridBagConstraints.BOTH;

        //constraints.gridx = 0;
        //constraints.gridy = 0;
        //constraints.weightx = 1.0;
        //constraints.weighty = 1.0;
        //basedPanel.add(inputPanel, constraints);

        //constraints.gridx = 0;
        //constraints.gridy = 0;
        //constraints.weightx = 1.0;
        //constraints.weighty = 0.2;
        //basedPanel.add(buttonPanel, constraints);


        JLabel accountLabel = new JLabel("Учетная запись:");
        accountLabel.setForeground(Color.WHITE);
        accountField = new JTextField();

        JLabel passwordLabel = new JLabel("Пароль:");
        passwordLabel.setForeground(Color.WHITE);
        passwordField = new JPasswordField();

        JLabel urlLabel = new JLabel("Сайт (URL):");
        urlLabel.setForeground(Color.WHITE);
        urlField = new JTextField();

        JButton addButton = new JButton("Добавить");
        JButton delButton = new JButton("Удалить");
        JButton editButton = new JButton("Редактировать");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = accountField.getText();
                String password = new String(passwordField.getPassword());
                //String m_key = JOptionPane.showInputDialog(null, "Введите мастер пароль:");
                checkMasterPassword();
                String url = urlField.getText();
                if (account.isEmpty() || password.isEmpty() || url.isEmpty()) {
                    JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Поля не могут быть пустыми.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (rst) {
                    rst = false;
                    addPassword(account, password, url);
                    accountField.setText("");
                    passwordField.setText("");
                    urlField.setText("");
                    updateAccountTable();
                }
                else {JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Мастер пароль неверен.", "Ошибка", JOptionPane.ERROR_MESSAGE);}
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = accountField.getText();
                String password = new String(passwordField.getPassword());
                checkMasterPassword();
                String url = urlField.getText();
                if (account.isEmpty() || password.isEmpty() || url.isEmpty()) {
                    JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Поля не могут быть пустыми.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (rst) {
                    rst = false;
                    editPassword(account, password, url);
                    accountField.setText("");
                    passwordField.setText("");
                    urlField.setText("");
                    updateAccountTable();
                }
                else {JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Мастер пароль неверен.", "Ошибка", JOptionPane.ERROR_MESSAGE);}
            }
        });

        delButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String account = accountField.getText();
                String password = new String(passwordField.getPassword());
                String url = urlField.getText();
                checkMasterPassword();

                if (rst) {
                    rst = false;
                int selectedOption = JOptionPane.showConfirmDialog(null,
                        "Вы точно хотите удалить запись?",
                        "Внимание!",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {
                    deletePassword(account, url);
                    accountField.setText("");
                    passwordField.setText("");
                    urlField.setText("");
                    updateAccountTable();
                }
                else {JOptionPane.showMessageDialog(PasswordManagerGUI.this, "Мастер пароль неверен.", "Ошибка", JOptionPane.ERROR_MESSAGE);}
                }
            }
        });


        inputPanel.add(accountLabel);
        inputPanel.add(accountField);
        inputPanel.add(editButton);

        inputPanel.add(passwordLabel);
        inputPanel.add(passwordField);
        inputPanel.add(addButton);

        inputPanel.add(urlLabel);
        inputPanel.add(urlField);
        inputPanel.add(delButton);

        //inputPanel.add(new JLabel());
        //buttonPanel.add(editButton);
        //buttonPanel.add(addButton);
        //buttonPanel.add(delButton);

        //basedPanel.add(inputPanel);
        //basedPanel.add(buttonPanel);

        panel.add(inputPanel, BorderLayout.NORTH);
        //panel.add(buttonPanel, BorderLayout.CENTER);

        tableModel = new DefaultTableModel(new Object[]{"Учетная запись", "Сайт (URL)", "Пароль"}, 0);
        accountTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setShowGrid(false);
        accountTable.setRowHeight(25);
        accountTable.setForeground(Color.WHITE);
        accountTable.setFont(accountTable.getFont().deriveFont(Font.PLAIN, 12f));
        accountTable.setBackground(new Color(32, 34, 37));
        accountTable.getTableHeader().setReorderingAllowed(false);
        accountTable.getTableHeader().setBackground(new Color(32, 34, 37));
        accountTable.getTableHeader().setForeground(Color.BLACK);
        accountTable.getTableHeader().setFont(accountTable.getTableHeader().getFont().deriveFont(Font.BOLD).deriveFont(12f));
        accountTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        accountTable.setGridColor(new Color(169, 169, 169));
        accountTable.setIntercellSpacing(new Dimension(0, 0));
        accountTable.setFocusable(false);
        accountTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return label;
            }
        });
        accountTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = accountTable.rowAtPoint(e.getPoint());
                int column = accountTable.columnAtPoint(e.getPoint());
                if (row >= 0 && column >= 0) {
                    String account = (String) accountTable.getValueAt(row, 0);
                    String url = (String) accountTable.getValueAt(row, 1);
                    String password = getPassword(account, url);
                    if (password.startsWith("Пароль")) {
                        JOptionPane.showMessageDialog(PasswordManagerGUI.this, password, "Ошибка", JOptionPane.ERROR_MESSAGE);
                    } else {
                        showInfoLabel("Пароль: " + password, Color.WHITE);
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(accountTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.setBackground(new Color(32, 34, 37));

        panel.add(tableScrollPane, BorderLayout.CENTER);

        infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD).deriveFont(12f));
        infoLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        infoLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(32, 34, 37));
        infoPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(169, 169, 169)));
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(getWidth(), 40));
        infoPanel.add(infoLabel, BorderLayout.WEST);

        panel.add(infoPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(new Color(32, 34, 37));
        add(panel);

        //showInfoLabel("Введите мастер-пароль:", Color.YELLOW);
        setupDatabase();
    }

    private void setupDatabase() {
        try {
            String url = "jdbc:sqlite:passwords.db";
            connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();

            // Создаем таблицу, если она не существует
            statement.execute("CREATE TABLE IF NOT EXISTS passwords (id INTEGER PRIMARY KEY AUTOINCREMENT, account TEXT NOT NULL, password TEXT NOT NULL, url TEXT NOT NULL)");

            // Загружаем сохраненные учетные записи из базы данных
            ResultSet resultSet = statement.executeQuery("SELECT * FROM passwords");
            while (resultSet.next()) {
                String account = resultSet.getString("account");
                String password = resultSet.getString("password");
                url = resultSet.getString("url");


                if (!passwordMap.containsKey(url)) {
                    passwordMap.put(url, new HashMap<>());
                }
                passwordMap.get(url).put(account, password);
            }

            updateAccountTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addPassword(String account, String password, String url) {
        try {
            String query = "INSERT INTO passwords (account, password, url) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, account);
            statement.setString(2, password);
            statement.setString(3, url);
            statement.executeUpdate();

            if (!passwordMap.containsKey(url)) {
                passwordMap.put(url, new HashMap<>());
            }
            passwordMap.get(url).put(account, password);

            showInfoLabel("Пароль успешно добавлен.", Color.GREEN);
        } catch (SQLException e) {
            e.printStackTrace();
            showInfoLabel("Ошибка при добавлении пароля.", Color.RED);
        }
    }

    private void editPassword(String account, String newPassword, String url) {
        try {
            String query = "UPDATE passwords SET password = ? WHERE account = ? AND url = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, newPassword);
            statement.setString(2, account);
            statement.setString(3, url);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                passwordMap.get(url).put(account, newPassword);
                showInfoLabel("Пароль успешно изменен.", Color.GREEN);
            } else {
                showInfoLabel("Учетная запись не найдена.", Color.RED);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showInfoLabel("Ошибка при изменении пароля.", Color.RED);
        }
    }

    private void deletePassword(String account, String url) {
        try {
            String query = "DELETE FROM passwords WHERE account = ? AND url = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, account);
            statement.setString(2, url);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                passwordMap.get(url).remove(account);
                showInfoLabel("Пароль успешно удален.", Color.GREEN);
            } else {
                showInfoLabel("Учетная запись не найдена.", Color.RED);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showInfoLabel("Ошибка при удалении пароля.", Color.RED);
        }
    }


    private String getPassword(String account, String url) {
        if (passwordMap.containsKey(url)) {
            Map<String, String> accountMap = passwordMap.get(url);
            checkMasterPassword();
            if (!rst)
            {return "Мастер пароль неверен";}
            if (accountMap.containsKey(account)) {
                String password = accountMap.get(account);
                return password;
            } else {
                return "Учетная запись не найдена.";
            }
        } else {
            return "Сайт (URL) не найден.";
        }
    }

    public void updateAccountTable() {
        tableModel.setRowCount(0);
        for (String url : passwordMap.keySet()) {
            Map<String, String> accountMap = passwordMap.get(url);
            for (String account : accountMap.keySet()) {
                tableModel.addRow(new Object[]{account, url, "********"});
            }
        }
    }

    public void showInfoLabel(String text, Color color) {
        infoLabel.setText(text);
        infoLabel.setForeground(color);
    }

    public String HashCode(String passwordToHash)
    {
        String generatedPassword = null;

        try
        {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Add password bytes to digest
            md.update(passwordToHash.getBytes());

            // Get the hash's bytes
            byte[] bytes = md.digest();

            // This bytes[] has bytes in decimal format. Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            // Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public boolean CheckMaster() {
        String inboxed = "af5ef29965809723549a48c12823a757";
        String concated = inboxed + HashCode(masterPassword);
        String concated_hash = HashCode(concated);
        String OnlyHash = masterHash.substring(0, masterHash.length() - 5);
        return concated_hash.equals(OnlyHash);
    }
    //Проверяем наличие файла .hash в РД и то что он один и получаем значение masterhash
    private void CheckHash() {
        String directoryPath = System.getProperty("user.dir");
        String fileExtension = "hash";

        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] matchingFiles = directory.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith("." + fileExtension.toLowerCase());
                }
            });

            if (matchingFiles != null && matchingFiles.length == 1) {
                masterHash = matchingFiles[0].getName();
            } else {
                JOptionPane.showMessageDialog(null, "Целостность программы нарушена. База данных автоматически удалена", "Ошибка", JOptionPane.WARNING_MESSAGE);
                setMasterPassword();
                File file = new File("passwords.db");
                if (file.exists()) {
                    boolean deleted = file.delete();
                }
            }
        }
    }
    public void SaveMasterHash(String filename) {

        String inboxed = "af5ef29965809723549a48c12823a757";
        String concated = inboxed + HashCode(filename);

        String fileName = HashCode(concated) + ".hash";

        File file = new File(fileName);

        try {
            boolean created = file.createNewFile();

            if (created) {
                System.out.println("Файл успешно создан.");
            } else {
                System.out.println("Файл уже существует или не удалось создать файл.");
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка при создании файла: " + e.getMessage());
        }
    }

    private void setMasterPassword() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Введите мастер-пароль:");
        JPasswordField masterPasswordInput = new JPasswordField();

        JButton submitButton = new JButton("OK");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                char[] password = masterPasswordInput.getPassword();
                if (password.length > 0) {
                    masterPassword = new String(password);
                    SaveMasterHash(masterPassword);
                    frame.dispose();
                }
                else {JOptionPane.showMessageDialog(null,"Не указан мастер-пароль", "Предупреждение", JOptionPane.WARNING_MESSAGE);}
            }
        });
        panel.add(label, BorderLayout.NORTH);
        panel.add(masterPasswordInput, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
        masterPassword = null;
    }

    private void checkMasterPassword() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showOptionDialog(null, passwordField, "Введите мастер пароль:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            char[] password = passwordField.getPassword();
            String passwordString = new String(password);
            masterPassword = passwordString;
            if(CheckMaster()) {rst = true;}
            else {rst = false;}
        } else {
            JOptionPane.showMessageDialog(null,"Мастер пароль не введен", "Предупреждение", JOptionPane.WARNING_MESSAGE);
        }

    }



    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PasswordManagerGUI().setVisible(true);
            }
        });
    }
}
