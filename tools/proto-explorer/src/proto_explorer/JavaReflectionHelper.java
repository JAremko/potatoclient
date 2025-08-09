package proto_explorer;

import java.lang.reflect.*;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Keyword;

/**
 * Java helper for reflection tasks that Babashka cannot perform.
 * This is called via shell execution from Babashka.
 */
public class JavaReflectionHelper {
    
    private static final IFn prStr = Clojure.var("clojure.core", "pr-str");
    private static final IFn readString = Clojure.var("clojure.edn", "read-string");
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("{:error \"Usage: java JavaReflectionHelper <command> <class-name>\"}");
            System.exit(1);
        }
        
        String command = args[0];
        String className = args[1];
        
        try {
            switch (command) {
                case "class-info":
                    printClassInfo(className);
                    break;
                case "field-mapping":
                    printFieldMapping(className);
                    break;
                case "builder-info":
                    printBuilderInfo(className);
                    break;
                case "validation-info":
                    printValidationInfo(className);
                    break;
                default:
                    System.out.println("{:error \"Unknown command: " + command + "\"}");
                    System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("{:error \"" + e.getMessage() + "\"}");
            System.exit(1);
        }
    }
    
    private static void printClassInfo(String className) throws Exception {
        Class<?> cls = Class.forName(className);
        
        StringBuilder edn = new StringBuilder();
        edn.append("{");
        edn.append(":class-name \"").append(cls.getName()).append("\", ");
        edn.append(":simple-name \"").append(cls.getSimpleName()).append("\", ");
        edn.append(":package \"").append(cls.getPackage() != null ? cls.getPackage().getName() : "").append("\", ");
        
        // Fields
        edn.append(":fields [");
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) edn.append(" ");
            Field f = fields[i];
            edn.append("{:name \"").append(f.getName()).append("\" ");
            edn.append(":type \"").append(f.getType().getSimpleName()).append("\" ");
            edn.append(":modifiers \"").append(Modifier.toString(f.getModifiers())).append("\"}");
        }
        edn.append("], ");
        
        // Methods (limit to first 10 to avoid huge output)
        edn.append(":methods [");
        Method[] methods = cls.getDeclaredMethods();
        int methodCount = Math.min(methods.length, 10);
        for (int i = 0; i < methodCount; i++) {
            if (i > 0) edn.append(" ");
            Method m = methods[i];
            edn.append("{:name \"").append(m.getName()).append("\" ");
            edn.append(":return-type \"").append(m.getReturnType().getSimpleName()).append("\" ");
            edn.append(":param-count ").append(m.getParameterCount()).append("}");
        }
        edn.append("], ");
        edn.append(":total-methods ").append(methods.length);
        
        // Check if it's a protobuf message
        if (GeneratedMessage.class.isAssignableFrom(cls)) {
            try {
                Method getDescriptor = cls.getMethod("getDescriptor");
                Descriptors.Descriptor descriptor = (Descriptors.Descriptor) getDescriptor.invoke(null);
                edn.append(", :protobuf? true");
                edn.append(", :proto-name \"").append(descriptor.getName()).append("\"");
                edn.append(", :field-count ").append(descriptor.getFields().size());
            } catch (Exception e) {
                // Not a proper protobuf message
            }
        }
        
        edn.append("}");
        System.out.println(edn.toString());
    }
    
    private static void printFieldMapping(String className) throws Exception {
        Class<?> cls = Class.forName(className);
        
        if (!GeneratedMessage.class.isAssignableFrom(cls)) {
            System.out.println("{:error \"Not a protobuf message class\"}");
            return;
        }
        
        Method getDescriptor = cls.getMethod("getDescriptor");
        Descriptors.Descriptor descriptor = (Descriptors.Descriptor) getDescriptor.invoke(null);
        
        StringBuilder edn = new StringBuilder();
        edn.append("{:message-name \"").append(descriptor.getName()).append("\", ");
        edn.append(":field-mappings [");
        
        boolean first = true;
        for (Descriptors.FieldDescriptor field : descriptor.getFields()) {
            if (!first) edn.append(" ");
            first = false;
            
            String protoName = field.getName();
            String camelName = toCamelCase(protoName);
            String getterName = "get" + capitalize(camelName);
            String hasName = "has" + capitalize(camelName);
            
            edn.append("{:proto-name \"").append(protoName).append("\" ");
            edn.append(":field-number ").append(field.getNumber()).append(" ");
            edn.append(":getter \"").append(getterName).append("\" ");
            edn.append(":has-method \"").append(hasName).append("\" ");
            edn.append(":type \"").append(field.getType().name()).append("\"}");
        }
        
        edn.append("]}");
        System.out.println(edn.toString());
    }
    
    private static void printBuilderInfo(String className) throws Exception {
        Class<?> cls = Class.forName(className);
        String builderClassName = className + "$Builder";
        
        try {
            Class<?> builderClass = Class.forName(builderClassName);
            
            StringBuilder edn = new StringBuilder();
            edn.append("{:builder-class \"").append(builderClassName).append("\", ");
            edn.append(":methods [");
            
            Method[] methods = builderClass.getDeclaredMethods();
            boolean first = true;
            int count = 0;
            
            for (Method m : methods) {
                String name = m.getName();
                if (name.startsWith("set") || name.startsWith("add") || 
                    name.startsWith("clear") || name.equals("build")) {
                    if (!first) edn.append(" ");
                    first = false;
                    
                    edn.append("{:name \"").append(name).append("\" ");
                    edn.append(":param-count ").append(m.getParameterCount()).append("}");
                    
                    if (++count >= 20) break; // Limit output
                }
            }
            
            edn.append("]}");
            System.out.println(edn.toString());
            
        } catch (ClassNotFoundException e) {
            System.out.println("{:error \"Builder class not found: " + builderClassName + "\"}");
        }
    }
    
    private static String toCamelCase(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                result.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        
        return result.toString();
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    
    private static void printValidationInfo(String className) throws Exception {
        Class<?> cls = Class.forName(className);
        
        if (!GeneratedMessage.class.isAssignableFrom(cls)) {
            System.out.println("{:error \"Not a protobuf message class\"}");
            return;
        }
        
        Method getDescriptor = cls.getMethod("getDescriptor");
        Descriptors.Descriptor descriptor = (Descriptors.Descriptor) getDescriptor.invoke(null);
        
        StringBuilder edn = new StringBuilder();
        edn.append("{:message-name \"").append(descriptor.getName()).append("\", ");
        edn.append(":fields-with-validation [");
        
        boolean first = true;
        for (Descriptors.FieldDescriptor field : descriptor.getFields()) {
            // Get field options which may contain buf.validate constraints
            Object fieldOptions = field.getOptions();
            
            // The validation options are stored as extensions
            // We need to check for buf.validate.field extension
            String optionsStr = fieldOptions.toString();
            
            // Only include fields that have validation options
            if (optionsStr.contains("buf.validate")) {
                if (!first) edn.append(" ");
                first = false;
                
                edn.append("{:field-name \"").append(field.getName()).append("\" ");
                edn.append(":field-number ").append(field.getNumber()).append(" ");
                edn.append(":type \"").append(field.getType().name()).append("\" ");
                
                // Include the raw options string for now - we can parse it later
                // Escape quotes in the options string
                String escapedOptions = optionsStr.replace("\"", "\\\"");
                edn.append(":options \"").append(escapedOptions).append("\"}");
            }
        }
        
        edn.append("]}");
        System.out.println(edn.toString());
    }
}