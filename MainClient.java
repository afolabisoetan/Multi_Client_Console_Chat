public class MainClient {
    public static void main (String [] args)
    {
        Client s = new Client ("127.0.0.1", "127.0.0.1", 53742);
        Server.addClient();
    }
}
