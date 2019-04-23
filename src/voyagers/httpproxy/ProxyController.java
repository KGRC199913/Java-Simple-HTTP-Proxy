package voyagers.httpproxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyController extends Thread {
    private ServerSocket mServerSocket;
    private static int mPort = 8888;
    private boolean mProxyListenContinue = true;
    ProxyController(int port) {
        mPort = port;
    }

    @Override
    public void run() {
        super.run();
        try {
            mServerSocket = new ServerSocket(mPort);
            while (mProxyListenContinue) {
                Socket socket = mServerSocket.accept();
                new HttpReqHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mServerSocket != null) {
                    mServerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ProxyController controller = new ProxyController(8888);
    }
}
