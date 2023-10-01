package drawingbot.files.proxy;

import drawingbot.files.FileUtils;
import drawingbot.files.json.JsonLoaderManager;

import java.io.File;
import java.net.Proxy;

public class SimpleProxyHandler {

    private static Proxy defaultProxy = null;
    public static final String fileName = "http_proxy.json";

    public static Proxy getDefaultProxy(){
        if(defaultProxy == null){
            SimpleProxy simpleProxy = loadProxyFromFile();
            defaultProxy = simpleProxy == null ? Proxy.NO_PROXY : simpleProxy.toProxy();
        }
        return defaultProxy;
    }

    public static SimpleProxy loadProxyFromFile(){
        File proxyFile = new File(FileUtils.getUserDataDirectory() + File.separator + fileName);
        return JsonLoaderManager.getOrCreateJSONFile(SimpleProxy.class, proxyFile, c -> new SimpleProxy());
    }

}