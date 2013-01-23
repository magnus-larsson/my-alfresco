package se.vgregion.alfresco.repo.scripts;

import junit.framework.Assert;

import org.junit.Test;

import se.vgregion.alfresco.repo.security.GenerateUsersService;

public class ScriptGenerateUsersTest {

  @Test
  public void testGenerateUsersSuccess() {
    final ScriptGenerateUsers script = new ScriptGenerateUsers();

    final GenerateUsersService generateUsersService = null;

    script.setGenerateUsersService(generateUsersService);

    final int generated = script.generateUsers(10);

    Assert.assertEquals(10, generated);
  }

}
