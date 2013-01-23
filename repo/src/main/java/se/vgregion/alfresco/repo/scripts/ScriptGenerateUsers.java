package se.vgregion.alfresco.repo.scripts;

import org.alfresco.repo.processor.BaseProcessorExtension;

import se.vgregion.alfresco.repo.security.GenerateUsersService;

public class ScriptGenerateUsers extends BaseProcessorExtension {

  private GenerateUsersService _generateUsersService;

  public void setGenerateUsersService(final GenerateUsersService generateUsersService) {
    _generateUsersService = generateUsersService;
  }

  public int generateUsers(final int amount) {
    return _generateUsersService.generateUsers(amount);
  }

}
