package se.vgregion.alfresco.repo.scripts;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TriggerUserUpdateTest {
  
  @Test
  public void testExecute() {
    TriggerUserUpdate triggerUserUpdate = new TriggerUserUpdate();

    WebScriptRequest req = null;
    Status status = null;
    Cache cache = null;
    
    // triggerUserUpdate.executeImpl(req, status, cache);
  }

  @Test
  public void testStartingLetters() {
    TriggerUserUpdate triggerUserUpdate = new TriggerUserUpdate();
    
    String[] startingLetters = triggerUserUpdate.getStartingLetters();
    
    assertEquals("aa", startingLetters[0].substring(0, 2));
    assertEquals("ne", startingLetters[342].substring(0, 2));
  }

}
