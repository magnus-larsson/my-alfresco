package se.vgregion.alfresco.repo.constraints;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import se.vgregion.alfresco.repo.model.ApelonNode;

public interface ApelonService {

  static final String PROFILE_ID = "vgr_8bf361b9f29e0f0369eb6367b9cb753d";

  List<ApelonNode> getVocabulary(String path);

  List<ApelonNode> getVocabulary(String path, boolean sort);

  List<ApelonNode> getVocabulary(String path, boolean sort, int returnSize);

  List<ApelonNode> findNodes(String namespace, String propertyName, String propertyValue);

  List<ApelonNode> findNodes(String namespace, String propertyName, String propertyValue, boolean sort);

  List<ApelonNode> findNodes(String namespace, String propertyName, String propertyValue, boolean sort, int returnSize);

  List<NodeRef> getNodes(String apelonType);

  List<NodeRef> getNodes(String apelonType, boolean sort);

  List<NodeRef> getRecordTypeList(String documentTypeId);

  List<NodeRef> getRecordTypeList(String documentTypeId, boolean sort);

  List<NodeRef> getDocumentTypeList();

  List<NodeRef> getDocumentTypeList(boolean sort);

  List<NodeRef> getDocumentStatusList();

  List<NodeRef> getDocumentStatusList(boolean sort);

  List<NodeRef> getLanguageList();

  List<NodeRef> getLanguageList(boolean sort);

  List<NodeRef> getHsacodeList();

  List<NodeRef> getHsacodeList(boolean sort);

  ApelonNode getRecordTypeApelonNode(String documentType, String recordType);

  ApelonNode getDocumentTypeApelonNode(String documentType);

  NodeRef getRecordTypeFromPath(String recordTypePath);

  List<ApelonNode> getKeywords(String text);

}
