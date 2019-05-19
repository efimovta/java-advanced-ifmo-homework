package ru.ifmo.rain.efimov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.Tester;

public class Main {
    public static void main(String[] args) {
        Tester.main("list", IterativeParallelism.class.getName());
    }
}
