package com.prevelio.customer;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class CustomerApplication {
    public static void main(String... args) {
        Quarkus.run(args);
    }
}
