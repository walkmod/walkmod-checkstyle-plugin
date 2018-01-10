package org.walkmod.checkstyle.xml;

public class InvalidCheckstyleConfigurationException extends Exception {
    public InvalidCheckstyleConfigurationException(Throwable cause) {
        super("Invalid Checkstyle configuration", cause);
    }
}
