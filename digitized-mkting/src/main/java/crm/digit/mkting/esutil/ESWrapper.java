package crm.digit.mkting.esutil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.google.common.collect.Lists;


/**
 * Created by huoqiang on 26/7/2017.
 */
public class ESWrapper {
    private static Log log = LogFactory.getLog(ESWrapper.class);
    // private static Object lock = new Object();
    private static TransportClient client;

    // FIXME 20160428 临时增加，日后做成纯静态的
//	private static ConcurrentHashMap<LUCENE_INDEX_TYPE, SearchRequestBuilder> builders = new ConcurrentHashMap<LUCENE_INDEX_TYPE, SearchRequestBuilder>();

    public synchronized static TransportClient getClient() {
        if (client == null) {
            try {
                Settings settings =  ImmutableSettings.settingsBuilder()//
                        .put("cluster.name", ESClientConfig.CLUSTER_NAME)//
                        .put("client.transport.sniff", true)//
                        .build();
                List<InetSocketTransportAddress> list = Lists.newArrayList();
                for (String addr : ESClientConfig.ADDR) {
                    list.add(new InetSocketTransportAddress(InetAddress.getByName(addr), ESClientConfig.PORT));
                }
                InetSocketTransportAddress addresses[] = (InetSocketTransportAddress[]) list.toArray(new InetSocketTransportAddress[list.size()]);
                client = new TransportClient(settings).addTransportAddresses(addresses);
            } catch (UnknownHostException e) {
                log.error(e.getMessage(), e);
            }
        }
        return client;
    }


    public static void main(String[] args){
        TransportClient client = getClient();
        System.out.println(client);
        System.out.println(ESClientConfig.CLUSTER_NAME);

    }
}
