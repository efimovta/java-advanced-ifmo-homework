package ru.ifmo.rain.efimov.hello;

import info.kgeorgiy.java.advanced.hello.Tester;

public class Main {
    public static void main(String[] args) {
        Tester.main("client-i18n", HelloUDPClient.class.getName());
        System.out.println("|...starting server-i18n test...|");
        Tester.main("server-i18n", HelloUDPServer.class.getName());
    }
}
