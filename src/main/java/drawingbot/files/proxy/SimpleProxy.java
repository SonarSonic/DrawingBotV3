package drawingbot.files.proxy;

import drawingbot.files.json.JsonData;

import java.net.InetSocketAddress;
import java.net.Proxy;

@JsonData
public class SimpleProxy {

    public Proxy.Type type = Proxy.Type.DIRECT;
    public String hostname = "";
    public int port = 0;

    public SimpleProxy(){}

    public boolean hasProxy(){
        return type != Proxy.Type.DIRECT && !hostname.isEmpty();
    }

    public Proxy toProxy(){
        if(!hasProxy()){
            return Proxy.NO_PROXY;
        }
        return new Proxy(type, new InetSocketAddress(hostname, port));
    }

}
