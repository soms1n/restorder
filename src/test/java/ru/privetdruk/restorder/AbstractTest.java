package ru.privetdruk.restorder;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class AbstractTest {
}
