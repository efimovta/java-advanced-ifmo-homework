package ru.ifmo.rain.efimov.mapper;

import info.kgeorgiy.java.advanced.mapper.Tester;

public class Main {
    public static void main(String[] args) {
        Tester.main("list", ParallelMapperImpl.class.getName()+","+ru.ifmo.rain.efimov.mapper.IterativeParallelism.class.getName());
    }
}
