package voyagers.httpproxy;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class HttpReqHandler extends Thread {
    private Socket mClientSocket;
    private Socket mDestinationSocket;
    private StringBuilder reqBuilder = new StringBuilder();
    private static final String EOL = "\r\n";
    private static final int HTTP_PORT = 80;
    private static final String BLACKLIST = "blacklist.conf";
    private static final String FORBIDDEN_HTTP_RESPONSE = "HTTP/1.1 403 Forbidden\r\n\r\n<body><h1 style=\"text-align: center;\"><span style=\"text-decoration: underline; color: #ff0000;\"><strong>ACCESS DENIED</strong></span></h1><hr /><p style=\"text-align: center;\">This site was <span style=\"color: #ff0000;\"><em><span style=\"text-decoration: underline;\">blocked</span></em></span> by the proxy server.</p></body>\r\n\r\n";
    HttpReqHandler(Socket acceptedSocket) {
        mClientSocket = acceptedSocket;
    }

    void handleBlackList(DataOutputStream destinationWriter) throws IOException {
        destinationWriter.writeBytes(FORBIDDEN_HTTP_RESPONSE);
        destinationWriter.flush();
    }

    boolean isUrlBlackListed(String url) {
        if (url == null)
            return false;
        BufferedReader reader = null;
        FileReader fileReader = null;
        try {
            File blacklistFile = new File(BLACKLIST);
            blacklistFile.createNewFile();

            fileReader = new FileReader(blacklistFile);
            reader = new BufferedReader(fileReader);
            String lines;

            while ((lines = reader.readLine()) != null) {
                if (lines.equals(url)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (fileReader != null)
                    fileReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void run() {
        super.run();
        try {
            BufferedReader incomingReqReader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
            DataOutputStream responseWriter = new DataOutputStream(mClientSocket.getOutputStream());

            String reqLine;
            reqLine = incomingReqReader.readLine();
            reqBuilder.append(reqLine).append(EOL);
            String[] httpHeaderComponents = reqLine.split(" ");
            if (isGETorPOST(httpHeaderComponents[0])) {
                return;
            }
            String host = getHost(httpHeaderComponents[1]);

            ProxyGUI.logToStatusBoard("Processing " + reqLine + "\n");
            if (isUrlBlackListed(host)) {
                ProxyGUI.logToStatusBoard("BLACKLISTED site, attempting to block access\n");
                handleBlackList(responseWriter);
                return;
            }
            buildReq(incomingReqReader);
            sendReqAndTransferRes(responseWriter, reqBuilder.toString(), host);
        } catch (IOException exception) {
            System.out.println(exception.toString());
        } finally {
            try {
                if (mClientSocket != null) {
                    mClientSocket.close();
                }
                if (mDestinationSocket != null) {
                    mDestinationSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isGETorPOST(String method) {
        return !method.equals("GET") && !method.equals("POST");
    }

    private String getHost(String reqUrl) throws MalformedURLException {
        URL url = new URL(reqUrl);
        return url.getHost();
    }

    private void buildReq(BufferedReader incomingReqReader) throws IOException {
        String line;
        while (true) {
            if (incomingReqReader.ready()) {
                line = incomingReqReader.readLine();
                if (line.equals(""))
                    reqBuilder.append(EOL);
                else
                    reqBuilder.append(line).append(EOL);
            }
            else
                break;
        }
        reqBuilder.append(EOL);
    }

    private void sendReqAndTransferRes(DataOutputStream responseWriter, String req, String host) throws IOException {
        mDestinationSocket = new Socket(InetAddress.getByName(host), HTTP_PORT);
        DataOutputStream destinationReqWriter = new DataOutputStream(mDestinationSocket.getOutputStream());
        destinationReqWriter.writeBytes(req);
        destinationReqWriter.flush();

        InputStream responseReader = mDestinationSocket.getInputStream();
        responseReader.transferTo(responseWriter);
        responseWriter.flush();
    }
}
