package se.vgregion.alfresco.repo.admin.patch.impl;

import org.junit.Assert;
import org.junit.Test;

import se.vgregion.alfresco.repo.admin.patch.impl.FixMissingIdValuesJob.PatchContributorSavedby;

public class FixMissingIdValuesPatchTest {

  @Test
  public void testGetParenthesisValueSuccess1() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "Niklas Ekman (nike) Redpill Linpro";

    final String result = patch.getParenthesisValue(value);

    Assert.assertEquals("nike", result);
  }

  @Test
  public void testGetParenthesisValueSuccess2() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "Niklas Ekman (nike)";

    final String result = patch.getParenthesisValue(value);

    Assert.assertEquals("nike", result);
  }

  @Test
  public void testGetParenthesisValueFailure() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "Niklas Ekman";

    final String result = patch.getParenthesisValue(value);

    Assert.assertEquals("Niklas Ekman", result);
  }

  public void testGetValueBeforePipeSuccess() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "nike|Niklas Ekman";

    final String result = patch.getValueBeforePipe(value);

    Assert.assertEquals("nike", result);
  }

  @Test
  public void testGetValueBeforePipeFailure() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "Niklas Ekman";

    final String result = patch.getValueBeforePipe(value);

    Assert.assertEquals("Niklas Ekman", result);
  }

  @Test
  public void testGetValueAfterPipeSuccess() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "nike|Niklas Ekman";

    final String result = patch.getValueAfterPipe(value);

    Assert.assertEquals("Niklas Ekman", result);
  }

  @Test
  public void testGetValueAfterPipeFailure() {
    final FixMissingIdValuesJob instance = new FixMissingIdValuesJob();

    final PatchContributorSavedby patch = instance.new PatchContributorSavedby();

    final String value = "Niklas Ekman";

    final String result = patch.getValueAfterPipe(value);

    Assert.assertEquals("Niklas Ekman", result);
  }

}
