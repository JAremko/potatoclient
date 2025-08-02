package potatoclient.transit;

import com.cognitect.transit.Keyword;
import com.cognitect.transit.TransitFactory;
import java.util.*;

/**
 * Defines metadata for a single command action in the system.
 * This class is used for registry lookup and validation metadata only.
 * NO protobuf dependencies allowed!
 */
public class ActionDefinition {
    private final Keyword keyword;
    private final String name;
    private final String description;
    private final Set<Keyword> requiredParams;
    private final Set<Keyword> optionalParams;
    private final boolean implemented;
    
    private ActionDefinition(Builder builder) {
        this.keyword = builder.keyword;
        this.name = builder.name;
        this.description = builder.description;
        this.requiredParams = Collections.unmodifiableSet(new HashSet<>(builder.requiredParams));
        this.optionalParams = Collections.unmodifiableSet(new HashSet<>(builder.optionalParams));
        this.implemented = builder.implemented;
    }
    
    // Getters
    public Keyword getKeyword() { return keyword; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Set<Keyword> getRequiredParams() { return requiredParams; }
    public Set<Keyword> getOptionalParams() { return optionalParams; }
    public boolean isImplemented() { return implemented; }
    
    /**
     * Get all parameter names (required + optional)
     */
    public Set<Keyword> getAllParams() {
        Set<Keyword> all = new HashSet<>();
        all.addAll(requiredParams);
        all.addAll(optionalParams);
        return Collections.unmodifiableSet(all);
    }
    
    /**
     * Check if this action has the given parameter
     */
    public boolean hasParam(Keyword param) {
        return requiredParams.contains(param) || optionalParams.contains(param);
    }
    
    /**
     * Check if the given parameter is required
     */
    public boolean isRequired(Keyword param) {
        return requiredParams.contains(param);
    }
    
    // Builder pattern for clean construction
    public static class Builder {
        private Keyword keyword;
        private String name;
        private String description = "";
        private Set<Keyword> requiredParams = new HashSet<>();
        private Set<Keyword> optionalParams = new HashSet<>();
        private boolean implemented = false;
        
        public Builder(String name) {
            this.name = name;
            this.keyword = TransitFactory.keyword(name);
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder required(String... params) {
            for (String param : params) {
                this.requiredParams.add(TransitFactory.keyword(param));
            }
            return this;
        }
        
        public Builder optional(String... params) {
            for (String param : params) {
                this.optionalParams.add(TransitFactory.keyword(param));
            }
            return this;
        }
        
        public Builder implemented() {
            this.implemented = true;
            return this;
        }
        
        public Builder implemented(boolean impl) {
            this.implemented = impl;
            return this;
        }
        
        public ActionDefinition build() {
            return new ActionDefinition(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ActionDefinition{name='%s', required=%s, optional=%s, implemented=%s}",
                name, requiredParams, optionalParams, implemented);
    }
}