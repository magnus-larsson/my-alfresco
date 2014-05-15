package se.vgregion.alfresco.repo.scripts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TriggerUserUpdateTest {
  
  @Test
  public void testStartingLetters() {
    TriggerUserUpdate triggerUserUpdate = new TriggerUserUpdate();
    
    String[] startingLetters = triggerUserUpdate.getStartingLetters();
    
    assertEquals("aa", startingLetters[0].substring(0, 2));
    assertEquals("ne", startingLetters[342].substring(0, 2));
  }

}
