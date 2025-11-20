// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class OtherClient {
    public static void main (String [] args)
    {
        Client s = new Client ("127.0.0.1", "127.0.0.1", 53743);
        Server.addClient();
    }
}