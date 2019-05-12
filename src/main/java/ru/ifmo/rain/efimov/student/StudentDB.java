package ru.ifmo.rain.efimov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/**
 * Most method descriptions just copy-pasted from interface javadocs,
 * but there are notes in some places.
 */
public class StudentDB implements StudentGroupQuery {

    /**
     * In the description of {@link StudentQuery#sortStudentsByName(Collection)} such specification was given
     */
    private final Comparator<Student> studentByNameComparator = comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparingInt(Student::getId);

    /**
     * Returns student groups, where both groups and students within a group are ordered by name.
     */
    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getSortedGroups(students, studentByNameComparator);
    }

    /**
     * Returns student groups, where groups are ordered by name, and students within a group are ordered by id.
     */
    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getSortedGroups(students, comparingInt(Student::getId));
    }

    /**
     * Returns name of the group containing maximum number of students.
     * If there are more than one largest group, the one with smallest name is returned.
     * <br/>
     * <b>Note:</b> 1. "lexicography smallest" as I understood from the tests)
     * <br/>2. 'orElse("")' also from tests specification
     */
    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargest(students, counting());
    }

    /**
     * Returns name of the group containing maximum number of students with distinct first names
     * (compare groups by number of 'students with distinct first names').
     * If there are more than one largest group, the one with smallest name is returned.
     * <br/>
     * <b>Note:</b> "lexicography smallest" as I understood from the tests)
     * <br/>2. 'orElse("")' also from tests specification
     */
    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargest(students, collectingAndThen(mapping(Student::getFirstName, toSet()), s -> (long) s.size()));
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapStudentToProperty(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapStudentToProperty(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapStudentToProperty(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapStudentToProperty(students, student -> student.getFirstName() + " " + student.getLastName());
    }

    /**
     * Returns distinct student {@link Student#getFirstName() first names} in alphabetical order.
     */
    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream().map(Student::getFirstName).collect(toCollection(TreeSet::new));
    }

    /**
     * Returns name of the student with minimal {@link Student#getId() id}.
     */
    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(comparing(Student::getId)).map(Student::getFirstName).orElse("");
    }


    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortBy(students, Student::compareTo);
    }

    /**
     * Returns list of students sorted by name
     * (students are ordered by {@link Student#getLastName() lastName},
     * students with equal last names are ordered by {@link Student#getFirstName() firstName},
     * students having equal both last and first names are ordered by {@link Student#getId() id}.
     */
    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortBy(students, studentByNameComparator);
    }

    /**
     * Returns list of students having specified first name. Students are ordered by name.
     */
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String firstName) {
        return findStudentsBy(students, Student::getFirstName, firstName);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String lastName) {
        return findStudentsBy(students, Student::getLastName, lastName);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsBy(students, Student::getGroup, group);
    }

    /**
     * Returns map of group's student last names mapped to minimal first name.
     */
    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(s -> s.getGroup().equals(group))
                .collect(toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }

    private List<Student> sortBy(Collection<Student> students, Comparator<Student> cmp) {
        return students.stream().sorted(cmp).collect(toList());
    }

    private List<Group> getSortedGroups(Collection<Student> students, Comparator<Student> cmp) {
        return students.stream().sorted(cmp)
                .collect(groupingBy(Student::getGroup, TreeMap::new, toList()))
                .entrySet().stream()
                .map(e -> new Group(e.getKey(), e.getValue())).collect(toList());
    }

    /**
     * Calc stat 'group -> comparing calculated attribute' and find max.
     * If there are more than one largest group, the one with smallest name is returned.
     */
    private <A, R> String getLargest(Collection<Student> students, Collector<Student, ?, Long> attributeCalculatorForCompareGroups) {
        return students.stream()
                .collect(groupingBy(Student::getGroup, attributeCalculatorForCompareGroups))
                .entrySet().stream()
                .max(comparingLong(Entry<String, Long>::getValue)
                        .thenComparing(e -> e.getKey(), reverseOrder(String::compareTo))
                )
                .map(Entry::getKey).orElse("");
    }

    private List<String> mapStudentToProperty(List<Student> students, Function<Student, String> mapFunction) {
        return students.stream().map(mapFunction).collect(toList());
    }

    /**
     * Returns list of students having specified attribute. Students are ordered by this attribute.
     */
    private List<Student> findStudentsBy(Collection<Student> students, Function<Student, String> attributeGetter, String attribute) {
        return students.stream()
                .filter(s -> attributeGetter.apply(s).equals(attribute))
                .sorted(studentByNameComparator).collect(toList());
    }
}
