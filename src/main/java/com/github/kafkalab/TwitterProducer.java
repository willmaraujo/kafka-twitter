package com.github.kafkalab;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    Logger logger = LoggerFactory.getLogger(TwitterProducer.class);

    final String CONSUMER_KEY = "7VKgRgNy3wkUfBAMv00MhCE91";
    final String CONSUMER_SECRET = "tTg5RRQluIfDPWX5ELkD9xmcqK7RyVWNDgR8U4Ui0a6i9goFsK";
    final String TOKEN = "1254047344544428038-4pYd6QjlIgVQtF9PIDwT3LggSVNDJG";
    final String SECRET = "K7jYsUyeDL6ObzbK7GMrUYrUdfSYYFido2BpeOX9VYz9H";

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        // create a twitter client
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(100000);
        Client client = createTwitterClient(msgQueue);
        client.connect();

        // create a kafka producer

        // loop to send tweets to kafka
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }

            if (msg != null) {
                logger.info(msg);
            }
        }

        logger.info("End of application");
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {
        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        List<String> terms = Lists.newArrayList("bitcoin");
        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, SECRET);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        return hosebirdClient;

    }
}
