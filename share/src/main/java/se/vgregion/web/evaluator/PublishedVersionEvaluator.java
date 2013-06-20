package se.vgregion.web.evaluator;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.json.simple.JSONObject;

import se.vgregion.web.scripts.PublishedDocumentService;
import se.vgregion.web.scripts.PublishedStatus;

public class PublishedVersionEvaluator extends BaseEvaluator {

  private PublishedDocumentService _publishedDocumentService;

  public void setPublishedDocumentService(PublishedDocumentService publishedDocumentService) {
    _publishedDocumentService = publishedDocumentService;
  }

  @Override
  public boolean evaluate(JSONObject jsonObject) {
    String nodeRef = (String) jsonObject.get("nodeRef");

    PublishedStatus status = _publishedDocumentService.getPublishedStatus(nodeRef);

    return status.published;
  }

}
