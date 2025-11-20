public class ThirdClient {
    public static void main (String [] args)
    {
        Client s = new Client ("127.0.0.1", "127.0.0.1", 53744);
        Server.addClient();
    }
}
