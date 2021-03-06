package org.solwind;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by solwind on 6/14/17.
 */
public class ZookeeperDiscoveryConnector implements DiscoveryConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperDiscoveryConnector.class);

    private final Properties properties;
    private String host;
    private ZooKeeper zk;
    private ZooKeeperClient zooKeeperClient;

    public ZookeeperDiscoveryConnector(Properties properties) {
        this.properties = properties;
    }

    public void init() {
        this.host = properties.getProperty("host");
        this.zooKeeperClient = new ZooKeeperClient();
    }

    public void connect() throws IOException, InterruptedException {
        this.zk = this.zooKeeperClient.connect();
    }

    public void push(String path, String data) throws KeeperException, InterruptedException {
        this.zooKeeperClient.create(path, data);
    }

    public String retrieve(String path) {
        try {
            return new String(ZookeeperDiscoveryConnector.this.zk.getData(path, true, null));
        } catch (KeeperException e) {
            LOGGER.info(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public Properties props() {
        return this.properties;
    }

    private class ZooKeeperClient {

        final CountDownLatch connectedSignal = new CountDownLatch(1);
        private ZooKeeper zoo;

        public ZooKeeper connect() throws IOException, InterruptedException {

            zoo = new ZooKeeper(host, 5000, we -> {

                if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            });

            connectedSignal.await();
            return zoo;
        }

        public void close() throws InterruptedException {
            zoo.close();
        }

        public void create(String path, String data) throws
                KeeperException, InterruptedException {
            if (ZookeeperDiscoveryConnector.this.zk.exists("/" + path, true) == null) {
                ZookeeperDiscoveryConnector.this.zk.create("/" + path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        }
    }

}
