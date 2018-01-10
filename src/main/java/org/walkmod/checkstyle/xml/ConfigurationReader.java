package org.walkmod.checkstyle.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.walkmod.util.DomHelper;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationReader {

    private final String configurationFile;

    public ConfigurationReader(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public Set<String> getRules() throws InvalidCheckstyleConfigurationException {
        try {
            InputStream is = readFromClasspath();
            if (is == null) {
                is = readFromURL();
                if (is == null) {
                    is = readFromFile();
                }
            }
            return getRules(is);
        } catch (Exception e) {
            throw new InvalidCheckstyleConfigurationException(e);
        }
    }

    private InputStream readFromFile() {
        File cfgFile = new File(configurationFile);
        if (cfgFile.exists()) {
            try {
                return new FileInputStream(cfgFile);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private InputStream readFromURL() {
        try {
            URL url = new URL(configurationFile);
            return url.openStream();
        }catch (Exception e) {
            return null;
        }
    }

    private InputStream readFromClasspath() {
        URL url = this.getClass().getResource(configurationFile);
        try {
            if (url != null) {
                //we try as a resource
                File cfgFile = new File(url.toURI());
                if (cfgFile.exists()) {
                    return new FileInputStream(cfgFile);
                }
            }

        } catch (Exception e) {
            return null;
        }
       return null;
    }

    private Set<String> getRules(InputStream is) throws Exception {

        if (is == null) {
            return Collections.emptySet();
        }
        try {
            Set<String> rules = new HashSet<String>();
            InputSource in = new InputSource(is);
            in.setSystemId(configurationFile);
            Document doc = DomHelper.parse(in);
            NodeList module = doc.getElementsByTagName("module");
            int max = module.getLength();
            for (int i = 0; i < max; i++) {
                Node rule = module.item(i);
                if (rule instanceof Element) {

                    Element elem = (Element) rule;

                    if (isASyntacticRuleSet(elem)) {

                        NodeList children = elem.getChildNodes();
                        int limit = children.getLength();
                        for (int k = 0; k < limit; k++) {
                            Node child = children.item(k);
                            if (isARuleNode(child)) {
                                if (child instanceof Element) {
                                    Element moduleRule = (Element) child;
                                    String moduleName = moduleRule.getAttribute("name");
                                    if (isRuleClass(moduleName)) {
                                        moduleName = toRuleName(moduleName);
                                    }

                                    rules.add(moduleName);
                                }
                            }
                        }
                    }
                }
            }
            return rules;
        } finally {
            is.close();
        }
    }

    private boolean isARuleNode(Node child) {
        return child.getNodeName().equals("module");
    }

    private boolean isASyntacticRuleSet(Element node) {
        Element elem = (Element) node;
        if (elem.hasAttribute("name")) {
            String name = elem.getAttribute("name");
            return (name.endsWith("TreeWalker"));
        }
        return false;
    }

    private boolean isRuleClass(String moduleName) {
        return  (moduleName.startsWith("com.puppycrawl.tools.checkstyle.checks.")
                && moduleName.endsWith("Check"));
    }

    private String toRuleName(String moduleName) {
        int checkClassNameIndex = moduleName.lastIndexOf(".");
        return moduleName.substring(
                checkClassNameIndex + 1, moduleName.lastIndexOf("Check"));
    }
}
