package potatoclient.transit;

import com.cognitect.transit.Keyword;
import com.cognitect.transit.TransitFactory;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

public class ActionDefinitionTest {
    
    @Test
    public void testBuilderBasic() {
        ActionDefinition action = new ActionDefinition.Builder("test-action")
                .description("Test action")
                .build();
        
        assertEquals("test-action", action.getName());
        assertEquals("Test action", action.getDescription());
        assertFalse("Should not be implemented by default", action.isImplemented());
        assertTrue("Should have no required params", action.getRequiredParams().isEmpty());
        assertTrue("Should have no optional params", action.getOptionalParams().isEmpty());
    }
    
    @Test
    public void testBuilderWithParameters() {
        ActionDefinition action = new ActionDefinition.Builder("test-with-params")
                .description("Test with parameters")
                .required("x", "y", "channel")
                .optional("speed", "duration")
                .implemented()
                .build();
        
        assertEquals(3, action.getRequiredParams().size());
        assertEquals(2, action.getOptionalParams().size());
        assertTrue(action.isImplemented());
        
        // Check parameter keywords
        Keyword xKeyword = TransitFactory.keyword("x");
        assertTrue("Should have x as required", action.isRequired(xKeyword));
        assertTrue("Should have x as param", action.hasParam(xKeyword));
        
        Keyword speedKeyword = TransitFactory.keyword("speed");
        assertFalse("speed should not be required", action.isRequired(speedKeyword));
        assertTrue("Should have speed as param", action.hasParam(speedKeyword));
    }
    
    @Test
    public void testKeywordCreation() {
        ActionDefinition action = new ActionDefinition.Builder("kebab-case-action")
                .build();
        
        Keyword keyword = action.getKeyword();
        assertNotNull(keyword);
        assertEquals("kebab-case-action", keyword.getName());
    }
    
    @Test
    public void testGetAllParams() {
        ActionDefinition action = new ActionDefinition.Builder("all-params-test")
                .required("req1", "req2")
                .optional("opt1", "opt2", "opt3")
                .build();
        
        Set<Keyword> allParams = action.getAllParams();
        assertEquals(5, allParams.size());
        
        // Check all params are present
        assertTrue(action.hasParam(TransitFactory.keyword("req1")));
        assertTrue(action.hasParam(TransitFactory.keyword("opt3")));
    }
    
    @Test
    public void testImmutability() {
        ActionDefinition action = new ActionDefinition.Builder("immutable-test")
                .required("x", "y")
                .build();
        
        Set<Keyword> required = action.getRequiredParams();
        int originalSize = required.size();
        
        // Try to modify (should throw exception)
        try {
            required.add(TransitFactory.keyword("z"));
            fail("Should not be able to modify required params");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        
        assertEquals(originalSize, action.getRequiredParams().size());
    }
    
    @Test
    public void testUnknownParameter() {
        ActionDefinition action = new ActionDefinition.Builder("param-test")
                .required("x")
                .build();
        
        Keyword unknownParam = TransitFactory.keyword("unknown");
        assertFalse("Should not have unknown param", action.hasParam(unknownParam));
        assertFalse("Unknown param should not be required", action.isRequired(unknownParam));
    }
}