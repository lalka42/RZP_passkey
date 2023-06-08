private static final int SHIFT = 150;
private static final String PATH = "data/pass.txt";

public static void main(String[] args) {
        System.out.print("Пароль: ");
        Scanner in = new Scanner(System.in);
        String password = in.nextLine();
        System.out.println(password);
        String encodeText = encode(password, SHIFT);
        System.out.println("Зашифрованный пароль: " + encodeText);
        save(encodeText, PATH);
        System.out.println("Расшифрованный пароль: " + decode(write(PATH), SHIFT));
        in.close();
        }

//Шифрование (сдвиг символов на величину shift. Каждый последующий символ сдвигается на одну позицию больше предыдущего)
public static String encode(String text, int shift) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        sb.append((char) (text.charAt(i) + shift + i));
        return sb.toString();
        }

//Расшифровка
public static String decode(String text, int shift) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        sb.append((char) (text.charAt(i) - shift - i));
        return sb.toString();
        }

//Запись в файл
public static void save(String text, String path) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)))) {
        bw.write(text);
        } catch (IOException exc) {
        System.out.println(exc.getMessage());
        }
        }

//Чтение из файла
public static String write(String path) {
        String decode = null;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
        decode = br.readLine();
        } catch (FileNotFoundException exc) {
        System.out.println("Файл не найден");
        } catch (IOException exc) {
        System.out.println(exc.getMessage());
        }
        return decode;
        }
