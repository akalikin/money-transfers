package com.revolut.akalikin.launcher;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory;
import org.glassfish.grizzly.http.server.HttpServer;

import java.io.IOException;
import java.net.URI;

public class AccountTransferServiceLauncher {

    private static final String ENDPOINT = "http://localhost:8080/";
    private static final String PACKAGE = "com.revolut.akalikin.controller";

    public static HttpServer startServer() throws IOException {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Store.class).to(InMemoryStore.class);
            }
        });

        ResourceConfig resourceConfig = new PackagesResourceConfig(PACKAGE);
        IoCComponentProviderFactory ioc = new GuiceComponentProviderFactory(resourceConfig, injector);
        return GrizzlyServerFactory.createHttpServer(URI.create(ENDPOINT).toString(), resourceConfig, ioc);
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            System.out.println("Server started.");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.out.println("Unable to start the server");
            e.printStackTrace();
            return;
        }
    }

}