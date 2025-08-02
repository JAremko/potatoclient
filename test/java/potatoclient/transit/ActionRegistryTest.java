package potatoclient.transit;

import com.cognitect.transit.Keyword;
import com.cognitect.transit.TransitFactory;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

import java.util.*;

public class ActionRegistryTest {
    
    @BeforeClass
    public static void setup() {
        // Registry is initialized statically, but let's make sure
        // by accessing it
        ActionRegistry.getAllActionNames();
    }
    
    @Test
    public void testBasicCommandsRegistered() {
        // Test that basic commands are registered
        assertTrue("ping should be registered", ActionRegistry.isKnownAction("ping"));
        assertTrue("noop should be registered", ActionRegistry.isKnownAction("noop"));
        assertTrue("frozen should be registered", ActionRegistry.isKnownAction("frozen"));
    }
    
    @Test
    public void testKeywordLookup() {
        // Test keyword-based lookup
        Keyword pingKeyword = TransitFactory.keyword("ping");
        assertTrue("ping keyword should be registered", ActionRegistry.isKnownAction(pingKeyword));
        
        ActionDefinition pingDef = ActionRegistry.getAction(pingKeyword);
        assertNotNull("Should get ping definition by keyword", pingDef);
        assertEquals("ping", pingDef.getName());
    }
    
    @Test
    public void testUnknownAction() {
        assertFalse("unknown action should not be registered", 
                    ActionRegistry.isKnownAction("unknown-action"));
        assertNull("Should return null for unknown action", 
                   ActionRegistry.getAction("unknown-action"));
    }
    
    @Test
    public void testBasicCommandsHaveNoParameters() {
        // Basic commands should have no required or optional parameters
        ActionDefinition ping = ActionRegistry.getAction("ping");
        assertNotNull(ping);
        assertTrue("ping should have no required params", ping.getRequiredParams().isEmpty());
        assertTrue("ping should have no optional params", ping.getOptionalParams().isEmpty());
    }
    
    @Test
    public void testBasicCommandsAreImplemented() {
        // Basic commands should be marked as implemented
        ActionDefinition ping = ActionRegistry.getAction("ping");
        ActionDefinition noop = ActionRegistry.getAction("noop");
        ActionDefinition frozen = ActionRegistry.getAction("frozen");
        
        assertTrue("ping should be implemented", ping.isImplemented());
        assertTrue("noop should be implemented", noop.isImplemented());
        assertTrue("frozen should be implemented", frozen.isImplemented());
    }
    
    @Test
    public void testGetAllActionKeywords() {
        List<Keyword> keywords = ActionRegistry.getAllActionKeywords();
        assertNotNull(keywords);
        assertTrue("Should have at least 3 actions", keywords.size() >= 3);
        
        // Check that our basic commands are in the list
        Set<String> keywordNames = new HashSet<>();
        for (Keyword k : keywords) {
            keywordNames.add(k.getName());
        }
        assertTrue("Should contain ping", keywordNames.contains("ping"));
        assertTrue("Should contain noop", keywordNames.contains("noop"));
        assertTrue("Should contain frozen", keywordNames.contains("frozen"));
    }
    
    @Test
    public void testStatistics() {
        Map<String, Object> stats = ActionRegistry.getStatistics();
        assertNotNull(stats);
        assertTrue("Should have total count", stats.containsKey("total"));
        assertTrue("Should have implemented count", stats.containsKey("implemented"));
        assertTrue("Should have unimplemented count", stats.containsKey("unimplemented"));
        
        int total = (Integer) stats.get("total");
        assertTrue("Should have at least 3 actions", total >= 3);
    }
    
    @Test
    public void testParameterChecking() {
        // Test with empty params map (should pass for basic commands)
        Map<String, Object> emptyParams = new HashMap<>();
        assertTrue("ping should accept empty params", 
                   ActionRegistry.hasRequiredParams("ping", emptyParams));
    }
}